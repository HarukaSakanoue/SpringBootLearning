/**
 * Vue.jsアプリケーションのエントリーポイント
 * 
 * 役割:
 * - Vue.jsアプリケーションを起動する
 * - ルーターを設定する
 * - HTMLの <div id="app"> にマウントする
 * 
 * 実行タイミング:
 * index.htmlから <script type="module" src="..."> で読み込まれる
 */

import { createApp } from 'vue'        // Vue.js本体をインポート
import App from './App.vue'            // ルートコンポーネント
import router from './router'          // ルーティング設定
import 'bootstrap/dist/css/bootstrap.css'  // Bootstrap CSSを読み込む

// Vueアプリケーションインスタンスを作成
const app = createApp(App)

// ルーター機能を追加（ページ遷移のため）
app.use(router)

// HTMLの <div id="app"> にVueアプリをマウント（表示）
app.mount('#app')

// ========================================
// デバッグ用: ブラウザのConsoleでVueインスタンスにアクセス可能にする
// ========================================
// 使用例:
// console.log(window.__APP__)        // Vueアプリの情報を表示
// console.log(window.__router__)     // ルーターの情報を表示
window.__APP__ = app
window.__router__ = router
