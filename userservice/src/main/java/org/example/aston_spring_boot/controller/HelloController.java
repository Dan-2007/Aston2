package org.example.aston_spring_boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String home() {
        return """
            <html>
                <body>
                    <h1>User Management API</h1>
                    <p>Available endpoints:</p>
                    <ul>
                        <li><a href="/swagger-ui.html">Swagger UI Documentation</a></li>
                        <li><a href="/v3/api-docs">OpenAPI JSON</a></li>
                        <li><a href="/api/users">Users API (тут пусто, если вы не добавляли пользователей)</a></li>
                    </ul>
                </body>
            </html>
            """;
    }
}
