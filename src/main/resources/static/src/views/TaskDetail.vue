<!--
  TaskDetail.vue - タスク詳細ページコンポーネント
  
  役割:
  - タスクの詳細情報を表示
  - 編集ページへのリンク
  - 削除機能
  
  表示URL: http://localhost:3000/tasks/1
  
  データフロー:
  1. コンポーネントがマウントされる
  2. loadTask()でAPIからタスク詳細を取得
  3. taskにデータを保存
  4. v-ifでデータがあれば表示
-->
<template>
  <!-- TODO: ここにHTMLを書く -->
  <!--データがあれば表示-->
  <div v-if="task">
    <!--ボタンエリア-->
    <div class="mb-3">
      <router-link :to="`/tasks`" class="btn btn-primary">一覧へ戻る</router-link>
      <router-link :to="`/tasks/${task.id}/edit`" class="btn btn-secondary">編集</router-link>
      <button @click="deleteTask" class="btn btn-danger">削除</button>
    </div>

    <!--タスク詳細表示-->
    <h2>#{{ task.id }} {{ task.summary }}</h2>
    <p><strong>ステータス:</strong> {{ task.status }}</p>
    <pre>{{ task.description }}</pre>
  </div>

  <!--データがない場合-->
  <div v-else>
    <p>タスクの詳細情報が見つかりません。</p>
  </div>
</template>

<script>
// TODO: ここにロジックを書く
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { taskApi } from '../services/apiService';

export default {
  name: 'TaskDetail',

  props: {
    id: {
      type: Number,
      required: true
    }

  },

  setup(props) {
    const router = useRouter();
    const task = ref(null);

    //タスク詳細取得
    const loadTask = async () => {
      try {
        const response = await taskApi.getTaskById(props.id);
        task.value = response.data;
      } catch (error) {
        console.error('タスク詳細の取得に失敗:', error);
      }
    };

    //タスク削除
    const deleteTask = async () => {
      // 1. 確認ダイアログ
      if (confirm('本当に削除しますか？')) {
        try {
          // 2. API呼び出し
          await taskApi.deleteTask(props.id)
          // 3. 削除成功したら一覧ページへ
          router.push('/tasks')
        } catch (error) {
          console.error('削除に失敗:', error)
        }
      }
    };

    onMounted(() => {
      loadTask();
    });
    return {
      task,
      deleteTask
    };
  }
}
</script>

<style scoped>
/* 詳細ページ専用のスタイル */
</style>
