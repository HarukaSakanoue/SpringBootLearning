# E2Eテストのセットアップ手順

## EdgeDriverの手動インストール

プロキシ環境下でEdgeDriverが自動ダウンロードできない場合、手動でインストールします。

### 1. EdgeDriverをダウンロード

1. Microsoft Edgeのバージョンを確認
   - Edgeを開く → 設定 → Edge情報 でバージョン番号を確認
   
2. EdgeDriverをダウンロード
   - https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
   - Edgeのバージョンに合ったドライバーをダウンロード
   - 例: Edge 138の場合 → EdgeDriver 138.x.x.x

### 2. EdgeDriverを配置

ダウンロードしたzipファイルを解凍し、`msedgedriver.exe`を以下のいずれかの場所に配置:

**推奨: プロジェクトフォルダに配置**
```
SpringBootLearning/
  ├── drivers/
  │   └── msedgedriver.exe  ← ここに配置
  ├── src/
  ├── build.gradle
  └── ...
```

**または: 任意のフォルダに配置**
- 例: `C:\WebDrivers\msedgedriver.exe`

### 3. テスト実行方法

#### 方法1: 環境変数を設定して実行

**Windowsコマンドプロンプト:**
```cmd
set EDGE_DRIVER_PATH=C:\Users\sm3harukasakanoue\Desktop\SpringBootLearning\drivers\msedgedriver.exe
gradlew test --tests com.example.todo.selenium.TaskE2ETest
```

**PowerShell:**
```powershell
$env:EDGE_DRIVER_PATH="C:\Users\sm3harukasakanoue\Desktop\SpringBootLearning\drivers\msedgedriver.exe"
gradlew test --tests com.example.todo.selenium.TaskE2ETest
```

#### 方法2: Gradleのシステムプロパティで指定

```cmd
gradlew test --tests com.example.todo.selenium.TaskE2ETest -Dwebdriver.edge.driver=C:\Users\sm3harukasakanoue\Desktop\SpringBootLearning\drivers\msedgedriver.exe
```

#### 方法3: build.gradleに設定を追加（永続的）

`build.gradle`のtestタスクに以下を追加:
```gradle
tasks.named('test') {
    useJUnitPlatform()
    
    systemProperty 'webdriver.edge.driver', 'C:/Users/sm3harukasakanoue/Desktop/SpringBootLearning/drivers/msedgedriver.exe'
}
```

### 4. 動作確認

```cmd
gradlew test --tests com.example.todo.selenium.TaskE2ETest.testIndexPageTitle
```

成功すると:
- Edgeブラウザが自動的に開く
- アプリケーションのページが表示される
- スクリーンショットが `build/reports/tests/e2e/` に保存される
- テストが成功する

## トラブルシューティング

### エラー: "Unable to locate the msedgedriver executable"
→ EdgeDriverのパスが正しく設定されていません。上記の方法1-3を確認してください。

### エラー: EdgeDriverのバージョン不一致
→ Edgeブラウザのバージョンとドライバーのバージョンが一致していません。
   Edgeのバージョンを確認し、同じバージョンのドライバーをダウンロードしてください。

### ブラウザが開かない
→ ポート8080が使用中の可能性があります。他のアプリケーションを終了してください。

## 参考リンク

- EdgeDriver公式: https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/
- Selenide公式: https://selenide.org/
- Selenium公式: https://www.selenium.dev/
