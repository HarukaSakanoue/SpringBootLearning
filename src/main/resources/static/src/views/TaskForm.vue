<!--
  TaskForm.vue - タスク作成・編集フォームコンポーネント
  
  役割:
  - タスクの作成と編集を1つのコンポーネントで処理
  - バリデーションエラーの表示
  - router.meta.mode で CREATE/EDIT を判定
  
  表示URL:
  - 作成モード: http://localhost:3000/tasks/create
  - 編集モード: http://localhost:3000/tasks/1/edit
  
  データフロー:
  - CREATE: フォーム入力 → POST /api/tasks → 一覧ページへ遷移
  - EDIT: 既存データ取得 → フォーム編集 → PUT /api/tasks/1 → 詳細ページへ遷移
-->
<template>
  <!-- TODO: ここにHTMLを書く -->
  <div>
    <!--見出し-->
    <h1>{{ mode === 'CREATE' ? 'タスク作成' : 'タスク編集' }}</h1>
    <!--フォーム-->
    <form @submit.prevent="submitForm">
      <!--概要入力-->
      <div class="mb-3">
        <label class="form-label">概要 <span class="text-danger">*</span></label>
        <input 
          type="text" 
          class="form-control" 
          v-model="form.summary" 
          :class="{ 'is-invalid': errors.summary }"
          required
          maxlength="256"
          placeholder="タスクの概要を入力してください" />
        <div v-if="errors.summary" class="invalid-feedback d-block">
          {{ errors.summary }}
        </div>
      </div>

      <!--詳細入力-->
      <div class="mb-3">
        <label class="form-label">詳細</label>
        <textarea 
          class="form-control" 
          rows="5" 
          v-model="form.description"
          :class="{ 'is-invalid': errors.description }"
          placeholder="タスクの詳細を入力してください（任意）"></textarea>
        <div v-if="errors.description" class="invalid-feedback d-block">
          {{ errors.description }}
        </div>
      </div>

      <!--ステータス選択-->
      <div class="mb-3">
        <label class="form-label">ステータス <span class="text-danger">*</span></label>
        <select v-model="form.status" class="form-control" required>
          <option value="TODO">TODO</option>
          <option value="DOING">DOING</option>
          <option value="DONE">DONE</option>
        </select>
      </div>

      <!--ボタン-->
      <button type="submit" class="btn btn-primary">
        {{ mode === 'CREATE' ? '作成' : '更新' }}
      </button>
      <router-link :to="mode === 'CREATE' ? '/tasks' : `/tasks/${id}`" class="btn btn-secondary ms-2">
        戻る
      </router-link>
    </form>
  </div>

</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { taskApi } from '../services/apiService';

export default {
  name: 'TaskForm',

  props: {
    id: {
      type: Number,
      default: null  // CREATEモードでは不要
    }
  },

  setup(props) {
    const route = useRoute();
    const router = useRouter();

    //Create/Editの判定
    const mode = route.meta.mode || 'CREATE';  // 'CREATE' または 'EDIT'

    //フォームの入力値
    const form = ref({
      summary: '',
      description: '',
      status: 'TODO'
    });

    //エラーメッセージ
    const errors = ref({});

    const loadTask = async () => {
      //EDITモードかつIDがある場合のみ実行
      if (mode === 'EDIT' && props.id) {
        try {
          const response = await taskApi.getTaskById(props.id);
          form.value = response.data;
        } catch (error) {
          console.error('タスクデータの取得に失敗:', error);
        }
      }
    }

    const submitForm = async () => {
      console.log('🚀 フォーム送信開始')
      console.log('🚀 送信データ:', form.value)
      console.log('🚀 モード:', mode)
      
      errors.value = {};  // エラーリセット

      try {
        if (mode === 'CREATE') {
          //新規作成
          console.log('🚀 CREATE API呼び出し')
          await taskApi.createTask(form.value);
          router.push('/tasks');  // 一覧ページへ遷移
        } else {
          //編集
          console.log('🚀 UPDATE API呼び出し')
          await taskApi.updateTask(props.id, form.value)
          router.push(`/tasks/${props.id}`);  // 詳細ページへ遷移
        }
        console.log('✅ 送信成功')
      } catch (error) {
        // バリデーションエラー処理
        console.log('❌ エラーが発生:', error)
        console.log('❌ エラーレスポンス:', error.response)
        console.log('❌ エラーデータ:', error.response?.data)

        const resp = error?.response;
        const data = resp?.data ?? error;

        // 既知パターン1: { errors: [ { field, defaultMessage } ] } （Spring Validation）
        if (data && Array.isArray(data.errors)) {
          const mapped = {};
          for (const e of data.errors) {
            const field = e.field || (e.arguments?.[0]?.code) || 'summary';
            const msg = e.defaultMessage || e.message || '不正な入力です';
            if (!mapped[field]) mapped[field] = [];
            mapped[field].push(msg);
          }
          errors.value = mapped;
          console.debug('🟨 errors mapped (validation array)', errors.value);
          return;
        }

        // 既知パターン2: { summary: ['msg1','msg2'], description: ['...'] }
        if (data && typeof data === 'object' && !Array.isArray(data)) {
          const keys = Object.keys(data);
          const looksLikeFieldMap = keys.some(k => Array.isArray(data[k]));
          if (looksLikeFieldMap) {
            const mapped = {};
            for (const k of keys) {
              if (Array.isArray(data[k])) {
                mapped[k] = data[k].map(x => (typeof x === 'string' ? x : JSON.stringify(x)));
              }
            }
            errors.value = mapped;
            console.debug('🟨 errors mapped (object-map)', errors.value);
            return;
          }
        }

        // 既知パターン3: 単一メッセージ
        if (typeof data === 'string') {
          errors.value = { summary: [data] };
          console.debug('🟨 errors mapped (string)', errors.value);
          return;
        }

        // フォールバック: メッセージが取れない場合
        errors.value = { summary: ['送信に失敗しました。入力内容をご確認ください。'] };
        console.debug('🟨 errors fallback', errors.value);
      }
    };

    onMounted(loadTask);

    return {
      id: props.id,
      form,
      errors,
      mode,
      submitForm
    };
  }
}
</script>

<style scoped>
/* フォーム専用のスタイル */
</style>
