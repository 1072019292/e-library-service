package dev.lizhi.elibrary.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 200)
    private String author;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(nullable = false)
    private int totalLicenses;

    protected Book() { }

    public Book(String title, String author, String description, int totalLicenses) {
        if (totalLicenses < 1) { throw new IllegalArgumentException("At least one license is required."); }
        this.title = title;
        this.author = author;
        this.description = description;
        this.totalLicenses = totalLicenses;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public int getTotalLicenses() { return totalLicenses; }
}
