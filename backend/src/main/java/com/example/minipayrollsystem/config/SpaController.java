package com.example.minipayrollsystem.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards Angular client routes to index.html when the UI is packaged
 * inside Spring Boot static resources (single-URL deployment).
 */
@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/employees",
            "/attendance",
            "/leave",
            "/payroll"
    })
    public String forwardAngularRoutes() {
        return "forward:/index.html";
    }
}
