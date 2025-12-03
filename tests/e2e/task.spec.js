import { test, expect } from '@playwright/test';

/**
 * タスク管理アプリ E2Eテスト
 * 
 * 前提条件:
 * - Spring Boot APIが http://localhost:8080 で起動中
 * - Vue開発サーバーが http://localhost:3000 で起動中
 */

test.describe('タスク一覧画面', () => {
  
  test('タスク一覧が表示される', async ({ page }) => {
    await page.goto('/tasks');

    // テーブルが表示されることを確認
    await expect(page.locator('table')).toBeVisible();
    
    // テーブルヘッダーの確認（最初のthタグ）
    await expect(page.locator('th').first()).toContainText('ID');
  });

  test('検索条件でタスクを絞り込める', async ({ page }) => {
    await page.goto('/tasks');

    // 検索キーワードを入力
    await page.fill('input[type="text"]', 'Spring');

    // 検索ボタンをクリック（@clickイベントのボタン）
    await page.click('button:has-text("検索")');

    // 検索結果に「Spring」が含まれることを確認
    await expect(page.locator('table')).toContainText('Spring');
  });

});

test.describe('タスク作成', () => {
  
  test('HTML5バリデーション: 概要が空の場合は送信できない', async ({ page }) => {
    await page.goto('/tasks/create');
    
    // 概要を空のまま送信ボタンをクリック
    await page.click('button[type="submit"]');
    
    // HTML5バリデーションメッセージを確認
    const summaryInput = page.locator('input[type="text"]').first();
    const isValid = await summaryInput.evaluate((el) => el.validity.valid);
    expect(isValid).toBe(false);
    
    // ページが遷移していないことを確認
    await expect(page).toHaveURL('/tasks/create');
  });

  test('HTML5バリデーション: 概要が257文字以上入力できない', async ({ page }) => {
    await page.goto('/tasks/create');
    
    const summaryInput = page.locator('input[type="text"]').first();
    
    // 257文字入力を試みる
    const longText = 'あ'.repeat(257);
    await summaryInput.fill(longText);
    
    // maxlength=256により256文字までしか入力されていないことを確認
    const actualValue = await summaryInput.inputValue();
    expect(actualValue.length).toBe(256);
  });

  test('正常系: タスクを作成できる', async ({ page }) => {
    await page.goto('/tasks/create');
    
    // フォーム入力
    await page.fill('input[type="text"]', 'Playwrightで作成したタスク');
    await page.fill('textarea', 'E2Eテストの動作確認');
    await page.selectOption('select', 'TODO');
    
    // 送信
    await page.click('button[type="submit"]');
    
    // タスク一覧ページへリダイレクト
    await expect(page).toHaveURL('/tasks');
    
    // 作成したタスクが表示されることを確認
    await expect(page.locator('table')).toContainText('Playwrightで作成したタスク');
  });

});

test.describe('タスク詳細・編集・削除', () => {
  
  test('タスク詳細が表示される', async ({ page }) => {
    await page.goto('/tasks');
    
    // 最初のタスクIDリンクをクリック
    await page.click('table tbody tr:first-child a');
    
    // 詳細ページに遷移
    await expect(page).toHaveURL(/\/tasks\/\d+$/);
    
    // タスク情報が表示されることを確認（h2タグを使用）
    await expect(page.locator('h2')).toBeVisible();
  });

  test('タスクを編集できる', async ({ page }) => {
    // まず編集用のタスクを作成
    await page.goto('/tasks/create');
    
    // フォームが表示されるまで待つ
    await page.waitForSelector('input[type="text"]');
    
    // フォームに入力
    await page.fill('input[type="text"]', '編集前のタスク');
    await page.fill('textarea', '編集前の説明');
    await page.selectOption('select', 'TODO');
    await page.click('button[type="submit"]');
    
    // 一覧ページに戻る
    await page.waitForURL('/tasks');
    
    // 作成したタスクの詳細ページへ移動（最初のタスク）
    await page.click('a.btn-sm.btn-info >> nth=0');
    
    // タスク詳細が表示されるまで待つ
    await page.waitForSelector('h2');
    
    // 編集ボタンをクリック
    await page.click('a.btn-secondary:has-text("編集")');
    
    // 編集ページに遷移したことを確認
    await page.waitForURL(/\/tasks\/\d+\/edit/);
    
    // 概要を変更
    await page.fill('input[type="text"]', '編集後のタスク概要');
    
    // 更新ボタンをクリック
    await page.click('button[type="submit"]');
    
    // 詳細ページへリダイレクト
    await page.waitForURL(/\/tasks\/\d+$/);
    
    // 更新後の内容が表示されることを確認
    await expect(page.locator('h2')).toContainText('編集後のタスク概要');
  });

  test('タスクを削除できる', async ({ page }) => {
    // まず削除用のタスクを作成
    await page.goto('/tasks/create');
    await page.fill('input[type="text"]', '削除テスト用タスク');
    await page.selectOption('select', 'TODO');
    await page.click('button[type="submit"]');
    
    // 一覧ページで作成したタスクを確認
    await expect(page).toHaveURL('/tasks');
    await expect(page.locator('table')).toContainText('削除テスト用タスク');
    
    // 作成したタスクの詳細ページへ移動（最後の行の詳細ボタンをクリック）
    await page.click('table tbody tr:last-child a.btn-info');
    
    // 詳細ページで削除ボタンをクリック（確認ダイアログが表示される）
    page.on('dialog', dialog => dialog.accept()); // 確認ダイアログを自動承認
    await page.click('button.btn-danger:has-text("削除")');
    
    // 一覧ページにリダイレクトされることを確認
    await expect(page).toHaveURL('/tasks');
    
    // 削除したタスクが一覧に表示されないことを確認
    await expect(page.locator('table')).not.toContainText('削除テスト用タスク');
  });

});
