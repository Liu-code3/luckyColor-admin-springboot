package com.luckycolor.admin.modules.platform.docs.web;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Hidden
public class ApiDocsController {

    @GetMapping(value = {"/docs", "/docs/"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String renderDocs(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>LuckyColor 接口文档</title>
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
                const swaggerZhMap = new Map([
                  ["Authorize", "认证授权"],
                  ["Authorized", "已授权"],
                  ["Available authorizations", "可用授权"],
                  ["Close", "关闭"],
                  ["Scopes", "作用域"],
                  ["Scope", "作用域"],
                  ["Value", "参数值"],
                  ["Example Value", "示例值"],
                  ["Example", "示例"],
                  ["Schema", "数据结构"],
                  ["Schemas", "数据模型"],
                  ["Model", "模型"],
                  ["Models", "模型"],
                  ["Parameters", "请求参数"],
                  ["Request body", "请求体"],
                  ["Responses", "响应"],
                  ["Response headers", "响应头"],
                  ["Response body", "响应体"],
                  ["Server response", "服务器响应"],
                  ["Response content type", "响应内容类型"],
                  ["Request URL", "请求地址"],
                  ["Request duration", "请求耗时"],
                  ["Code", "状态码"],
                  ["Details", "详情"],
                  ["No parameters", "无请求参数"],
                  ["No operations defined in spec!", "当前文档中没有可展示的接口"],
                  ["Try it out", "在线调试"],
                  ["Execute", "执行"],
                  ["Clear", "清空"],
                  ["Cancel", "取消"],
                  ["Description", "说明"],
                  ["Name", "名称"],
                  ["Expand all", "展开全部"],
                  ["Collapse all", "折叠全部"],
                  ["Download", "下载"],
                  ["Server", "服务器"],
                  ["Servers", "服务器"],
                  ["default", "默认"],
                  ["string", "字符串"],
                  ["integer", "整数"],
                  ["boolean", "布尔值"],
                  ["array", "数组"],
                  ["object", "对象"]
                ]);

                function translateSwaggerUi(root) {
                  if (!root) {
                    return;
                  }
                  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
                    acceptNode(node) {
                      const parent = node.parentElement;
                      if (!parent) {
                        return NodeFilter.FILTER_REJECT;
                      }
                      if (["SCRIPT", "STYLE", "CODE", "PRE"].includes(parent.tagName)) {
                        return NodeFilter.FILTER_REJECT;
                      }
                      const text = node.nodeValue.trim();
                      if (!text || !swaggerZhMap.has(text)) {
                        return NodeFilter.FILTER_SKIP;
                      }
                      return NodeFilter.FILTER_ACCEPT;
                    }
                  });
                  const textNodes = [];
                  while (walker.nextNode()) {
                    textNodes.push(walker.currentNode);
                  }
                  textNodes.forEach((node) => {
                    const source = node.nodeValue.trim();
                    const target = swaggerZhMap.get(source);
                    if (target) {
                      node.nodeValue = node.nodeValue.replace(source, target);
                    }
                  });
                  root.querySelectorAll("[placeholder], [title], [aria-label]").forEach((element) => {
                    ["placeholder", "title", "aria-label"].forEach((attr) => {
                      const value = element.getAttribute(attr);
                      if (value && swaggerZhMap.has(value)) {
                        element.setAttribute(attr, swaggerZhMap.get(value));
                      }
                    });
                  });
                }

                window.ui = SwaggerUIBundle({
                  url: "%s/v3/api-docs",
                  dom_id: "#swagger-ui",
                  deepLinking: true,
                  presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                  ],
                  layout: "BaseLayout",
                  docExpansion: "list"
                });

                const root = document.getElementById("swagger-ui");
                const observer = new MutationObserver(() => translateSwaggerUi(root));
                observer.observe(root, { childList: true, subtree: true });
                window.addEventListener("load", () => translateSwaggerUi(root));
              </script>
            </body>
            </html>
            """.formatted(contextPath, contextPath, contextPath, contextPath);
    }
}
