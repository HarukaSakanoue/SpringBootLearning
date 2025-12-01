package com.example.todo.validation;
/**
 * バリデーションメッセージ定数クラス
 * 
 * 役割:
 * - バリデーションエラーメッセージを一元管理
 * - メッセージ変更時の修正箇所を1箇所にする
 * - タイポを防ぐ
 */
public class ValidationMessages {
    // コンストラクタをprivateにして、インスタンス化を防ぐ
    private ValidationMessages() {
        throw new AssertionError("定数クラスはインスタンス化できません");
    }


    // ========================================
    // 概要（summary）関連
    // ========================================
    
    /**
     * 概要が未入力の場合のメッセージ
     */
    public static final String SUMMARY_REQUIRED = "概要は必須です";

    /**
     * 概要が文字数制限を超えた場合のメッセージ
     */
    public static final String SUMMARY_SIZE = "概要は256文字以内で入力してください";


    // ========================================
    // ステータス（status）関連
    // ========================================
    
    /**
     * ステータスが未選択の場合のメッセージ
     */
    public static final String STATUS_REQUIRED = "ステータスは必須です";

    /**
     * ステータスの値が不正な場合のメッセージ
     */
    public static final String STATUS_PATTERN = "ステータスはTODO, DOING, DONEのいずれかで指定してください";
}
