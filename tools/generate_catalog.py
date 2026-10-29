"""Offline sample-data generator. Default checks V6; --output creates a NEW migration file."""
from pathlib import Path
import argparse
import json

ROOT = Path(__file__).resolve().parents[1]


def literal(value):
    if value is None:
        return 'NULL'
    if isinstance(value, bool):
        return 'TRUE' if value else 'FALSE'
    if isinstance(value, int):
        return str(value)
    return "'" + str(value).replace("'", "''") + "'"


def generate():
    books = json.loads((ROOT / 'catalog/books.json').read_text(encoding='utf-8'))
    sources = json.loads((ROOT / 'catalog/sources.json').read_text(encoding='utf-8'))
    statements = [
        '-- Curated sample bibliography. Descriptions and reading notes are original editorial text.',
        '-- Demo capacities are illustrative. Existing book IDs, capacities and loan history are preserved.',
        '-- Cover thumbnails use the source URLs as requested by Open Library; offline typography is the fallback.',
    ]
    for index, book in enumerate(books, 1):
        source = sources[book['slug']]
        fields = {
            'author': book['author'], 'description': book['description'], 'category': book['category'],
            'audience': book['audience'], 'highlights': '\n'.join(book['highlights']),
            'cover_url': source.get('coverSource'), 'source_url': source['source'], 'featured': book['featured'],
        }
        condition = 'title = ' + literal(book['title'])
        statements.append('UPDATE books SET ' + ', '.join(key + ' = ' + literal(value) for key, value in fields.items())
                          + ' WHERE ' + condition + ';')
        values = {'title': book['title'], **fields, 'total_licenses': 1 + index % 3}
        statements.append('INSERT INTO books (' + ', '.join(values) + ') SELECT ' + ', '.join(map(literal, values.values()))
                          + ' WHERE NOT EXISTS (SELECT 1 FROM books WHERE ' + condition + ');')
    return '\n\n'.join(statements) + '\n'


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, help='New migration path; never overwrite an applied migration')
    args = parser.parse_args()
    result = generate()
    if args.output:
        with args.output.open('x', encoding='utf-8') as target:
            target.write(result)
        print('Created ' + str(args.output))
    else:
        existing = ROOT / 'src/main/resources/db/demo/V6__expanded_editorial_catalog.sql'
        if existing.read_text(encoding='utf-8') != result:
            raise SystemExit('Catalog differs from V6. Create a NEW migration with --output; do not rewrite deployed V6.')
        print('CATALOG_MIGRATION_MATCH')
