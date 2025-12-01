package com.example.todo.controller.api.task;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 共通API例外ハンドラ。
 * 現時点ではバリデーションエラー (MethodArgumentNotValidException) を統一したJSON形式で返す。
 * 目的: フロント(Vue)側が安定した `errors` 配列をパースできるようにする。
 *
 * レスポンス例:
 * {
 *   "message": "Validation failed",
 *   "errors": [
 *     { "field": "summary", "defaultMessage": "概要は必須です", "code": "NotBlank" }
 *   ]
 * }
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("field", fe.getField());
                    m.put("defaultMessage", fe.getDefaultMessage());
                    m.put("code", fe.getCode());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }
}
