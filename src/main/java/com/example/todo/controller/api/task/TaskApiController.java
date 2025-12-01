package com.example.todo.controller.api.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.service.task.TaskService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * タスク管理用 REST API コントローラー
 * Vue.js（フロントエンド）からのリクエストを受け取り、JSON形式でレスポンスを返す
 * 
 * @RestController: HTMLではなくJSONを返すコントローラー
 * @RequestMapping("/api/tasks"): このクラスの全メソッドは /api/tasks 配下のURLになる
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {
    // タスクのビジネスロジックを担当するサービスクラス
    private final TaskService taskService;

    // コンストラクタインジェクション（Spring Bootが自動的にtaskServiceを注入）
    public TaskApiController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 全タスク取得（検索条件付き）
     * 
     * エンドポイント: GET /api/tasks
     * Vue.jsの一覧画面から呼び出される
     * 
     * @param searchForm 検索条件（概要のキーワード、ステータス）
     * @return タスク一覧のJSON配列
     * 
     * 例: GET /api/tasks?summary=Spring&status=TODO,DOING
     * 戻り値: [{"id":1,"summary":"...","status":"TODO"},...]
     */
    @GetMapping
    public ResponseEntity<List<TaskApiDTO>> getAllTasks(
            @ModelAttribute TaskSearchForm searchForm) {
        // 1. 検索条件をEntityに変換
        // 2. サービスクラスでデータベース検索
        // 3. 取得したEntityをDTOに変換（JSONで返すため）
        var taskList = taskService.find(searchForm.toEntity())
                .stream()
                .map(TaskApiDTO::toDTO)
                .toList();
        
        // 200 OKレスポンスとしてJSON配列を返す
        return ResponseEntity.ok(taskList);
    }

    /**
     * タスク詳細取得
     * 
     * エンドポイント: GET /api/tasks/{id}
     * Vue.jsの詳細画面・編集画面から呼び出される
     * 
     * @param taskId 取得するタスクのID（URLパスから取得）
     * @return タスクのJSON、見つからない場合は404
     * 
     * 例: GET /api/tasks/1
     * 戻り値: {"id":1,"summary":"Spring Bootを学ぶ","status":"TODO"}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskApiDTO> getTask(@PathVariable("id") long taskId) {
        // データベースからIDで検索し、DTOに変換
        var taskDTO = taskService.findById(taskId)
                .map(TaskApiDTO::toDTO)
                .orElse(null);
        
        // タスクが見つからない場合は404 Not Foundを返す
        if (taskDTO == null) {
            return ResponseEntity.notFound().build();
        }
        // タスクが見つかった場合は200 OKとJSONを返す
        return ResponseEntity.ok(taskDTO);
    }

    /**
     * タスク作成
     * 
     * エンドポイント: POST /api/tasks
     * Vue.jsの作成フォームから呼び出される
     * 
     * @param request タスク作成リクエスト（JSON形式で受け取る）
     * @return 作成されたタスクのJSON、201 Created
     * 
     * リクエスト例: POST /api/tasks
     * Body: {"summary":"新しいタスク","description":"詳細","status":"TODO"}
     * 戻り値: {"id":5,"summary":"新しいタスク",...}
     */
    @PostMapping
    public ResponseEntity<TaskApiDTO> createTask(@Validated @RequestBody TaskApiCreateRequest request) {
        // 1. リクエストをEntityに変換
        var entity = request.toEntity();
        // 2. サービスクラスでデータベースに保存
        var createdEntity = taskService.create(entity);
        // 3. 作成されたタスクをDTOに変換し、201 Createdで返す
        return ResponseEntity.status(201).body(TaskApiDTO.toDTO(createdEntity));
    }

    /**
     * タスク更新
     * 
     * エンドポイント: PUT /api/tasks/{id}
     * Vue.jsの編集フォームから呼び出される
     * 
     * @param id 更新するタスクのID（URLパスから取得）
     * @param request タスク更新リクエスト（JSON形式で受け取る）
     * @return 更新されたタスクのJSON、200 OK
     * 
     * リクエスト例: PUT /api/tasks/1
     * Body: {"summary":"更新後のタスク","description":"詳細","status":"DOING"}
     * 戻り値: {"id":1,"summary":"更新後のタスク",...}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskApiDTO> updateTask(
            @PathVariable("id") long id,
            @Validated @RequestBody TaskApiUpdateRequest request) {
        // 1. リクエストとIDをEntityに変換
        var entity = request.toEntity(id);
        // 2. サービスクラスでデータベースを更新
        var updatedEntity = taskService.update(entity);
        // 3. 更新されたタスクをDTOに変換し、200 OKで返す
        return ResponseEntity.ok(TaskApiDTO.toDTO(updatedEntity));
    }

    /**
     * タスク削除
     * 
     * エンドポイント: DELETE /api/tasks/{id}
     * Vue.jsの詳細画面から呼び出される
     * 
     * @param id 削除するタスクのID（URLパスから取得）
     * @return 204 No Content（レスポンスボディなし）
     * 
     * リクエスト例: DELETE /api/tasks/1
     * 戻り値: なし（ステータスコード204のみ）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") long id) {
        // サービスクラスでデータベースから削除
        taskService.delete(id);
        // 204 No Content（削除成功、レスポンスボディなし）を返す
        return ResponseEntity.noContent().build();
    }
}
