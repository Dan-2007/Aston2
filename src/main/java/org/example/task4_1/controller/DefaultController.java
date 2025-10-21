package org.example.task4_1.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {
    @GetMapping("/")
    public String home() {
        return """
            <html>
                <body>
                    <div class='wrapper' style = "position: absolute; top:0; left: 0; width:100%; height: 100%; overflow: auto;">
                        <div style="font-size: 220%; font-family: monospace; color: #cd66cc; position:absolute; top:5%; left:50%;">
                            USER MENU
                            <div style="font-size: 50%; display:block; font-family: monospace; color: #ffffff; position:relative; top:10%; margin:auto;">
                                <a style = "text-align:center;" href="/api/users">
                                    SEE ALL USERS
                                </a>
                            </div>
                        </div>
                    </div>
                </body>
            </html>
            """;
    }
}