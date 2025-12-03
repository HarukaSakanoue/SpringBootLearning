# 学習記録: バリデーション実装とテスト（Controller + E2E）

**学習日**: 2025年12月1日  
**ブランチ**: feature-Vue  
**テーマ**: TaskForm.vueのバリデーション問題解決 → Controllerテスト実装 → E2Eテスト構築

---

## 📚 学習内容サマリー

### 本日の学習の流れ
1. **バリデーションエラーの原因分析と解決** - GlobalExceptionHandler実装
2. **Controllerテストの学習的実装** - 8つのテストを段階的に作成
3. **Request DTOパターンの理解** - セキュリティとアーキテクチャの学習
4. **HTML5バリデーションの追加** - フロントエンド側の防御層
5. **E2Eテストの構築** - Playwrightによる全体テスト

---

## 🔍 Phase 1: バリデーション問題の解決

### 問題
TaskForm.vueでバリデーションエラーが表示されない

### 原因分析
Spring Bootの`MethodArgumentNotValidException`が返すエラー構造:
```json
{
  "errors": [
    { "field": "summary", "defaultMessage": "概要は必須です" }
  ]
}
```

フロントエンドが期待していた構造と異なっていた。

### 解決策: GlobalExceptionHandlerの実装
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
        // エラーを統一形式で返す
        List<FieldError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new FieldError(
                error.getField(), 
                error.getDefaultMessage()))
            .toList();
        return ResponseEntity
            .badRequest()
            .body(new ValidationErrorResponse(errors));
    }
}
```

**学んだこと:**
- `@RestControllerAdvice`でグローバルな例外ハンドリング
- エラーレスポンスの統一形式の重要性
- フロントエンドとバックエンドの契約（API仕様）

### TaskForm.vueでのバリデーション実装位置について

**問題提起:**
空送信などの簡単なバリデーションエラーについてはフロントエンドで処理をすべきでは？

**結論:**
フロントエンドでの実装も非常に重要。ただし、**バリデーションの多層実装**が本質的な学習目的。

#### 多層防御（Defense in Depth）の実装

**第一層: HTML5バリデーション（ブラウザレベル）**
```html
<input type="text" v-model="task.summary" required maxlength="256" />
<select v-model="task.status" required>
```
- **役割**: UX向上、即座のフィードバック
- **利点**: サーバーリクエスト前にユーザーに通知、通信コスト削減
- **欠点**: DevToolsで簡単に無効化可能（`removeAttribute('required')`）
- **実装タイミング**: Phase 4で追加

**第二層: JavaScriptバリデーション（アプリケーションレベル）**
```javascript
// 今回は未実装だが、将来的に追加可能
const validateForm = () => {
  if (!task.value.summary || task.value.summary.length > 256) {
    errors.value.push('概要は1〜256文字で入力してください');
    return false;
  }
  // 複雑なビジネスルール（例: 期限が過去日付でないか）
  return true;
};
```
- **役割**: 複雑なルール、リアルタイムバリデーション、カスタムメッセージ
- **利点**: HTML5より柔軟、ユーザー体験の向上
- **欠点**: クライアント側なので改ざん可能
- **実装タイミング**: 今回は省略（HTML5で十分なため）

**第三層: サーバーサイドバリデーション（最終防御線）**
```java
@NotBlank(message = "概要は必須です")
@Size(max = 256, message = "概要は256文字以内で入力してください")
@Pattern(regexp = "TODO|DOING|DONE", message = "ステータスが不正です")
```
- **役割**: セキュリティ、データ整合性の保証
- **利点**: 改ざん不可能、必ず実行される
- **欠点**: サーバーラウンドトリップが必要（レスポンスが遅い）
- **実装タイミング**: Phase 1で実装（最優先）

#### なぜこの順序で学習したか

```
Phase 1: サーバー側実装
    ↓ セキュリティの基礎を固める
Phase 2-3: エラーハンドリングとAPI設計
    ↓ フロント・バックの連携を理解
Phase 4: HTML5追加
    ↓ UX向上のレイヤーを追加
Phase 5: E2Eテストで全層を検証
```

**教育的意義:**
1. **セキュリティファースト**: クライアント側は信用できないことを学ぶ
2. **段階的な防御**: 各層の役割と限界を理解
3. **実践的な設計**: UXとセキュリティの両立

#### 実際の動作フロー

**正常なユーザーの場合:**
```
ユーザーが概要を空で送信
  ↓
HTML5: 「このフィールドを入力してください」← ここで止まる（快適！）
  ↓（ブラウザが送信を拒否）
サーバーには到達しない（通信コスト削減）
```

**悪意のあるユーザー（DevToolsで改ざん）の場合:**
```
HTML5のrequired属性を削除
  ↓
空のデータをPOST送信
  ↓
サーバー: @NotBlankバリデーション発動
  ↓
400 Bad Request + エラーメッセージ ← 確実にブロック！
```

**結論:**
- HTML5だけ → セキュリティ不十分
- サーバーだけ → UX不十分、無駄な通信
- **両方実装 → セキュリティとUXの両立** ✅

---

## 🧪 Phase 2: Controllerテストの段階的実装

### 学習方法
TODOとヒントだけ書かれたテストファイルを受け取り、1つずつ実装していく学習的アプローチ

### 実装した8つのテスト

#### Test 1: getAllTasks - タスク一覧取得
```java
@Test
@DisplayName("GET /api/tasks - タスク一覧を取得できる")
void getAllTasks() throws Exception {
    List<TaskEntity> mockTasks = Arrays.asList(
        createMockTask(1L, "Spring学習", "TODO"),
        createMockTask(2L, "Vue学習", "DOING")
    );
    when(taskService.findAllTasks(any(), any())).thenReturn(mockTasks);
    
    mockMvc.perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
}
```

**学んだこと:**
- `MockMvc`でHTTPリクエストをシミュレート
- `@WebMvcTest`でController層だけをテスト
- `@MockBean`でServiceをモック化

#### Test 2: getTask_found - 存在するタスクを取得
```java
@Test
@DisplayName("GET /api/tasks/{id} - 存在するタスクを取得できる")
void getTask_found() throws Exception {
    TaskEntity mockTask = createMockTask(1L, "テストタスク", "TODO");
    when(taskService.getTaskById(1L)).thenReturn(Optional.of(mockTask));
    
    mockMvc.perform(get("/api/tasks/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.summary").value("テストタスク"));
}
```

#### Test 3: getTask_notFound - 存在しないタスクで404
```java
@Test
@DisplayName("GET /api/tasks/{id} - 存在しないIDで404を返す")
void getTask_notFound() throws Exception {
    when(taskService.getTaskById(999L)).thenReturn(Optional.empty());
    
    mockMvc.perform(get("/api/tasks/999"))
        .andExpect(status().isNotFound());
}
```

**学んだこと:**
- `Optional.empty()`で存在しないケースをモック
- HTTPステータスコードの適切な使い分け

#### Test 4: createTask_success - タスク作成成功
```java
@Test
@DisplayName("POST /api/tasks - 正常にタスクを作成できる")
void createTask_success() throws Exception {
    String requestBody = """
        {
            "summary": "新しいタスク",
            "description": "説明",
            "status": "TODO"
        }
        """;
    
    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated());
}
```

**学んだこと:**
- POSTリクエストでのJSON送信
- `201 Created`の使い方
- Text Blocksでの読みやすいJSON記述

#### Test 5: createTask_validationError - バリデーションエラー
```java
@Test
@DisplayName("POST /api/tasks - バリデーションエラーで400を返す")
void createTask_validationError() throws Exception {
    String invalidRequest = """
        {
            "summary": "",
            "status": "TODO"
        }
        """;
    
    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))));
}
```

**学んだこと:**
- バリデーションエラーのテスト方法
- `GlobalExceptionHandler`の動作確認
- JSONPathでエラー配列を検証

#### Test 6: updateTask_success - タスク更新成功
```java
@Test
@DisplayName("PUT /api/tasks/{id} - タスクを更新できる")
void updateTask_success() throws Exception {
    String requestBody = """
        {
            "summary": "更新後のタスク",
            "description": "更新後の説明",
            "status": "DOING"
        }
        """;
    
    mockMvc.perform(put("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk());
}
```

#### Test 7: updateTask_validationError - 更新時のバリデーションエラー
```java
@Test
@DisplayName("PUT /api/tasks/{id} - 不正なステータスで400を返す")
void updateTask_validationError() throws Exception {
    String invalidRequest = """
        {
            "summary": "タスク",
            "status": "INVALID_STATUS"
        }
        """;
    
    mockMvc.perform(put("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
        .andExpect(status().isBadRequest());
}
```

**学んだこと:**
- `@Pattern`バリデーションのテスト
- 更新操作でのバリデーション確認

#### Test 8: deleteTask_success - タスク削除成功
```java
@Test
@DisplayName("DELETE /api/tasks/{id} - タスクを削除できる")
void deleteTask_success() throws Exception {
    mockMvc.perform(delete("/api/tasks/1"))
        .andExpect(status().isNoContent());
    
    verify(taskService, times(1)).deleteTask(1L);
}
```

**学んだこと:**
- DELETEリクエストのテスト
- `204 No Content`の使い方
- `verify()`でメソッド呼び出しを検証

### テスト結果
```
TaskApiControllerTest > 8/8 tests passed ✅
```

**コミット:** `b69705f` - "Add TaskApiController tests (all 8 tests passing)"

---

## 🏗️ Phase 3: Request DTOパターンの理解

### 議論したトピック

#### なぜEntityを直接使わずにRequestを挟むのか？

**セキュリティ上の理由:**
```java
// ❌ 危険: EntityをそのままリクエストBODYにバインド
@PostMapping
public TaskEntity create(@RequestBody TaskEntity task) {
    // ユーザーがJSON内でidを指定できてしまう
    // { "id": 999, "summary": "...", "userId": 1 }
    // → 他人のIDを勝手に使える
}

// ✅ 安全: CreateRequestはIDフィールドを持たない
@PostMapping
public TaskApiDTO create(@RequestBody TaskApiCreateRequest request) {
    TaskEntity entity = request.toEntity(); // IDは常にnull
    // サーバー側でIDを自動採番するため安全
}
```

**編集時のセキュリティ:**
```java
// ✅ IDはURLパスから取得、リクエストBODYからは受け取らない
@PutMapping("/{id}")
public TaskApiDTO update(
    @PathVariable Long id,  // URLから取得（改ざん不可）
    @RequestBody TaskApiUpdateRequest request) {
    TaskEntity entity = request.toEntity(id); // IDを明示的に設定
    return taskService.updateTask(entity);
}
```

**学んだこと:**
- Request DTO = API層の境界での防御
- Entity = データ層のモデル
- DTO = レスポンス用のデータ転送オブジェクト
- レイヤー分離の重要性

---

## 🛡️ Phase 4: HTML5バリデーションの追加

### TaskForm.vueに追加
```vue
<template>
  <form @submit.prevent="handleSubmit">
    <!-- 概要: 必須 + 最大256文字 -->
    <input 
      type="text" 
      v-model="task.summary" 
      required 
      maxlength="256"
    />
    <span class="text-danger">*</span>
    
    <!-- ステータス: 必須 -->
    <select v-model="task.status" required>
      <option value="">選択してください</option>
      <option value="TODO">TODO</option>
      <option value="DOING">DOING</option>
      <option value="DONE">DONE</option>
    </select>
    <span class="text-danger">*</span>
  </form>
</template>
```

### 3層のバリデーション戦略
```
1. HTML5バリデーション (required, maxlength)
   ↓ 最初の防御線、UX向上
2. JavaScriptバリデーション (将来的に追加可能)
   ↓ 複雑なルール、リアルタイムフィードバック
3. サーバーサイドバリデーション (@NotBlank, @Size, @Pattern)
   ↓ 最終防御線、必ず実装
```

**学んだこと:**
- クライアント側バリデーションは改ざん可能（DevToolsで無効化できる）
- サーバーサイドバリデーションは必須
- 多層防御の考え方

---

## 🎭 Phase 5: E2Eテストの実装

### 1. E2Eテストとは
実際のユーザー操作をブラウザで自動実行し、アプリ全体（フロントエンド→API→データベース）が正しく動作するか確認するテスト。

**テストの階層:**
- **Unit Test**: 個別のメソッドをテスト（`TaskService`など）
- **Controller Test**: HTTP層をテスト（`TaskApiController`） ← Phase 2で実装
- **E2E Test**: 画面操作から保存まで全体をテスト ← Phase 5で実装

---

## 🛠️ E2Eテスト実装内容

### セットアップ
```bash
npm install -D @playwright/test
npx playwright install chromium
```

### 作成したファイル
1. **`playwright.config.js`** - Playwright設定ファイル
2. **`tests/e2e/task.spec.js`** - E2Eテストスイート（7テストケース）
3. **`package.json`** - テスト実行スクリプト追加

---

## ✅ 実装した8つのテストケース

### タスク一覧画面
1. **タスク一覧が表示される**
   - テーブルが表示されること
   - ヘッダーに「ID」が含まれること

2. **検索条件でタスクを絞り込める**
   - 検索キーワード入力 → 検索ボタンクリック
   - 検索結果が正しく表示されること

### タスク作成
3. **HTML5バリデーション: 概要が空の場合は送信できない**
   - `required`属性によるクライアント側バリデーション
   - 空欄のまま送信できないこと

4. **HTML5バリデーション: 概要が257文字以上入力できない**
   - `maxlength="256"`属性による文字数制限
   - 256文字を超えて入力できないこと

5. **正常系: タスクを作成できる**
   - フォーム入力 → 送信
   - 一覧ページへリダイレクト
   - 作成したタスクが表示されること

### タスク詳細・編集・削除
6. **タスク詳細が表示される**
   - 一覧から詳細ボタンをクリック
   - 詳細ページへ遷移
   - タスク情報が表示されること

7. **タスクを編集できる**
   - 編集ページへ遷移
   - フォーム編集 → 更新ボタンクリック
   - 詳細ページへ戻る
   - 変更内容が反映されていること

8. **タスクを削除できる（12/2追加）**
   - 削除用タスクを作成
   - 詳細ページで削除ボタンクリック
   - 確認ダイアログを承認
   - 一覧ページへリダイレクト
   - 削除したタスクが表示されないこと

---

## 🎯 テスト結果

```
Running 8 tests using 1 worker
  8 passed (1m 2s)
```

**全テストが成功** - アプリ全体（CRUD全機能）が正しく動作していることを確認

---

## 💡 学んだこと

### Playwrightの基本操作
```javascript
// ページ遷移
await page.goto('/tasks');

// 要素の操作
await page.fill('input[type="text"]', 'テキスト');
await page.click('button:has-text("検索")');
await page.selectOption('select', 'TODO');

// ダイアログ処理（削除確認など）
page.on('dialog', dialog => dialog.accept());

// 検証
await expect(page.locator('table')).toBeVisible();
await expect(page).toHaveURL('/tasks');
await expect(page.locator('h2')).toContainText('タスク');
await expect(page.locator('table')).not.toContainText('削除済み');
```

### HTML5バリデーションのテスト方法
```javascript
// バリデーション状態を取得
const isValid = await input.evaluate((el) => el.validity.valid);
expect(isValid).toBe(false);

// 実際の入力値を確認
const actualValue = await input.inputValue();
expect(actualValue.length).toBe(256);
```

### テストのデバッグ
- **Headedモード**: `npm run test:e2e:headed` でブラウザを表示して実行
- **スクリーンショット**: 失敗時に自動保存
- **動画録画**: テスト実行の様子を記録
- **HTMLレポート**: `npx playwright show-report` で詳細確認

---

## 🔧 トラブルシューティング

### 遭遇した問題と解決策

**問題1: ERR_CONNECTION_REFUSED**
- 原因: Vue開発サーバーが起動していない
- 解決: `npm run dev` で別ウィンドウでサーバー起動

**問題2: セレクタのミスマッチ**
- 原因: 実際のHTMLと異なるセレクタを使用
- 解決: 
  - `h1` → `h2`（実際のVueコンポーネントに合わせた）
  - `button[type="submit"]` → `button:has-text("検索")`

**問題3: Strict mode violation**
- 原因: 複数の要素がマッチする
- 解決: `.first()`で最初の要素を明示的に指定

---

## 📁 テストの実行方法

```bash
# 通常実行（ヘッドレスモード）
npm run test:e2e

# ブラウザを表示して実行
npm run test:e2e:headed

# UIモードで実行
npm run test:e2e:ui

# HTMLレポート表示
npm run test:report
```

**前提条件:**
- Spring Boot: `http://localhost:8080` で起動中
- Vue開発サーバー: `http://localhost:3000` で起動中

---

## 📊 本日の成果物

### 実装・修正したファイル一覧
| ファイル | 内容 | 行数 |
|---------|------|------|
| `GlobalExceptionHandler.java` | バリデーションエラーの統一ハンドリング | ~30行 |
| `TaskApiControllerTest.java` | Controller層のテスト（8テスト） | ~200行 |
| `TaskForm.vue` | HTML5バリデーション追加（required, maxlength） | 修正 |
| `playwright.config.js` | Playwright設定 | ~20行 |
| `tests/e2e/task.spec.js` | E2Eテスト（8テスト） | ~150行 |
| `package.json` | E2Eテストスクリプト追加 | 修正 |

### テスト構成
| レイヤー | テストクラス | テスト数 | 状態 |
|---------|------------|---------|------|
| **Service層** | `TaskServiceTest` | 5 | ✅ Pass |
| **Controller層** | `TaskApiControllerTest` | 8 | ✅ Pass (12/1実装) |
| **E2E** | `task.spec.js` | 8 | ✅ Pass (12/1-12/2実装) |
| **合計** | - | **21** | ✅ All Pass |

---

## 🎓 学習のポイント

### 1. グローバル例外ハンドリング
- `@RestControllerAdvice`でアプリ全体のエラーレスポンスを統一
- フロントエンドとの契約（API仕様）の重要性
- FieldErrorの構造化

### 2. Controller層のテスト技法
- `@WebMvcTest`でController層だけを分離テスト
- `MockMvc`でHTTPリクエストをシミュレート
- `@MockBean`でService層をモック化
- JSONPathでレスポンスを検証
- バリデーションエラーのテスト方法

### 3. セキュアなAPI設計
- Request DTOパターンでID改ざんを防止
- Entity直接バインドの危険性
- URLパスとリクエストBODYの使い分け
- レイヤー分離（API層 / ビジネス層 / データ層）

### 4. 多層防御のバリデーション
- HTML5バリデーション（UX向上）
- サーバーサイドバリデーション（必須）
- クライアント側バリデーションは改ざん可能
- 各層の役割分担

### 5. E2Eテストの実践
- Unit/Controllerテストでは検出できない統合バグを発見
- 実際のユーザー操作を再現
- Playwrightの基本操作（fill, click, expect）
- テストのデバッグ手法（headed mode, screenshot, video）

### 6. テスト駆動の開発
- テストを書くことで仕様が明確になる
- リファクタリング時の安全網
- 段階的な実装による学習効果

---

## 🔄 次のステップ（提案）

- [ ] GitHub ActionsでE2Eテストを自動実行
- [ ] エラーケースのE2Eテスト追加（404ページなど）
- [ ] パフォーマンステスト（大量データでの動作確認）
- [ ] クロスブラウザテスト（Firefox, WebKit）

---

## 📝 本日のまとめ

### 学習の流れ
1. **バリデーション問題の分析** → GlobalExceptionHandler実装で解決
2. **Controller層の8テスト** → 段階的実装で学習
3. **Request DTOパターン** → セキュリティとアーキテクチャの深い理解
4. **HTML5バリデーション** → 多層防御の実装
5. **E2Eテスト7ケース** → Playwright導入と全体テスト（12/1）
6. **E2Eテスト削除追加** → CRUD完全網羅（12/2）

### 達成事項
- ✅ バリデーションエラーの統一レスポンス実装（GlobalExceptionHandler）
- ✅ Controller層の完全なテストカバレッジ（8/8テストパス）
- ✅ HTML5バリデーション追加（required, maxlength）
- ✅ E2Eテストフレームワーク構築（8/8テストパス）
- ✅ **CRUD全機能のE2Eテスト完成**（作成・読取・更新・削除）
- ✅ 全21テストがパス（Service 5 + Controller 8 + E2E 8）
- ✅ セキュアなAPI設計の理解（Request DTOパターン）

### 技術スタック
- **バックエンド**: Spring Boot, @RestControllerAdvice, @WebMvcTest, MockMvc
- **フロントエンド**: Vue 3, HTML5 Validation
- **テスト**: JUnit 5, Mockito, JSONPath, Playwright, Chromium
- **アーキテクチャ**: Request/Response DTO, 3層アーキテクチャ, 多層バリデーション

### トラブルシューティング経験
- ❌ ERR_CONNECTION_REFUSED → ✅ サーバー起動確認
- ❌ セレクタミスマッチ → ✅ 実際のVueコンポーネント確認
- ❌ Strict mode violation → ✅ `.first()`で要素を特定

**学習時間**: 約5-6時間（12/1-12/2）
**コミット**: 
- `b69705f` - Controller tests (12/1)
- 最新コミット - E2E tests with delete (12/2)
**ブランチ**: feature-Vue
