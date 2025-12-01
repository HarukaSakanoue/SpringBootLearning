package com.example.todo.controller.api.task;

import com.example.todo.service.task.TaskSearchEntity;
import com.example.todo.service.task.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * タスク検索フォーム用クラス
 * 
 * 役割:
 * - Vue.jsから送られる検索条件を受け取る
 * - クエリパラメータをEntityに変換する
 * 
 * リクエスト例:
 * GET /api/tasks?summary=Spring&status=TODO&status=DOING
 * 
 * @param summary 検索キーワード（概要に部分一致）
 * @param status ステータスのリスト（複数指定可能）
 */
public record TaskSearchForm(
        String summary,      // 検索キーワード（任意）
        List<String> status  // ステータスリスト（任意）
) {
    /**
     * フォームデータを検索用Entityに変換する
     * 
     * @return データベース検索用のTaskSearchEntity
     * 
     * 処理の流れ:
     * 1. statusリストがnullの場合は空リストに変換
     * 2. 文字列のステータスをEnum型に変換
     * 3. TaskSearchEntityを生成して返す
     */
    public TaskSearchEntity toEntity() {
        // statusがnullまたは空の場合、空リストを使用
        var statusEntityList = Optional.ofNullable(status())
                .map(statusList -> statusList.stream().map(TaskStatus::valueOf).toList())
                .orElse(List.of());

        return new TaskSearchEntity(summary(), statusEntityList);
    }

    /**
     * 指定されたステータスがチェックされているか判定
     * 
     * @param status チェックするステータス文字列
     * @return リストに含まれていればtrue
     * 
     * 使用例（Thymeleaf用、Vue.jsでは未使用）:
     * th:checked="${searchForm.isChecked('TODO')}"
     */
    public boolean isChecked(String status) {
        return Optional.ofNullable(this.status())
                .map(statusList -> statusList.contains(status))
                .orElse(false);
    }
}
