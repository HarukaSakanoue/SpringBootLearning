package com.example.todo.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST APIの例外を一元管理するグローバル例外ハンドラー
 * 
 * 役割:
 * - バリデーションエラーを統一フォーマットで返す
 * - フロントエンド（Vue.js）が常に同じ形式で受け取れるようにする
 * 
 * @RestControllerAdvice: 全ての @RestController に適用される例外ハンドラー
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * バリデーションエラー（@Validatedによる検証失敗）をハンドリング
     * 
     * 発生タイミング:
     * - POST /api/tasks で summary が空の場合
     * - PUT /api/tasks/{id} で status が不正な値の場合
     * 
     * @param ex MethodArgumentNotValidException（バリデーション失敗時の例外）
     * @return 統一フォーマットのエラーレスポンス
     * 
     * レスポンス例:
     * {
     *   "errors": [
     *     {
     *       "field": "summary",
     *       "defaultMessage": "概要は必須です"
     *     }
     *   ]
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        // 1. 例外からフィールドエラー一覧を取得
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        
        // 2. フィールドエラーをDTOに変換
        List<ValidationErrorResponse.FieldErrorDetail> errors = fieldErrors.stream()
                .map(error -> new ValidationErrorResponse.FieldErrorDetail(
                        error.getField(),                    // field: "summary"
                        error.getDefaultMessage()            // defaultMessage: "概要は必須です"
                ))
                .collect(Collectors.toList());
        
        // 3. 統一フォーマットのレスポンスを作成
        ValidationErrorResponse response = new ValidationErrorResponse(errors);
        
        // 4. 400 Bad Request として返す
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * バリデーションエラーのレスポンス形式
     * 
     * フロントエンド（Vue.js）が受け取る形式:
     * {
     *   "errors": [
     *     { "field": "summary", "defaultMessage": "概要は必須です" }
     *   ]
     * }
     */
    public record ValidationErrorResponse(
            List<FieldErrorDetail> errors
    ) {
        /**
         * 各フィールドのエラー詳細
         * 
         * @param field エラーが発生したフィールド名（例: "summary", "status"）
         * @param defaultMessage エラーメッセージ（例: "概要は必須です"）
         */
        public record FieldErrorDetail(
                String field,
                String defaultMessage
        ) {}
    }
}
