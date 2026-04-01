package com.luckycolor.admin.modules.platform.docs.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ApiDocsController {

    @GetMapping(value = {"/docs", "/docs/"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String renderDocs(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>LuckyColor API Docs</title>
              <link rel="stylesheet" href="%s/docs/assets/swagger-ui.css">
              <style>
                html { box-sizing: border-box; overflow-y: scroll; }
                *, *:before, *:after { box-sizing: inherit; }
                body { margin: 0; background: #fafafa; }
              </style>
            </head>
            <body>
              <div id="swagger-ui"></div>
              <script src="%s/docs/assets/swagger-ui-bundle.js"></script>
              <script src="%s/docs/assets/swagger-ui-standalone-preset.js"></script>
              <script>
                window.ui = SwaggerUIBundle({
                  url: "%s/v3/api-docs",
                  dom_id: "#swagger-ui",
                  deepLinking: true,
                  presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                  ],
                  layout: "BaseLayout"
                });
              </script>
            </body>
            </html>
            """.formatted(contextPath, contextPath, contextPath, contextPath);
    }
}
