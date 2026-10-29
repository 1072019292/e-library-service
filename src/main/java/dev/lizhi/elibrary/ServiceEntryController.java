package dev.lizhi.elibrary;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServiceEntryController {
    @GetMapping("/")
    public String entry() {
        return "forward:/index.html";
    }
}
