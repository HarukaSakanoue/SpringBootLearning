package com.example.todo.controller.api.task;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskStatus;
import com.example.todo.validation.ValidationMessages;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * タスク更新リクエスト用クラス
 * 
 * 役割:
 * - Vue.jsから送られるタスク更新データを受け取る
 * - バリデーション（入力チェック）を実施
 * 
 * リクエスト例:
 * PUT /api/tasks/1
 * Body: {"summary":"更新後のタスク","description":"詳細","status":"DOING"}
 * 
 * 注意: バリデーションルールはTaskApiCreateRequestと同じ
 * 
 * @param summary タスク概要（必須、256文字以内）
 * @param description タスク詳細（任意）
 * @param status ステータス（必須、TODO/DOING/DONEのいずれか）
 */
public record TaskApiUpdateRequest(
        // 必須入力（空文字やnull不可）
        @NotBlank(message=ValidationMessages.SUMMARY_REQUIRED)
        // 最大256文字まで
        @Size(max = 256, message = ValidationMessages.SUMMARY_SIZE)
        String summary,

        // 詳細は任意項目
        String description,

        // 必須入力
        @NotBlank(message=ValidationMessages.STATUS_REQUIRED)
        // TODO, DOING, DONEのいずれかを強制
        @Pattern(regexp = "TODO|DOING|DONE", message = ValidationMessages.STATUS_PATTERN)
        String status
) {
    /**
     * リクエストデータとIDをEntityに変換する
     * 
     * @param id 更新対象のタスクID（URLパスから取得）
     * @return データベースを更新するためのTaskEntity
     * 
     * CreateRequestとの違い: IDが指定される（既存タスクの更新）
     */
    public TaskEntity toEntity(long id) {
        return new TaskEntity(id, summary, description, TaskStatus.valueOf(status));
    }
}
