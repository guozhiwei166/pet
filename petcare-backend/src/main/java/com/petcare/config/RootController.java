package com.petcare.config;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** 根路径引导页：直接在浏览器打开后端端口(8082)时，给出友好说明而非 401 JSON */
@Controller
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String index() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head><meta charset="UTF-8"><title>猫狗宠物寄养系统 API 服务</title>
                <style>
                  body{font-family:'Microsoft YaHei',sans-serif;background:#F7F5F0;display:flex;align-items:center;justify-content:center;height:100vh;margin:0}
                  .box{background:#fff;border-radius:18px;padding:44px 56px;text-align:center;box-shadow:0 20px 50px -24px rgba(47,70,57,.35);max-width:520px}
                  h1{color:#2F4639;margin:0 0 8px} p{color:#6B7F72;line-height:1.8;margin:6px 0}
                  .url{background:#EAF2ED;color:#2E7D57;font-weight:700;border-radius:10px;padding:10px 18px;display:inline-block;margin:12px 0;font-size:18px}
                  .api{font-size:12px;color:#9AA5A0;margin-top:14px}
                </style></head>
                <body>
                  <div class="box">
                    <h1>🐾 猫狗宠物寄养系统 API 服务</h1>
                    <p>这是后端接口服务（端口 8082），请在浏览器访问前端页面：</p>
                    <div class="url">http://localhost:5190</div>
                    <p>演示账号：user/user123 · staff/staff123 · admin/admin123</p>
                    <p class="api">接口文档基址：http://localhost:8082/api</p>
                  </div>
                </body></html>
                """;
    }
}
