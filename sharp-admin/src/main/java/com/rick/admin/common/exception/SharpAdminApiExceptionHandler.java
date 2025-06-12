package com.rick.admin.common.exception;

import com.rick.common.http.exception.ApiExceptionHandler;
import com.rick.common.http.model.Result;
import com.rick.notification.bark.PushNotification;
import com.rick.notification.bark.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Rick.Xu
 * @date 2025/4/27 18:23
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class SharpAdminApiExceptionHandler extends ApiExceptionHandler {

    private final PushNotificationService pushNotificationService;

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result elseExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException, ServletException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ex.printStackTrace(new PrintStream(baos));
        String exception = baos.toString();

        new Thread(() -> pushNotificationService.push(PushNotification.builder()
                .title("sharp-admin：服务器异常通知")
                .subtitle(ex.getMessage())
                .body("url:" + request.getRequestURI() + "\n" + exception.substring(0, exception.length() > 1000 ? 1000 : exception.length()))
                .badge(1)
                .sound("alarm")
                .icon("https://day.app/assets/images/avatar.jpg")
                .group("default")
                .url("https://sharp-admin.com/")
                .build())).start();
        return super.elseExceptionHandler(request, response, ex);
    }
}
