<!--
  App.vue - ルートコンポーネント
  
  役割:
  - Vue.jsアプリケーション全体の土台となるコンポーネント
  - ナビゲーションバーを表示
  - <router-view> で各ページのコンポーネントを表示
  
  構造:
  - <template>: HTML構造
  - <script>: JavaScript処理
  - <style>: CSS（scoped=このコンポーネントだけに適用）
-->
<template>
  <div id="app">
    <!-- ナビゲーションバー（Bootstrap） -->
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="container">
        <!-- ロゴ（クリックでトップページへ） -->
        <router-link to="/" class="navbar-brand">TODO</router-link>
        
        <!-- レスポンシブメニュー用のトグルボタン -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
          aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        
        <!-- ナビゲーションリンク -->
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav">
            <li class="nav-item">
              <!-- router-link: ページリロードなしで画面遷移 -->
              <router-link to="/" class="nav-link">Home</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/tasks" class="nav-link">タスク一覧</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <!-- メインコンテンツエリア -->
    <div class="container mt-3">
      <!-- router-view: ルートに応じた各ページコンポーネントがここに表示される -->
      <!-- 例: / なら TaskTopPage, /tasks なら TaskList -->
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'App'  // コンポーネント名（デバッグ時に役立つ）
}
</script>

<style scoped>
/* scoped: このコンポーネント内だけに適用されるCSS */
/* 必要に応じてスタイルを追加 */
</style>
