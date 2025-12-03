/**
 * API通信サービス
 * 
 * 役割:
 * - Spring BootのREST APIと通信する
 * - すべてのAPI呼び出しを一元管理
 * - axiosを使ってHTTPリクエストを送信
 * 
 * データ形式:
 * - リクエスト: JSON
 * - レスポンス: JSON
 */

import axios from 'axios'

// APIのベースURL（Viteのプロキシ設定で localhost:8080 に転送される）
const API_BASE_URL = '/api'

/**
 * axiosクライアントの作成
 * 
 * 設定:
 * - baseURL: すべてのリクエストの基本パス
 * - headers: デフォルトヘッダー（JSON形式を指定）
 * - paramsSerializer: 配列パラメータを正しく送信するための設定
 * 
 * プロキシの動き（vite.config.jsで設定）:
 * /api/tasks → http://localhost:3000/api/tasks → http://localhost:8080/api/tasks
 */
const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'  // JSON形式で通信
  },
  paramsSerializer: {
    // 配列パラメータを status=TODO&status=DOING の形式に変換
    indexes: null  // status[0]=TODO の形式ではなく、status=TODO&status=DOING にする
  }
})

/**
 * タスクAPIの関数集
 * 
 * 各関数はPromiseを返すため、async/awaitで呼び出す
 * 例: const response = await taskApi.getTasks()
 */
export const taskApi = {
  /**
   * 全タスク取得（検索条件付き）
   * 
   * @param {Object} params - 検索パラメータ
   * @param {string} params.summary - 検索キーワード（任意）
   * @param {Array<string>} params.status - ステータスリスト（任意）
   * @returns {Promise} タスク配列のJSON
   * 
   * 使用例:
   * const response = await taskApi.getTasks({ summary: 'Spring', status: ['TODO', 'DOING'] })
   * const tasks = response.data  // [{id:1, summary:'...', status:'TODO'}, ...]
   */
  getTasks(params = {}) {
    console.log('📡 API呼び出し - getTasks:', params)
    const response = client.get('/tasks', { params })
    response.then(res => {
      console.log('📡 APIレスポンス受信:', res.config.url, res.data.length + '件')
    }).catch(err => {
      console.error('📡 APIエラー:', err)
    })
    return response
  },

  /**
   * タスク詳細取得
   * 
   * @param {number} id - タスクID
   * @returns {Promise} タスクのJSON
   * 
   * 使用例:
   * const response = await taskApi.getTaskById(1)
   * const task = response.data  // {id:1, summary:'Spring Bootを学ぶ', ...}
   */
  getTaskById(id) {
    return client.get(`/tasks/${id}`)
  },

  /**
   * タスク作成
   * 
   * @param {Object} data - 作成するタスクデータ
   * @param {string} data.summary - 概要（必須）
   * @param {string} data.description - 詳細（任意）
   * @param {string} data.status - ステータス（'TODO', 'DOING', 'DONE'）
   * @returns {Promise} 作成されたタスクのJSON
   * 
   * 使用例:
   * const newTask = { summary: '新しいタスク', description: '詳細', status: 'TODO' }
   * const response = await taskApi.createTask(newTask)
   * const created = response.data  // {id:5, summary:'新しいタスク', ...}
   */
  createTask(data) {
    return client.post('/tasks', data)
  },

  /**
   * タスク更新
   * 
   * @param {number} id - 更新するタスクID
   * @param {Object} data - 更新後のデータ
   * @param {string} data.summary - 概要（必須）
   * @param {string} data.description - 詳細（任意）
   * @param {string} data.status - ステータス
   * @returns {Promise} 更新されたタスクのJSON
   * 
   * 使用例:
   * const updated = { summary: '更新後', description: '詳細', status: 'DOING' }
   * const response = await taskApi.updateTask(1, updated)
   */
  updateTask(id, data) {
    return client.put(`/tasks/${id}`, data)
  },

  /**
   * タスク削除
   * 
   * @param {number} id - 削除するタスクID
   * @returns {Promise} レスポンス（ボディなし、204 No Content）
   * 
   * 使用例:
   * await taskApi.deleteTask(1)
   * // 削除成功（レスポンスデータなし）
   */
  deleteTask(id) {
    return client.delete(`/tasks/${id}`)
  }
}

// デフォルトエクスポート（axiosクライアント本体）
export default client
