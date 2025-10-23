package com.example.todo.repository.task;

import static org.junit.jupiter.api.Assertions.*;
import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskSearchEntity;
import com.example.todo.service.task.TaskStatus;

/**
 * TaskRepositoryを対象にしたテストクラス
 * 
 * このテストクラスは、TaskRepositoryの各メソッド（検索、挿入、更新、削除）が
 * 正しく動作することを検証します。
 * 
 * @MybatisTest: MyBatisのMapperテスト用アノテーション
 *               - MyBatisの設定とMapperのみをロード（軽量）
 *               - トランザクション管理を自動化（各テスト後にロールバック）
 *               - H2インメモリDBを使用
 * 
 * @AutoConfigureTestDatabase: テストDB設定
 *               - NONE指定により、application.propertiesの設定を使用
 * 
 * テストデータ:
 *   - schema.sql: テーブル定義（自動実行）
 *   - data.sql: 初期データ（自動実行）
 *     1. ID=1, "Spring Boot を学ぶ", DONE
 *     2. ID=2, "Spring Security を学ぶ", TODO
 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("TaskRepositoryを対象にしたテストクラス")
public class TaskRepositoryTest {

    // テスト対象クラス（Spring DIコンテナから自動注入）
    @Autowired
    TaskRepository taskRepository;

    /**
     * 全件検索のテスト
     * 
     * 【テスト目的】
     * 検索条件を指定せずに全件検索を実行し、data.sqlで投入された
     * 初期データが正しく取得できることを検証する。
     * 
     * 【テスト手順】
     * 1. 検索条件なし（summary=null, status=空リスト）の条件オブジェクトを作成
     * 2. select()メソッドを呼び出し、全タスクを取得
     * 3. 取得結果が2件であることを確認
     * 4. 各タスクのデータがdata.sqlの内容と一致することを確認
     * 
     * 【期待される結果】
     * - 2件のタスクが取得される
     * - 1件目: ID=1, "Spring Boot を学ぶ", "TODO アプリを作る", DONE
     * - 2件目: ID=2, "Spring Security を学ぶ", "ログイン機能を作る", TODO
     */
    @Test
    @DisplayName("全件検索の結果をテストする")
    void test_SelectAll() {
        // 検索条件なしの条件オブジェクトを作成
        TaskSearchEntity condition = new TaskSearchEntity(null, emptyList());

        // テストを実行し、実測値リストを取得する
        List<TaskEntity> actualList = taskRepository.select(condition);

        // 実測値リストがnullでないことを検証する
        assertNotNull(actualList, "検索結果がnullであってはならない");
        
        // 実測値リストのサイズが期待値（2件）と一致しているかを検証する
        assertEquals(2, actualList.size(), "初期データは2件のはず");
        
        // 1件目のデータ（data.sqlの初期データ）を検証する
        TaskEntity actual1 = actualList.get(0);
        assertEquals(1L, actual1.id(), "1件目のIDは1のはず");
        assertEquals("Spring Boot を学ぶ", actual1.summary(), "1件目のサマリーが一致すること");
        assertEquals("TODO アプリを作る", actual1.description(), "1件目の説明が一致すること");
        assertEquals(TaskStatus.DONE, actual1.status(), "1件目のステータスがDONEであること");

        // 2件目のデータ（data.sqlの初期データ）を検証する
        TaskEntity actual2 = actualList.get(1);
        assertEquals(2L, actual2.id(), "2件目のIDは2のはず");
        assertEquals("Spring Security を学ぶ", actual2.summary(), "2件目のサマリーが一致すること");
        assertEquals("ログイン機能を作る", actual2.description(), "2件目の説明が一致すること");
        assertEquals(TaskStatus.TODO, actual2.status(), "2件目のステータスがTODOであること");
    }

    /**
     * サマリー検索のテスト（部分一致検索）
     * 
     * 【テスト目的】
     * サマリーに特定の文字列を含むタスクのみが検索されることを検証する。
     * 
     * 【テスト手順】
     * 1. サマリーに"Spring Boot"を指定した検索条件を作成
     * 2. select()メソッドを呼び出す
     * 3. "Spring Boot"を含むタスクのみが取得されることを確認
     * 
     * 【期待される結果】
     * - 1件のタスクが取得される
     * - サマリーが"Spring Boot を学ぶ"のタスクのみ
     * - "Spring Security を学ぶ"は含まれない
     */
    @Test
    @DisplayName("サマリー検索の結果をテストする")
    void test_SelectBySummary() {
        // サマリー"Spring Boot"を含むタスクを検索する条件を作成
        TaskSearchEntity condition = new TaskSearchEntity("Spring Boot", emptyList());

        // テストを実行し、実測値リストを取得する
        List<TaskEntity> actualList = taskRepository.select(condition);

        // 実測値リストがnullでないことを検証する
        assertNotNull(actualList, "検索結果がnullであってはならない");
        
        // 実測値リストのサイズが期待値（1件）と一致しているかを検証する
        assertEquals(1, actualList.size(), "「Spring Boot」を含むタスクは1件のはず");
        
        // 取得したタスクの内容を検証する
        TaskEntity actual = actualList.get(0);
        assertEquals("Spring Boot を学ぶ", actual.summary(), 
                "サマリーが「Spring Boot を学ぶ」と一致すること");
    }

    /**
     * ステータス検索のテスト
     * 
     * 【テスト目的】
     * 指定したステータスのタスクのみが検索されることを検証する。
     * 
     * 【テスト手順】
     * 1. ステータスに"TODO"を指定した検索条件を作成
     * 2. select()メソッドを呼び出す
     * 3. ステータスがTODOのタスクのみが取得されることを確認
     * 
     * 【期待される結果】
     * - 1件のタスクが取得される
     * - "Spring Security を学ぶ"（TODO）のみが取得される
     * - "Spring Boot を学ぶ"（DONE）は含まれない
     */
    @Test
    @DisplayName("ステータス検索の結果をテストする")
    void test_SelectByStatus() {
        // ステータスがTODOのタスクを検索する条件を作成
        TaskSearchEntity condition = new TaskSearchEntity(null, Arrays.asList(TaskStatus.TODO));

        // テストを実行し、実測値リストを取得する
        List<TaskEntity> actualList = taskRepository.select(condition);

        // 実測値リストがnullでないことを検証する
        assertNotNull(actualList, "検索結果がnullであってはならない");
        
        // 実測値リストのサイズが期待値（1件）と一致しているかを検証する
        assertEquals(1, actualList.size(), "ステータスがTODOのタスクは1件のはず");
        
        // 取得したタスクの内容を検証する
        TaskEntity actual = actualList.get(0);
        assertEquals(TaskStatus.TODO, actual.status(), "ステータスがTODOであること");
        assertEquals("Spring Security を学ぶ", actual.summary(), 
                "サマリーが「Spring Security を学ぶ」と一致すること");
    }

    /**
     * 複合検索のテスト（サマリー + ステータス）
     * 
     * 【テスト目的】
     * サマリーとステータスの両方の条件を満たすタスクのみが検索されることを検証する。
     * 
     * 【テスト手順】
     * 1. サマリーに"Spring"、ステータスに"DONE"を指定した検索条件を作成
     * 2. select()メソッドを呼び出す
     * 3. 両方の条件を満たすタスクのみが取得されることを確認
     * 
     * 【期待される結果】
     * - 1件のタスクが取得される
     * - "Spring Boot を学ぶ"（サマリーに"Spring"を含み、ステータスがDONE）のみ
     * - "Spring Security を学ぶ"はステータスがTODOなので含まれない
     */
    @Test
    @DisplayName("複合検索の結果をテストする")
    void test_SelectBySummaryAndStatus() {
        // サマリーに"Spring"を含み、ステータスがDONEのタスクを検索する条件を作成
        TaskSearchEntity condition = new TaskSearchEntity("Spring", Arrays.asList(TaskStatus.DONE));

        // テストを実行し、実測値リストを取得する
        List<TaskEntity> actualList = taskRepository.select(condition);

        // 実測値リストがnullでないことを検証する
        assertNotNull(actualList, "検索結果がnullであってはならない");
        
        // 実測値リストのサイズが期待値（1件）と一致しているかを検証する
        assertEquals(1, actualList.size(), 
                "「Spring」を含み、ステータスがDONEのタスクは1件のはず");
        
        // 取得したタスクの内容を検証する
        TaskEntity actual = actualList.get(0);
        assertEquals("Spring Boot を学ぶ", actual.summary(), 
                "サマリーが「Spring Boot を学ぶ」と一致すること");
        assertEquals(TaskStatus.DONE, actual.status(), "ステータスがDONEであること");
    }

    /**
     * 主キー検索のテスト（正常系）
     * 
     * 【テスト目的】
     * 存在するIDを指定した場合、該当するタスクが正しく取得できることを検証する。
     * 
     * 【テスト手順】
     * 1. 存在するID（1L）を指定してselectById()を呼び出す
     * 2. Optionalにデータが存在することを確認
     * 3. 取得したタスクの各フィールドが期待値と一致することを確認
     * 
     * 【期待される結果】
     * - Optionalにデータが存在する
     * - ID=1のタスク情報が正しく取得される
     */
    @Test
    @DisplayName("主キー検索の結果をテストする（正常系）")
    void test_SelectById() {
        // テストを実行し、実測値を取得する
        Optional<TaskEntity> optionalActual = taskRepository.selectById(1L);

        // Optionalにデータが存在することを検証する
        assertTrue(optionalActual.isPresent(), "ID=1のタスクが存在するはず");
        
        // Optionalからタスクを取り出す
        TaskEntity actual = optionalActual.get();
        
        // 期待値を生成する
        TaskEntity expected = new TaskEntity(
                1L, 
                "Spring Boot を学ぶ", 
                "TODO アプリを作る", 
                TaskStatus.DONE
        );
        
        // 期待値と実測値が一致しているかを検証する
        assertEquals(expected.id(), actual.id(), "IDが一致すること");
        assertEquals(expected.summary(), actual.summary(), "サマリーが一致すること");
        assertEquals(expected.description(), actual.description(), "説明が一致すること");
        assertEquals(expected.status(), actual.status(), "ステータスが一致すること");
    }

    /**
     * 主キー検索のテスト（異常系：存在しないID）
     * 
     * 【テスト目的】
     * 存在しないIDを指定した場合、空のOptionalが返されることを検証する。
     * 
     * 【テスト手順】
     * 1. 存在しないID（999L）を指定してselectById()を呼び出す
     * 2. Optionalが空であることを確認
     * 
     * 【期待される結果】
     * - 空のOptionalが返される
     * - 例外は発生しない
     */
    @Test
    @DisplayName("主キー検索の結果をテストする（存在しないID）")
    void test_SelectById_NotFound() {
        // 存在しないIDでテストを実行し、実測値を取得する
        Optional<TaskEntity> optionalActual = taskRepository.selectById(999L);

        // Optionalが空であることを検証する
        assertFalse(optionalActual.isPresent(), 
                "存在しないID=999のタスクは取得できないはず");
    }

    /**
     * 挿入のテスト
     * 
     * 【テスト目的】
     * 新しいタスクをデータベースに挿入できることを検証する。
     * 
     * 【テスト手順】
     * 1. 挿入前のタスク総数を取得（期待値: 2件）
     * 2. 新しいタスクを作成してinsert()を呼び出す
     * 3. 挿入後のタスク総数が1件増えていることを確認
     * 4. 挿入したタスクが正しく検索できることを確認
     * 5. 挿入したタスクの各フィールドが期待値と一致することを確認
     * 
     * 【期待される結果】
     * - タスク総数が2件から3件に増える
     * - 挿入したタスクのデータが正しく保存される
     * - IDは自動採番される（AUTO_INCREMENT）
     */
    @Test
    @DisplayName("挿入の結果をテストする")
    void test_Insert() {
        // 挿入前のタスク総数を取得する
        List<TaskEntity> beforeList = taskRepository.select(
                new TaskSearchEntity(null, emptyList()));
        int beforeCount = beforeList.size();

        // 挿入するタスクを作成する（IDはnull = 自動採番）
        TaskEntity newTask = new TaskEntity(
                null, 
                "新しいタスク", 
                "テスト用タスク", 
                TaskStatus.TODO
        );

        // テストを実行する（タスクを挿入）
        taskRepository.insert(newTask);

        // 挿入後のタスク総数を取得する
        List<TaskEntity> afterList = taskRepository.select(
                new TaskSearchEntity(null, emptyList()));
        
        // タスク総数が1件増えていることを検証する
        assertEquals(beforeCount + 1, afterList.size(), 
                "挿入後のタスク数が1件増えているはず");

        // 挿入したタスクを検索する
        TaskSearchEntity condition = new TaskSearchEntity("新しいタスク", emptyList());
        List<TaskEntity> actualList = taskRepository.select(condition);
        
        // 挿入したタスクが1件取得できることを検証する
        assertEquals(1, actualList.size(), "挿入したタスクが検索できるはず");
        
        // 挿入したタスクの内容を検証する
        TaskEntity actual = actualList.get(0);
        assertNotNull(actual.id(), "IDが自動採番されているはず");
        assertEquals("新しいタスク", actual.summary(), "サマリーが一致すること");
        assertEquals("テスト用タスク", actual.description(), "説明が一致すること");
        assertEquals(TaskStatus.TODO, actual.status(), "ステータスが一致すること");
    }

    /**
     * 更新のテスト
     * 
     * 【テスト目的】
     * 既存のタスクの内容を更新できることを検証する。
     * 
     * 【テスト手順】
     * 1. 更新対象のタスク（ID=1）が存在することを確認
     * 2. 更新後のデータを持つTaskEntityを作成
     * 3. update()メソッドを呼び出す
     * 4. 同じIDのタスクを再取得し、データが更新されていることを確認
     * 
     * 【期待される結果】
     * - ID=1のタスクが更新される
     * - サマリー、説明、ステータスが全て更新される
     * - IDは変わらない
     * 
     * 【更新前】
     * ID=1, "Spring Boot を学ぶ", "TODO アプリを作る", DONE
     * 
     * 【更新後】
     * ID=1, "更新されたタスク", "更新されたディスクリプション", DOING
     */
    @Test
    @DisplayName("更新の結果をテストする")
    void test_Update() {
        // 更新対象のタスクが存在することを確認する
        Optional<TaskEntity> beforeUpdate = taskRepository.selectById(1L);
        assertTrue(beforeUpdate.isPresent(), "更新前にID=1のタスクが存在するはず");

        // 更新後のタスクデータを作成する
        TaskEntity updatedTask = new TaskEntity(
                1L,
                "更新されたタスク",
                "更新されたディスクリプション",
                TaskStatus.DOING
        );

        // テストを実行する（タスクを更新）
        taskRepository.update(updatedTask);

        // 更新後のタスクを取得する
        Optional<TaskEntity> afterUpdate = taskRepository.selectById(1L);
        assertTrue(afterUpdate.isPresent(), "更新後もID=1のタスクが存在するはず");
        
        // 実測値を取得する
        TaskEntity actual = afterUpdate.get();
        
        // 期待値を生成する
        TaskEntity expected = new TaskEntity(
                1L,
                "更新されたタスク",
                "更新されたディスクリプション",
                TaskStatus.DOING
        );
        
        // 期待値と実測値が一致しているかを検証する
        assertEquals(expected.id(), actual.id(), "IDが変わらないこと");
        assertEquals(expected.summary(), actual.summary(), 
                "サマリーが「更新されたタスク」に更新されていること");
        assertEquals(expected.description(), actual.description(), 
                "説明が「更新されたディスクリプション」に更新されていること");
        assertEquals(expected.status(), actual.status(), 
                "ステータスがDOINGに更新されていること");
    }

    /**
     * 削除のテスト
     * 
     * 【テスト目的】
     * 指定したIDのタスクをデータベースから削除できることを検証する。
     * 
     * 【テスト手順】
     * 1. 削除対象のタスク（ID=1）が存在することを確認
     * 2. 削除前のタスク総数を取得（期待値: 2件）
     * 3. delete()メソッドを呼び出す
     * 4. 削除したIDのタスクが取得できないことを確認
     * 5. タスク総数が1件減っていることを確認
     * 
     * 【期待される結果】
     * - ID=1のタスクが削除される
     * - 削除したタスクは検索できない
     * - タスク総数が2件から1件に減る
     * - ID=2のタスクは残っている
     */
    @Test
    @DisplayName("削除の結果をテストする")
    void test_Delete() {
        // 削除対象のタスクが存在することを確認する
        Optional<TaskEntity> beforeDelete = taskRepository.selectById(1L);
        assertTrue(beforeDelete.isPresent(), "削除前にID=1のタスクが存在するはず");

        // 削除前のタスク総数を取得する
        List<TaskEntity> beforeList = taskRepository.select(
                new TaskSearchEntity(null, emptyList()));
        int beforeCount = beforeList.size();

        // テストを実行する（タスクを削除）
        taskRepository.delete(1L);

        // 削除後、同じIDのタスクが取得できないことを検証する
        Optional<TaskEntity> afterDelete = taskRepository.selectById(1L);
        assertFalse(afterDelete.isPresent(), 
                "削除後にID=1のタスクは存在しないはず");

        // 削除後のタスク総数を取得する
        List<TaskEntity> afterList = taskRepository.select(
                new TaskSearchEntity(null, emptyList()));
        
        // タスク総数が1件減っていることを検証する
        assertEquals(beforeCount - 1, afterList.size(), 
                "削除後のタスク数が1件減っているはず");
        assertEquals(1, afterList.size(), "残りのタスク数は1件のはず");
        
        // 残っているタスクがID=2のタスクであることを確認する
        TaskEntity remainingTask = afterList.get(0);
        assertEquals(2L, remainingTask.id(), "残っているタスクはID=2のはず");
    }

    /**
     * 複数ステータス検索のテスト
     * 
     * 【テスト目的】
     * 複数のステータスを指定した場合、いずれかのステータスに該当するタスクが
     * 全て検索されることを検証する（OR条件）。
     * 
     * 【テスト手順】
     * 1. ステータスに"TODO"と"DONE"の両方を指定した検索条件を作成
     * 2. select()メソッドを呼び出す
     * 3. 両方のステータスのタスクが全て取得されることを確認
     * 
     * 【期待される結果】
     * - 2件のタスクが取得される
     * - "Spring Boot を学ぶ"（DONE）が含まれる
     * - "Spring Security を学ぶ"（TODO）が含まれる
     * - IN句で検索される: WHERE status IN ('TODO', 'DONE')
     */
    @Test
    @DisplayName("複数ステータス検索の結果をテストする")
    void test_SelectByMultipleStatuses() {
        // ステータスがTODOまたはDONEのタスクを検索する条件を作成
        TaskSearchEntity condition = new TaskSearchEntity(
                null,
                Arrays.asList(TaskStatus.TODO, TaskStatus.DONE)
        );

        // テストを実行し、実測値リストを取得する
        List<TaskEntity> actualList = taskRepository.select(condition);

        // 実測値リストがnullでないことを検証する
        assertNotNull(actualList, "検索結果がnullであってはならない");
        
        // 実測値リストのサイズが期待値（2件）と一致しているかを検証する
        assertEquals(2, actualList.size(), 
                "ステータスがTODOまたはDONEのタスクは2件のはず");
        
        // 取得したタスクのステータスが期待値（TODOまたはDONE）であることを検証する
        for (TaskEntity task : actualList) {
            assertTrue(
                    task.status() == TaskStatus.TODO || task.status() == TaskStatus.DONE,
                    "取得したタスクのステータスはTODOまたはDONEであること"
            );
        }
    }
}
