package com.rick.admin.module.common.controller;

import com.rick.common.util.Time2StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * @author Rick.Xu
 * @date 2026/1/21 16:31
 */
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_admin')")
@RequestMapping("logs")
public class LogController {

    @Value("${logging.file.path}")
    private String logFilePath;

//    @GetMapping(produces = "text/plain;charset=UTF-8")
//    public String info() throws IOException {
//        File path = new File(logFilePath);
//        File infoLog = new File(path, "logback."+Time2StringUtils.format(LocalDate.now())+".log");
//        return FileUtils.readFileToString(infoLog, Charset.defaultCharset());
//    }

//    @GetMapping(produces = "text/html;charset=UTF-8")
//    public String infoPage() {
//        return "<!DOCTYPE html>" +
//                "<html>" +
//                "<head>" +
//                "    <meta charset='UTF-8'>" +
//                "    <title>日志信息</title>" +
//                "    <style>" +
//                "        body { margin: 0; padding: 20px; font-family: monospace; background: #1e1e1e; color: #d4d4d4; }" +
//                "        pre { white-space: pre-wrap; word-wrap: break-word; }" +
//                "        #timer { position: fixed; top: 10px; right: 10px; background: rgba(0,0,0,0.7); padding: 10px; border-radius: 5px; }" +
//                "    </style>" +
//                "</head>" +
//                "<body>" +
//                "    <div id='timer'>下次更新: <span id='countdown'>10</span>秒</div>" +
//                "    <pre id='logContent'>加载中...</pre>" +
//                "    <script>" +
//                "        function loadLog() {" +
//                "            fetch('/logs/api')" +
//                "                .then(response => response.text())" +
//                "                .then(data => {" +
//                "                    document.getElementById('logContent').textContent = data;" +
//                "                    window.scrollTo(0, document.body.scrollHeight);" +
//                "                });" +
//                "        }" +
//                "        " +
//                "        loadLog();" +
//                "        " +
//                "        let seconds = 10;" +
//                "        setInterval(function() {" +
//                "            seconds--;" +
//                "            document.getElementById('countdown').textContent = seconds;" +
//                "            if (seconds <= 0) {" +
//                "                loadLog();" +
//                "                seconds = 10;" +
//                "            }" +
//                "        }, 1000);" +
//                "    </script>" +
//                "</body>" +
//                "</html>";
//    }

    @GetMapping(produces = "text/html;charset=UTF-8")
    public String infoPage() {
        // language=HTML
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <title>日志信息</title>" +
                "    <style>" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
                "        body { font-family: 'Courier New', monospace; background: #1e1e1e; color: #d4d4d4; }" +
                "        .header { position: sticky; top: 0; background: #2d2d2d; padding: 10px 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.3); z-index: 100; }" +
                "        .tabs { display: flex; gap: 10px; }" +
                "        .tab { padding: 10px 20px; background: #3c3c3c; border: none; color: #d4d4d4; cursor: pointer; border-radius: 5px 5px 0 0; transition: all 0.3s; }" +
                "        .tab:hover { background: #4c4c4c; }" +
                "        .tab.active { background: #007acc; color: white; }" +
                "        .timer { float: right; background: rgba(0,0,0,0.5); padding: 8px 15px; border-radius: 5px; font-size: 14px; }" +
                "        .content { padding: 20px; }" +
                "        pre { white-space: pre-wrap; word-wrap: break-word; line-height: 1.5; }" +
                "        .loading { text-align: center; padding: 50px; color: #888; }" +
                "        .error-log { color: #f48771; }" +
                "        .info-log { color: #4ec9b0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='header'>" +
                "        <div class='tabs'>" +
                "            <button class='tab active' data-level='info'>INFO 日志</button>" +
                "            <button class='tab' data-level='error'>ERROR 日志</button>" +
                "            <button class='tab' data-level='warn'>WARN 日志</button>" +
                "            <button class='tab' data-level='debug'>DEBUG 日志</button>" +
                "            <button class='tab' data-level='trace'>TRACE 日志</button>" +
                "        </div>" +
                "        <div class='timer'>下次更新: <span id='countdown'>10</span>秒</div>" +
                "    </div>" +
                "    <div class='content'>" +
                "        <pre id='logContent' class='info-log'>加载中...</pre>" +
                "    </div>" +
                "    <script>" +
                "        let currentLevel = 'info';" +
                "        let seconds = 10;" +
                "        let countdownInterval;" +
                "        " +
                "        function loadLog() {" +
                "            fetch('/logs/api?level=' + currentLevel)" +
                "                .then(response => response.text())" +
                "                .then(data => {" +
                "                    const logContent = document.getElementById('logContent');" +
                "                    logContent.textContent = data || '暂无日志';" +
                "                    logContent.className = currentLevel + '-log';" +
                "                    window.scrollTo(0, document.body.scrollHeight);" +
                "                })" +
                "                .catch(error => {" +
                "                    document.getElementById('logContent').textContent = '加载失败: ' + error.message;" +
                "                });" +
                "        }" +
                "        " +
                "        function resetTimer() {" +
                "            seconds = 10;" +
                "            document.getElementById('countdown').textContent = seconds;" +
                "        }" +
                "        " +
                "        function startCountdown() {" +
                "            if (countdownInterval) clearInterval(countdownInterval);" +
                "            " +
                "            countdownInterval = setInterval(function() {" +
                "                seconds--;" +
                "                document.getElementById('countdown').textContent = seconds;" +
                "                if (seconds <= 0) {" +
                "                    loadLog();" +
                "                    resetTimer();" +
                "                }" +
                "            }, 1000);" +
                "        }" +
                "        " +
                "        // Tab 切换\n" +
                "        document.querySelectorAll('.tab').forEach(tab => {" +
                "            tab.addEventListener('click', function() {" +
                "                // 更新 active 状态\n" +
                "                document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));" +
                "                this.classList.add('active');" +
                "                " +
                "                // 切换日志类型\n" +
                "                currentLevel = this.dataset.level;" +
                "                loadLog();" +
                "                resetTimer();" +
                "            });" +
                "        });" +
                "        " +
                "        // 初始化\n" +
                "        loadLog();" +
                "        startCountdown();" +
                "    </script>" +
                "</body>" +
                "</html>";
    }

    @GetMapping(value = "api", produces = "text/plain;charset=UTF-8")
    public String infoApi(@RequestParam(defaultValue = "info") String level) throws IOException {
        // dev
//        File path = new File(logFilePath);
//        File infoLog = new File(path, "logback."+Time2StringUtils.format(LocalDate.now())+".log");

        // prod
        File path = new File(logFilePath + File.separator + Time2StringUtils.format(LocalDate.now()));
        File infoLog = new File(path, level + ".log");
        return FileUtils.readFileToString(infoLog, StandardCharsets.UTF_8);
    }
}
