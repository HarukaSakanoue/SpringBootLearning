package com.example.todo.controller.api.task;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskStatus;
import com.example.todo.validation.ValidationMessages;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * タスク作成リクエスト用クラス
 * 
 * 役割:
 * - Vue.jsから送られるタスク作成データを受け取る
 * - バリデーション（入力チェック）を実施
 * 
 * リクエスト例:
 * POST /api/tasks
 * Body: {"summary":"新しいタスク","description":"詳細","status":"TODO"}
 * 
 * @param summary タスク概要（必須、256文字以内）
 * @param description タスク詳細（任意）
 * @param status ステータス（必須、TODO/DOING/DONEのいずれか）
 */
public record TaskApiCreateRequest(
        // 必須入力（空文字やnull不可）
        @NotBlank(message = ValidationMessages.SUMMARY_REQUIRED)
        // 最大256文字まで
        @Size(max = 256, message = ValidationMessages.SUMMARY_SIZE)
        String summary,

        // 詳細は任意項目（バリデーションなし）
        String description,

        // 必須入力
        @NotBlank(message = ValidationMessages.STATUS_REQUIRED)
        // 正規表現でTODO, DOING, DONEのいずれかを強制
        @Pattern(regexp = "TODO|DOING|DONE", message = ValidationMessages.STATUS_PATTERN)
        String status
) {
    /**
     * リクエストデータをEntityに変換する
     * 
     * @return データベースに保存するためのTaskEntity
     * 
     * 注意: IDはnull（データベースが自動採番）
     */
    public TaskEntity toEntity() {
        return new TaskEntity(null, summary, description, TaskStatus.valueOf(status));
    }
}
