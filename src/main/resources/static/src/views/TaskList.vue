<template>
  <div>
    <!--タスク作成ボタン-->
    <div class="mb-3">
      <router-link to="/tasks/create" class="btn btn-primary">タスク作成</router-link>
    </div>

    <!-- 検索フォーム -->
    <div class="card mb-3">
      <div class="card-header">検索</div>
      <div class="card-body">
        <!--キーワード検索-->
        <div class="mb-3">
          <label class="form-label">概要</label>
          <input 
            v-model="searchForm.summary"
            type="text" 
            class="form-control" 
            placeholder="キーワードで検索"
          />
        </div>

        <!--ステータス絞り込み-->
        <div class="mb-3">
          <label class="form-label">ステータス</label>
          <div>
            <input 
              v-model="searchForm.status" 
              type="checkbox" 
              value="TODO" 
              id="todo"
            />
            <label for="todo" class="ms-1 me-3">TODO</label>

            <input 
              v-model="searchForm.status" 
              type="checkbox" 
              value="DOING" 
              id="doing"
            />
            <label for="doing" class="ms-1 me-3">DOING</label>

            <input 
              v-model="searchForm.status" 
              type="checkbox" 
              value="DONE" 
              id="done"
            />
            <label for="done" class="ms-1">DONE</label>
          </div>
        </div>

        <!--検索ボタン-->
        <button @click="searchTasks" class="btn btn-primary">検索</button>
      </div>
    </div>

    <!-- タスク一覧テーブル -->
    <table class="table table-striped">
      <thead>
        <tr>
          <th>ID</th>
          <th>概要</th>
          <th>ステータス</th>
          <th>期限</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="task in taskList" :key="task.id">
          <td>{{ task.id }}</td>
          <td>{{ task.summary }}</td>
          <td>{{ task.status }}</td>
          <td>{{ task.deadline }}</td>
          <td>
            <router-link :to="`/tasks/${task.id}`" class="btn btn-sm btn-info">詳細</router-link>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { taskApi } from '../services/apiService'

export default {
  name: 'TaskList',
  setup() {
    // ========================================
    // データ定義
    // ========================================
    
    // タスク一覧データ（初期値: 空配列）
    const taskList = ref([])
    
    // 検索フォームの入力値
    const searchForm = ref({
      summary: '',     // キーワード検索
      status: []       // ステータス絞り込み
    })
    
    // ========================================
    // 関数
    // ========================================
    
    // タスク一覧を取得する関数
    const loadTasks = async () => {
      console.log('タスク読み込み開始')
      try {
        // 検索パラメータを準備
        const params = {}
        
        if (searchForm.value.summary) {
          params.summary = searchForm.value.summary
        }
        
        if (searchForm.value.status.length > 0) {
          // Proxyを通常の配列に変換（重要！）
          params.status = [...searchForm.value.status]
        }
        
        // APIを呼び出してタスク一覧を取得
        const response = await taskApi.getTasks(params)
        taskList.value = response.data
        console.log('取得したタスク:', taskList.value)
      } catch (error) {
        console.error('エラー発生:', error)
      }
    }
    
    // 検索ボタンがクリックされたとき
    const searchTasks = () => {
      loadTasks()
    }
    
    // ========================================
    // ページ表示時に実行
    // ========================================
    onMounted(() => {
      loadTasks()  // 最初にタスク一覧を取得
    })
    
    // ========================================
    // テンプレートで使う値を返す
    // ========================================
    return {
      taskList,
      searchForm,
      searchTasks
    }
  }
}
</script>

<style scoped>
/* 必要に応じてスタイルを追加 */
</style>
