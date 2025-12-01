package com.example.todo.controller.api.task;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskService;
import com.example.todo.service.task.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static com.codeborne.selenide.Condition.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskApiController の REST API 動作を検証するテストクラス
 * 
 * テスト方針:
 * - @WebMvcTest: Controllerだけをテストする（軽量・高速）
 * - Service層はモック化: 実際のDBアクセスはせず、動作を偽装する
 * - HTTPレイヤのみ検証: リクエスト/レスポンスの形式とステータスコードを確認
 * 
 * 検証内容:
 * 1. 正しいHTTPメソッド・パスでアクセスできるか
 * 2. リクエストパラメータやボディが正しく処理されるか
 * 3. レスポンスのステータスコード（200, 201, 400, 404など）が正しいか
 * 4. レスポンスのJSON形式が期待通りか
 * 5. バリデーションエラーが正しく返されるか
 */
@WebMvcTest(TaskApiController.class) // TaskApiControllerだけをテスト対象にする
class TaskApiControllerTest {

    /**
     * MockMvc: HTTPリクエストを模擬的に送信するツール
     * 実際のサーバを起動せずにControllerをテストできる
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * TaskService のモック（偽物）
     * 実際のビジネスロジックは実行せず、「こう返す」と決めた値を返す
     * これによりController単体の動作だけを検証できる
     */
    @MockBean
    TaskService taskService;

    /**
     * テスト1: タスク一覧取得API
     * 
     * 目的: GET /api/tasks が正しく動作するか確認
     * 
     * 検証内容:
     * - HTTPステータス: 200 OK
     * - レスポンス形式: JSON配列
     * - 配列のサイズ: 2件
     * - 各要素の内容: id, summary, statusが正しいか
     * 
     * テスト手順:
     * 1. モックの準備: taskService.find()が2件のタスクを返すよう設定
     * 2. リクエスト送信: GET /api/tasks?summary=タスク&status=TODO&status=DOING
     * 3. レスポンス検証: ステータスコードとJSON内容を確認
     */
    @Test
    @DisplayName("GET /api/tasks : タスク一覧取得が200と配列を返す")
    void getAllTasks_returnsList() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: TaskEntityを2件作成する（e1, e2）
        var e1 = new TaskEntity(1L, "タスク1", "詳細1", TaskStatus.TODO);
        var e2 = new TaskEntity(2L, "タスク2", "詳細2", TaskStatus.DOING);
        // ヒント2: when(taskService.find(any())).thenReturn(...) でモックの動作を定義
        when(taskService.find(any()))
                .thenReturn(List.of(e1, e2));
        // ヒント3: mockMvc.perform(get("/api/tasks").param(...)) でリクエスト送信
        mockMvc.perform(get("/api/tasks")
                .param("summary", "タスク")
                .param("status", "TODO")
                .param("status", "DOING"))
                // ヒント4: .andExpect(status().isOk()) でステータスコード確認
                .andExpect(status().isOk())
                // ヒント5: .andExpect(jsonPath("$", hasSize(2))) で配列サイズ確認
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].summary").value("タスク1"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[1].status").value("DOING"));
    }

    /**
     * テスト2: タスク詳細取得API（正常系）
     * 
     * 目的: GET /api/tasks/{id} で存在するタスクが取得できるか確認
     * 
     * 検証内容:
     * - HTTPステータス: 200 OK
     * - レスポンス内容: 指定したIDのタスク情報
     * 
     * テスト手順:
     * 1. モックの準備: taskService.findById(10L)がタスクを返すよう設定
     * 2. リクエスト送信: GET /api/tasks/10
     * 3. レスポンス検証: id, summary, statusが正しいか確認
     */
    @Test
    @DisplayName("GET /api/tasks/{id} : 存在するIDで200")
    void getTask_found() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: TaskEntity e1 = new TaskEntity(10L, "学習", "Vue移行", TaskStatus.TODO);
        var e1 = new TaskEntity(10L, "学習", "Vue移行", TaskStatus.TODO);
        // ヒント2: when(taskService.findById(eq(10L))).thenReturn(Optional.of(e1));
        when(taskService.findById(eq(10L))).thenReturn(Optional.of(e1));
        // ヒント3: mockMvc.perform(get("/api/tasks/10"))
        mockMvc.perform(get("/api/tasks/10"))
                // ヒント4: jsonPath("$.id").value(10L) でIDを確認
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.summary").value("学習"))
                .andExpect(jsonPath("$.description").value("Vue移行"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    /**
     * テスト3: タスク詳細取得API（異常系：存在しないID）
     * 
     * 目的: 存在しないIDを指定したときに404が返るか確認
     * 
     * 検証内容:
     * - HTTPステータス: 404 Not Found
     * 
     * テスト手順:
     * 1. モックの準備: taskService.findById(999L)が空のOptionalを返すよう設定
     * 2. リクエスト送信: GET /api/tasks/999
     * 3. レスポンス検証: 404ステータスコード
     */
    @Test
    @DisplayName("GET /api/tasks/{id} : 存在しないIDで404")
    void getTask_notFound() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: when(taskService.findById(eq(999L))).thenReturn(Optional.empty());
        when(taskService.findById(eq(999L))).thenReturn(Optional.empty());
        // ヒント2: .andExpect(status().isNotFound())
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    /**
     * テスト4: タスク作成API（正常系）
     * 
     * 目的: POST /api/tasks で新規タスクが作成できるか確認
     * 
     * 検証内容:
     * - HTTPステータス: 201 Created
     * - レスポンス内容: 作成されたタスク情報（IDを含む）
     * 
     * テスト手順:
     * 1. モックの準備: taskService.create()が作成済みタスクを返すよう設定
     * 2. リクエスト送信: POST /api/tasks（JSONボディ付き）
     * 3. レスポンス検証: 201ステータスとタスク情報
     */
    @Test
    @DisplayName("POST /api/tasks : 正常作成で201とJSON")
    void createTask_success() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: var created = new TaskEntity(100L, "新規タスク", "詳細", TaskStatus.TODO);
        var created = new TaskEntity(100L, "新規タスク", "詳細", TaskStatus.TODO);
        // ヒント2: when(taskService.create(any(TaskEntity.class))).thenReturn(created);
        when(taskService.create(any(TaskEntity.class))).thenReturn(created);
        // ヒント3: String body =
        // """{"summary":"新規タスク","description":"詳細","status":"TODO"}""";
        String body = """
                {"summary":"新規タスク","description":"詳細","status":"TODO"}
                """;
        // ヒント4:
        // mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // ヒント5: .andExpect(status().isCreated())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.summary").value("新規タスク"))
                .andExpect(jsonPath("$.description").value("詳細"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    /**
     * テスト5: タスク作成API（異常系：バリデーションエラー）
     * 
     * 目的: summary（概要）が空の場合に400エラーが返るか確認
     * 
     * 検証内容:
     * - HTTPステータス: 400 Bad Request
     * - レスポンス内容: errors配列に field="summary" のエラー情報
     * 
     * 重要ポイント:
     * - GlobalExceptionHandlerが統一形式 { errors: [...] } で返す
     * - フロントエンド（Vue）がこの形式を期待している
     * 
     * テスト手順:
     * 1. リクエスト送信: summary=""（空）でPOST
     * 2. レスポンス検証: 400ステータスとerrors配列の内容
     */
    @Test
    @DisplayName("POST /api/tasks : summary空で400 + errors配列")
    void createTask_validationError_summaryBlank() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: String body = """{"summary":"","description":"詳細","status":"TODO"}""";
        String body = """
                {"summary":"","description":"詳細","status":"TODO"}
                """;

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))

                // ヒント2: .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                // ヒント3: .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))))
                // ヒント4: .andExpect(jsonPath("$.errors[0].field", anyOf(is("summary"),
                // is("taskApiCreateRequest.summary"))))
                .andExpect(jsonPath("$.errors[0].field", anyOf(is("summary"),
                        is("taskApiCreateRequest.summary"))));
    }

    /**
     * テスト6: タスク更新API（正常系）
     * 
     * 目的: PUT /api/tasks/{id} でタスクが更新できるか確認
     * 
     * 検証内容:
     * - HTTPステータス: 200 OK
     * - レスポンス内容: 更新後のタスク情報
     * 
     * テスト手順:
     * 1. モックの準備: taskService.update()が更新済みタスクを返すよう設定
     * 2. リクエスト送信: PUT /api/tasks/5（JSONボディ付き）
     * 3. レスポンス検証: 200ステータスと更新内容
     */
    @Test
    @DisplayName("PUT /api/tasks/{id} : 正常更新で200")
    void updateTask_success() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: var updated = new TaskEntity(5L, "更新後", "説明", TaskStatus.DOING);
        var updated = new TaskEntity(5L, "更新後", "説明", TaskStatus.DOING);
        // ヒント2: when(taskService.update(any(TaskEntity.class))).thenReturn(updated);
        when(taskService.update(any(TaskEntity.class))).thenReturn(updated);
        // ヒント3: mockMvc.perform(put("/api/tasks/5")...)
        mockMvc.perform(put("/api/tasks/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"summary":"更新後","description":"説明","status":"DOING"}
                        """))
                // ヒント4: .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.summary").value("更新後"))
                .andExpect(jsonPath("$.description").value("説明"))
                .andExpect(jsonPath("$.status").value("DOING"));
    }

    /**
     * テスト7: タスク更新API（異常系：バリデーションエラー）
     * 
     * 目的: status（ステータス）が不正な値の場合に400エラーが返るか確認
     * 
     * 検証内容:
     * - HTTPステータス: 400 Bad Request
     * - レスポンス内容: errors配列に field="status" のエラー情報
     * 
     * テスト手順:
     * 1. リクエスト送信: status="INVALID"（TODO/DOING/DONE以外）でPUT
     * 2. レスポンス検証: 400ステータスとerrors配列の内容
     */
    @Test
    @DisplayName("PUT /api/tasks/{id} : status不正で400")
    void updateTask_validationError_statusInvalid() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: String body =
        // """{"summary":"A","description":"説明","status":"INVALID"}""";
        String body = """
                {"summary":"A","description":"説明","status":"INVALID"}
                """;
        // ヒント2: .andExpect(jsonPath("$.errors[0].field", anyOf(is("status"), ...)))
        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.errors[0].field", anyOf(is("status"),
                        is("taskApiUpdateRequest.status"))));
    }

    /**
     * テスト8: タスク削除API
     * 
     * 目的: DELETE /api/tasks/{id} でタスクが削除できるか確認
     * 
     * 検証内容:
     * - HTTPステータス: 204 No Content（削除成功、レスポンスボディなし）
     * 
     * テスト手順:
     * 1. モックの準備: taskService.delete(20L)が正常終了するよう設定
     * 2. リクエスト送信: DELETE /api/tasks/20
     * 3. レスポンス検証: 204ステータスコード
     */
    @Test
    @DisplayName("DELETE /api/tasks/{id} : 204を返す")
    void deleteTask_success() throws Exception {
        // TODO: ここに実装を書く
        // ヒント1: Mockito.doNothing().when(taskService).delete(20L);
        Mockito.doNothing().when(taskService).delete(20L);
        // ヒント2: mockMvc.perform(delete("/api/tasks/20"))
        mockMvc.perform(delete("/api/tasks/20"))
                // ヒント3: .andExpect(status().isNoContent())
                .andExpect(status().isNoContent());
    }
}
