import { copyFile, mkdir } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const source = join(here, '../../src/main/resources/static');
const output = join(here, 'dist');
// Explicit published assets keep local, ignored scans and runtime data out of
// the edge bundle. These are the same files included in the application JAR.
const assets = [
  'index.html', 'assets/app.css', 'assets/app.js', 'assets/api.mjs',
  'assets/views.mjs', 'assets/icons.svg', 'assets/fonts/reading-serif.woff2',
  'assets/fonts/OFL.txt', 'assets/SOURCES.md', 'assets/PHOSPHOR-LICENSE.txt',
];
for (const asset of assets) {
  const destination = join(output, asset);
  await mkdir(dirname(destination), { recursive: true });
  await copyFile(join(source, asset), destination);
}
await copyFile(join(here, 'public/_worker.js'), join(output, '_worker.js'));
console.log(`EDGE_BUNDLE_READY: ${assets.length} application assets and the API gateway`);
