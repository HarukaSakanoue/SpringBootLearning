/**
 * Vue Router設定ファイル
 * 
 * 役割:
 * - URLパスとコンポーネントのマッピング
 * - クライアント側ルーティング（サーバーに問い合わせずにページ切り替え）
 * 
 * 特徴:
 * - ページリロードなしで画面遷移
 * - ブラウザの「戻る」ボタンが機能する
 */

import { createRouter, createWebHistory } from 'vue-router'
// 各ページのコンポーネントをインポート
import TaskTopPage from './views/TaskTopPage.vue'
import TaskList from './views/TaskList.vue'
import TaskDetail from './views/TaskDetail.vue'
import TaskForm from './views/TaskForm.vue'

/**
 * ルート定義
 * 各ルートは以下の情報を持つ:
 * - path: URLパス
 * - name: ルートの名前（router.push({ name: 'Home' })で使用可能）
 * - component: 表示するコンポーネント
 * - meta: 追加情報（フォームのCREATE/EDITモード判定など）
 * - props: URLパラメータをコンポーネントのpropsとして渡す
 */
const routes = [
  {
    path: '/',               // トップページ
    name: 'Home',
    component: TaskTopPage
  },
  {
    path: '/tasks',          // タスク一覧ページ
    name: 'TaskList',
    component: TaskList
  },
  {
    path: '/tasks/create',   // タスク作成ページ
    name: 'TaskCreate',
    component: TaskForm,
    meta: { mode: 'CREATE' }  // フォームをCREATEモードで起動
  },
  {
    path: '/tasks/:id',      // タスク詳細ページ（:idは動的パラメータ）
    name: 'TaskDetail',
    component: TaskDetail,
    // URLパラメータの :id を数値に変換してpropsで渡す
    // 例: /tasks/1 → props.id = 1
    props: route => ({ id: parseInt(route.params.id) })
  },
  {
    path: '/tasks/:id/edit', // タスク編集ページ
    name: 'TaskEdit',
    component: TaskForm,       // TaskFormコンポーネントを再利用
    meta: { mode: 'EDIT' },    // フォームをEDITモードで起動
    props: route => ({ id: parseInt(route.params.id) })
  }
]

/**
 * ルーターインスタンスを作成
 * 
 * createWebHistory():
 * - HTML5 History APIを使用（URLに # がつかない）
 * - 例: http://localhost:3000/tasks/1
 * 
 * createWebHashHistory()を使うと:
 * - URLに # がつく
 * - 例: http://localhost:3000/#/tasks/1
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
