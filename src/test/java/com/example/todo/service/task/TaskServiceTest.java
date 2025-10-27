package com.example.todo.service.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.todo.repository.task.TaskRepository;


/*
 * TaskServiceの単体テスト
 */
@SpringBootTest
public class TaskServiceTest {

    // テスト対象クラス（インジェクション）
    @Autowired
    private TaskService taskService;

    // テスト対象クラスの呼び出し先（モック化対象）
    @MockBean
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        // モックの初期化
        reset(taskRepository);

        // モック化されたTaskRepositoryの振る舞いを設定する
        TaskEntity task1 = new TaskEntity(1L, "タスク1", "説明1", TaskStatus.TODO);
        TaskEntity task2 = new TaskEntity(2L, "タスク2", "説明2", TaskStatus.DOING);
        TaskEntity task3 = new TaskEntity(3L, "タスク3", "説明3", TaskStatus.DONE);
        List<TaskEntity> allTasks = Arrays.asList(task1, task2, task3);

        // select(検索条件): 条件に応じたタスクのリストを返す
        when(taskRepository.select(any(TaskSearchEntity.class))).thenAnswer(invocation -> {
            TaskSearchEntity condition = invocation.getArgument(0);
            return allTasks.stream()
                    .filter(task -> {
                        // summaryによるフィルタリング
                        if (condition.summary() != null && !condition.summary().isEmpty()) {
                            if (!task.summary().contains(condition.summary())) {
                                return false;
                            }
                        }
                        // statusによるフィルタリング
                        if (condition.status() != null && !condition.status().isEmpty()) {
                            if (!condition.status().contains(task.status())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        });

        // selectById(ID検索): 指定されたIDのタスクを返す
        when(taskRepository.selectById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return allTasks.stream()
                    .filter(task -> task.id().equals(id))
                    .findFirst();
        });

        // insert(追加): 何もしない(voidメソッド)
        doNothing().when(taskRepository).insert(any(TaskEntity.class));

        // update(更新): 何もしない(voidメソッド)
        doNothing().when(taskRepository).update(any(TaskEntity.class));

        // delete(削除): 何もしない(voidメソッド)
        doNothing().when(taskRepository).delete(anyLong());
    }



    @Test    
    @DisplayName("検索条件なし: すべてのタスクが取得できること")
    void testSearch() {
        // 検索条件なしでタスクを取得
        List<TaskEntity> actual = taskService.find(new TaskSearchEntity(null, null));

        List<TaskEntity> expectedTasks = Arrays.asList(
            new TaskEntity(1L, "タスク1", "説明1", TaskStatus.TODO),
            new TaskEntity(2L, "タスク2", "説明2", TaskStatus.DOING),
            new TaskEntity(3L, "タスク3", "説明3", TaskStatus.DONE)
        );

        // すべてのタスクが取得できることを確認
        assertEquals(expectedTasks, actual);
    }



    @Test    
    @DisplayName("検索条件あり: 条件に合致するタスクのみ取得できること")
    void testSearchById() {
        // IDが2のタスクを取得
        long searchId = 2L;
        TaskEntity actual = taskService.findById(searchId).orElse(null);

        TaskEntity expectedTask = new TaskEntity(2L, "タスク2", "説明2", TaskStatus.DOING);

        // 条件に合致するタスクのみ取得できることを確認
        assertEquals(expectedTask, actual);
    }

    @Test
    @DisplayName("新規タスク作成: タスクが正常に作成できること")
    void testCreateTask() {
        TaskEntity newTask = new TaskEntity(null, "新規タスク", "新規説明", TaskStatus.TODO);
        taskService.create(newTask);
        
        // insertメソッドが正しく呼び出されたことを検証
        verify(taskRepository).insert(argThat(task -> {
            // IDはnullのまま（DBで自動生成されるため）
            assertNull(task.id());
            assertEquals("新規タスク", task.summary());
            assertEquals("新規説明", task.description());
            assertEquals(TaskStatus.TODO, task.status());
            return true;
        }));
    }

    @Test
    @DisplayName("タスク更新: タスクが正常に更新できること")
    void testUpdateTask() {
        TaskEntity updatedTask = new TaskEntity(1L, "更新タスク", "更新説明", TaskStatus.DOING);
        taskService.update(updatedTask);   

        // updateメソッドが正しく呼び出されたことを検証
        verify(taskRepository).update(argThat(task -> {
            assertEquals(1L, task.id());
            assertEquals("更新タスク", task.summary());
            assertEquals("更新説明", task.description());
            assertEquals(TaskStatus.DOING, task.status());
            return true;
        }));
    }

    @Test
    @DisplayName("タスク削除: タスクが正常に削除できるること")
    void testDeleteTask() {
        long deleteId = 3L;
        taskService.delete(deleteId);

        // deleteメソッドが正しく呼び出されたことを検証
        verify(taskRepository).delete(eq(deleteId));
    }


}
