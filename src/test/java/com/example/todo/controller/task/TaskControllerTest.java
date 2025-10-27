package com.example.todo.controller.task;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskService;
import com.example.todo.service.task.TaskStatus;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {
    // MockMVCをインジェクションする
    @Autowired
    private MockMvc mockMvc;

    // テスト対象クラスの呼び出し先（モック化対象）
    @MockBean
    private TaskService taskService;

    @Test
    @DisplayName("list画面への遷移")
    void testListRedirect() throws Exception {
        // モック化されたTaskServiceの振る舞いを設定する
        TaskEntity task1 = new TaskEntity(1L, "タスク1", "説明1", TaskStatus.TODO);
        TaskEntity task2 = new TaskEntity(2L, "タスク2", "説明2", TaskStatus.DOING);
        List<TaskEntity> tasks = Arrays.asList(task1, task2);

        when(taskService.find(any())).thenReturn(tasks);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list")) // "tasks/index" → "tasks/list"に修正
                .andExpect(model().attributeExists("taskList")) // "tasks" → "taskList"に修正
                .andExpect(model().attributeExists("searchDTO")); // searchDTOの存在も確認
    }

    @Test
    @DisplayName("detail画面への遷移")
    void testShowDetailRedirect() throws Exception {
        TaskEntity task = new TaskEntity(1L, "タスク1", "説明1", TaskStatus.TODO);
        when(taskService.findById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/detail"))
                .andExpect(model().attributeExists("task")); // "taskDTO" → "task"に修正
    }

    @Test
    @DisplayName("creationForm画面への遷移")
    void testShowCreationFormRedirect() throws Exception {
        mockMvc.perform(get("/tasks/creationForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("mode")); // "mode"属性の存在も確認

    }

    @Test
    @DisplayName("新規タスクの作成")
    void testCreateTask() throws Exception {
        // doNothing()でcreateメソッドをモック化（voidメソッド）
        doNothing().when(taskService).create(any(TaskEntity.class));

        mockMvc.perform(post("/tasks")
                .param("summary", "新しいタスク")
                .param("description", "タスクの説明")
                .param("status", "TODO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        // taskService.create()が1回呼ばれたことを検証
        verify(taskService, times(1)).create(any(TaskEntity.class));

        // 2. リダイレクト先（一覧画面）の表示を確認
        TaskEntity createdTask = new TaskEntity(1L, "新しいタスク", "タスクの説明", TaskStatus.TODO);

        when(taskService.find(any())).thenReturn(Arrays.asList(createdTask));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("taskList"));
    }

    @Test
    @DisplayName("creationForm画面でのバリデーションエラー")
    void testCreateTaskValidationError() throws Exception {
        mockMvc.perform(post("/tasks")
                .param("summary", "") // 空のsummaryでバリデーションエラーを発生させる
                .param("description", "タスクの説明")
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("mode")); // "mode"属性が存在を確認

    }

    @Test
    @DisplayName("editForm画面への遷移")
    void testShowEditFormRedirect() throws Exception {
        TaskEntity task = new TaskEntity(1L, "タスク1", "説明1", TaskStatus.TODO);
        when(taskService.findById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/tasks/1/editForm")) // "/edit" → "/editForm"に修正
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("mode"))
                .andExpect(model().attributeExists("taskForm"));
    }

    @Test
    @DisplayName("タスク更新の実行")
    void testUpdateTask() throws Exception {
        doNothing().when(taskService).update(any(TaskEntity.class));

        mockMvc.perform(put("/tasks/1")
                .param("summary", "更新されたタスク")
                .param("description", "更新された説明")
                .param("status", "DONE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/1"));

        verify(taskService, times(1)).update(any(TaskEntity.class));

        // 2. リダイレクト先(詳細画面)の表示を確認
        TaskEntity updatedTask = new TaskEntity(1L, "更新されたタスク", "更新された説明", TaskStatus.DONE);
        when(taskService.findById(1L)).thenReturn(Optional.of(updatedTask));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/detail"))
                .andExpect(model().attributeExists("task"));
    }

    @Test
    @DisplayName("タスク更新時のバリデーションエラー")
    void testUpdateTaskValidationError() throws Exception {
        mockMvc.perform(put("/tasks/1")
                .param("summary", "") // 空のsummaryでバリデーションエラーを発生させる
                .param("description", "更新された説明")
                .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/form"))
                .andExpect(model().attributeExists("mode")); // バリデーションエラー時にmodeが設定される
    }

    @Test
    @DisplayName("タスク削除の実行")
    void testDeleteTask() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
        verify(taskService, times(1)).delete(1L);

        // 2. リダイレクト先（一覧画面）の表示を確認
        when(taskService.find(any())).thenReturn(List.of());
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("taskList"));
    }
}