package com.example.todo.selenium.task;

import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.codeborne.selenide.Configuration;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class TaskE2ETest {

    // テストクラス全体の前処理
    @BeforeAll
    static void setupClass() {
        // ChromeDriverのパスを環境変数またはシステムプロパティから取得
        // 環境変数 CHROME_DRIVER_PATH が設定されている場合はそれを使用
        String chromeDriverPath = System.getenv("CHROME_DRIVER_PATH");
        if (chromeDriverPath == null || chromeDriverPath.isEmpty()) {
            // システムプロパティから取得を試みる
            chromeDriverPath = System.getProperty("webdriver.chrome.driver");
        }

        // ChromeDriverのパスが指定されている場合は設定
        if (chromeDriverPath != null && !chromeDriverPath.isEmpty()) {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        }

        // WebブラウザをGoogle Chromeに設定する
        Configuration.browser = "chrome";
        // ベースURLを設定する
        Configuration.baseUrl = "http://localhost:8080";
        // タイムアウトを設定する（ミリ秒）
        Configuration.timeout = 10000;

        // 現在日付と時刻をyyyyMMddHHmmss形式で取得する
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String dateTime = now.format(formatter);

        // スクリーンショットの保存先ディレクトリを設定する
        Configuration.reportsFolder = "build/reports/tests/e2e/" + dateTime;
    }

    @Test
    void testIndexPageTitle() {
        // アプリケーションのトップページを開く
        open("");

        // ページタイトルの検証（document.titleで取得）
        String pageTitle = executeJavaScript("return document.title;");
        System.out.println("Page title: " + pageTitle);
        screenshot("1-TopPage");

        // ページ内のh1要素が"タスク管理のお手伝いをします"であることを確認する
        $("h1").shouldHave(text("タスク管理のお手伝いをします"));
        screenshot("2-TopPage-H1");

        // ナビゲーションバーが表示されていることを確認する
        $("nav.navbar").shouldBe(visible);
        screenshot("3-TopPage-Navbar");
    }

    @Test
    void testTaskListView() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // ページタイトルが"タスク一覧"であることを確認する
        String pageTitle = executeJavaScript("return document.title;");
        pageTitle.contains("タスク一覧");
        screenshot("1-TaskList-Title");

        // テーブルが表示されていることを確認する
        $("table tbody").shouldBe(visible);

        // テーブル内1行目の1列目(ID)の内容が"#1"であることを確認する
        $("table tbody tr:nth-child(1) td:nth-child(1)").shouldHave(text("#1"));

        // テーブル内1行目の2列目(概要)の内容に"Spring Boot"が含まれることを確認する
        $("table tbody tr:nth-child(1) td:nth-child(2)").shouldHave(text("Spring Boot"));
        screenshot("2-TaskList-Table");
    }

    @Test
    void testTaskCreationAndDetailView() {
        // タスク作成画面に遷移する
        open("/tasks/creationForm");

        // ページにフォームが表示されることを確認する
        $("form").shouldBe(visible);
        screenshot("1-TaskCreation-Form");

        // フォームに入力する
        $("#summaryInput").setValue("E2Eテストで作成したタスク");
        $("#descriptionInput").setValue("これはSelenideで作成されたタスクです");
        $("#statusInput").selectOptionByValue("TODO");
        screenshot("2-TaskCreation-Input");

        // 作成ボタンをクリック
        $("button[type='submit']").click();

        // タスク一覧にリダイレクトされることを確認（URLで確認）
        webdriver().shouldHave(url("http://localhost:8080/tasks"));
        screenshot("3-TaskCreation-Redirect");

        // 作成したタスクがテーブルに表示されることを確認
        $("table tbody").shouldHave(text("E2Eテストで作成したタスク"));
        screenshot("4-TaskCreation-Listed");
    }

    @Test
    void testTaskUpdate() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // 1つ目のタスクの詳細リンクをクリック
        $("table tbody tr:nth-child(1) td:nth-child(1) a").click();

        // 詳細画面で編集ボタンをクリック
        $("a.btn.btn-primary").click();

        // 編集画面が表示されることを確認（URLで確認）
        webdriver().shouldHave(urlContaining("/editForm"));
        screenshot("1-TaskEdit-Form");

        // フォームの内容を更新する
        $("#summaryInput").setValue("更新後のタスク概要");
        $("#descriptionInput").setValue("更新後のタスク説明");
        $("#statusInput").selectOptionByValue("DOING");
        screenshot("2-TaskEdit-Input");

        // 更新ボタンをクリック
        $("button[type='submit']").click();

        // タスク詳細画面にリダイレクトされることを確認（URLで確認）
        webdriver().shouldHave(urlContaining("/tasks/"));
        screenshot("3-TaskEdit-Redirect");

        // 更新後の内容が表示されていることを確認
        $("body").shouldHave(text("更新後のタスク概要"));
        $("body").shouldHave(text("更新後のタスク説明"));
        $("body").shouldHave(text("DOING"));
        screenshot("4-TaskEdit-Updated");
    }

    @Test
    void testTaskDeletion() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // 1つ目のタスクの詳細リンクをクリック
        $("table tbody tr:nth-child(1) td:nth-child(1) a").click();

        // 詳細画面で編集ボタンをクリック
        $("button.btn.btn-danger").click();

        // 確認ダイアログでOKをクリック
        switchTo().alert().accept();

        // タスク一覧画面にリダイレクトされることを確認（URLで確認）
        webdriver().shouldHave(url("http://localhost:8080/tasks"));
        screenshot("1-TaskDeletion-Redirect");

        // 削除したタスクがテーブルに表示されていないことを確認
        $("table tbody").shouldNotHave(text("#1"));
        screenshot("2-TaskDeletion-Confirmed");
    }

    @Test
    void testTaskDetailView() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // 1つ目のタスクのリンクをクリック
        $("table tbody tr:nth-child(1) td:nth-child(1) a").click();

        // 詳細画面が表示されることを確認（URLで確認）
        webdriver().shouldHave(urlContaining("/tasks/"));
        screenshot("1-TaskDetail");

        // #1 Spring Boot を学ぶが表示されていることを確認
        $("body").shouldHave(text("#1 Spring Boot を学ぶ"));
        screenshot("2-TaskDetail-Content");
    }

    @Test
    void testTaskSearchByKeyword() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // 検索フォームに入力
        $("#summarySearch").setValue("Spring");
        screenshot("1-TaskSearch-Input");

        // 検索ボタンをクリック
        $("button[type='submit']").click();

        // 検索結果が表示されることを確認
        $("table tbody").shouldHave(text("Spring"));
        screenshot("2-TaskSearch-Result");
    }

    @Test
    void testTaskSearchByStatus() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // TODOのチェックボックスのラベルをクリック
        $("label[for='statusSearchTodo']").click();
        screenshot("1-TaskSearchByStatus-Input");

        // 検索ボタンをクリック
        $("button[type='submit']").click();

        // 検索結果が表示されることを確認
        $("table tbody").shouldHave(text("TODO"));
        screenshot("2-TaskSearchByStatus-Result");
    }

    @Test
    void testTaskSearchByStatusDoing() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // DOINGのチェックボックスのラベルをクリック
        $("label[for='statusSearchDoing']").click();
        screenshot("1-TaskSearchByStatusDoing-Input");

        // 検索ボタンをクリック
        $("button[type='submit']").click();

        // DOINGステータスのタスクは存在しないため、テーブルの行数が0であることを確認
        $$("table tbody tr").shouldHave(size(0));
        screenshot("2-TaskSearchByStatusDoing-NoResult");
    }

    @Test
    void testTaskSearchByStatusDone() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // DONEのチェックボックスのラベルをクリック
        $("label[for='statusSearchDone']").click();
        screenshot("1-TaskSearchByStatusDone-Input");

        // 検索ボタンをクリック
        $("button[type='submit']").click();

        // 検索結果が表示されることを確認
        $("table tbody").shouldHave(text("DONE"));
        screenshot("2-TaskSearchByStatusDone-Result");
    }

    @Test
    void testTaskSearchByMultipleStatuses() {
        // タスク一覧画面に遷移する
        open("/tasks");

        // TODOとDONEのチェックボックスのラベルをクリック
        $("label[for='statusSearchTodo']").click();
        $("label[for='statusSearchDone']").click();
        screenshot("1-TaskSearchByMultipleStatuses-Input");

        // 検索ボタンをクリック
        $("button[type='submit']").click();

        // TODOとDONEステータスのタスクが表示されることを確認
        $("table tbody").shouldHave(text("TODO"));
        $("table tbody").shouldHave(text("DONE"));
        screenshot("2-TaskSearchByMultipleStatuses-Result");
    }

    @Test
    void testClearSearchAndShowAllTasks() {
        open("/tasks");

        // 最初に検索を実行
        $("#summarySearch").setValue("Spring");
        $("button[type='submit']").click();
        $$("table tbody tr").shouldHave(size(2));

        // 検索条件をクリアして再検索
        $("#summarySearch").clear();
        $("button[type='submit']").click();

        // 全タスクが表示されることを確認
        $$("table tbody tr").shouldHave(size(2));
    }

    @Test
    void testCombinedKeywordAndStatusSearch() {
        open("/tasks");

        // キーワードとステータスの両方で検索
        $("#summarySearch").setValue("Spring");
        $("label[for='statusSearchTodo']").click();
        $("button[type='submit']").click();

        // 条件に一致するタスクのみ表示
        $("table tbody").shouldHave(text("Spring Security"));
        $("table tbody").shouldHave(text("TODO"));
        $$("table tbody tr").shouldHave(size(1));
    }

    @Test
    void testSearchWithNonExistentKeyword() {
        open("/tasks");

        $("#summarySearch").setValue("存在しないキーワード");
        $("button[type='submit']").click();

        // 結果が0件であることを確認
        $$("table tbody tr").shouldHave(size(0));
    }

    @Test
    void backToTaskList() {
        // タスク一覧画面に遷移する
        open("/tasks/1");

        // トップページへのリンクをクリック
        $("a.btn-secondary").click();

        // トップページが表示されることを確認（URLで確認）
        webdriver().shouldHave(url("http://localhost:8080/tasks"));
        screenshot("1-BackToTaskList");
    }



    @Test
void testTaskCreationWithEmptySummary() {
    // タスク作成画面に遷移
    open("/tasks/creationForm");
    
    // 概要を空のまま送信
    $("#summaryInput").clear();
    $("#descriptionInput").setValue("説明のみ入力");
    $("#statusInput").selectOptionByValue("TODO");
    $("button[type='submit']").click();
    
    // バリデーションエラーが表示されることを確認
    $("form").shouldBe(visible);
    $("#summaryInput").shouldHave(cssClass("is-invalid"));
    $(".invalid-feedback").shouldBe(visible);
    screenshot("ValidationError-EmptySummary");
}

@Test
void testTaskCreationWithTooLongSummary() {
    open("/tasks/creationForm");
    
    // 256文字を超える概要を入力
    String longSummary = "あ".repeat(257);
    $("#summaryInput").setValue(longSummary);
    $("#descriptionInput").setValue("説明");
    $("#statusInput").selectOptionByValue("TODO");
    $("button[type='submit']").click();
    
    // バリデーションエラーが表示されることを確認
    $("form").shouldBe(visible);
    $("body").shouldHave(text("256文字以内で入力してください"));
    screenshot("ValidationError-TooLongSummary");
}

@Test
void testTaskUpdateWithValidationError() {
    open("/tasks");
    
    // 既存タスクの編集画面へ
    $("table tbody tr:nth-child(1) td:nth-child(1) a").click();
    $("a.btn.btn-primary").click();
    
    // 概要を空にして送信
    $("#summaryInput").clear();
    $("button[type='submit']").click();
    
    // エラーメッセージが表示され、フォームが再表示されることを確認
    $("form").shouldBe(visible);
    // バリデーションエラー時はURLは /tasks/1 のまま（フォワード処理）
    $(".invalid-feedback").shouldBe(visible);
    $("#summaryInput").shouldHave(cssClass("is-invalid"));
    screenshot("UpdateValidationError");
}
}
