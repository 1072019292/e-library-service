package dev.lizhi.elibrary;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceEntryController {
    @GetMapping("/")
    public ResponseEntity<Void> entry() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/api/books")).build();
    }
}
