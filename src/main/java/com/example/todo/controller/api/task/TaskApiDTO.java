package com.example.todo.controller.api.task;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskStatus;

/**
 * タスクAPIのレスポンス用DTO（Data Transfer Object）
 * 
 * 役割:
 * - データベースのデータ（Entity）をJSON形式に変換する
 * - Vue.jsに返すデータの形を定義
 * 
 * recordTypeを使用:
 * - 不変オブジェクト（一度作ったら変更できない）
 * - getterメソッドが自動生成される
 * 
 * @param id タスクID
 * @param summary タスク概要
 * @param description タスク詳細
 * @param status ステータス（文字列形式: "TODO", "DOING", "DONE"）
 */
public record TaskApiDTO(
        long id,
        String summary,
        String description,
        String status
) {
    /**
     * EntityからDTOへ変換するファクトリーメソッド
     * 
     * @param entity データベースから取得したTaskEntity
     * @return JSONで返すためのTaskApiDTO
     * 
     * 使用例:
     * TaskEntity entity = taskService.findById(1);
     * TaskApiDTO dto = TaskApiDTO.toDTO(entity);
     * // dtoはJSONに変換されてブラウザに返される
     */
    public static TaskApiDTO toDTO(TaskEntity entity) {
        return new TaskApiDTO(
                entity.id(),
                entity.summary(),
                entity.description(),
                entity.status().name()  // Enumを文字列に変換
        );
    }

    /**
     * DTOからEntityへ変換するメソッド（必要に応じて使用）
     * 
     * @return データベース操作用のTaskEntity
     */
    public TaskEntity toEntity() {
        return new TaskEntity(id, summary, description, TaskStatus.valueOf(status));
    }
}
