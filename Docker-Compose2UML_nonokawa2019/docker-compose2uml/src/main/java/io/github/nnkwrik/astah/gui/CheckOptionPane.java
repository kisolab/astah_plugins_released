package io.github.nnkwrik.astah.gui;

import javax.swing.*;

/**
 * チェック結果を確認するOptionPane
 *
 * @author Reika Nonokawa
 */
public class CheckOptionPane extends JOptionPane {
    public final static String REFERENCE_CHECK = "リファレンスについて以下のエラーが検出されましたので、リバースできません。\n" +
            "編集したのち操作を行ってください。";
    public final static String VALIDATION_CHECK = "コンテナの妥当性について以下のワーニング及びエラーが検出されました";

    private String title;
    private String text;
    private boolean confirmCancel;

    //getValue() で続けるを押した時の戻り値
    public static final String CONTINUE = "1";
    //getValue() でキャンセルを押した時の戻り値
    public static final String CANCEL = "0";
    //option
    private static String[] options = {"キャンセル", "続ける", "確認"};


    /**
     * @param title         タイトル
     * @param message       メッセージ
     * @param messageType
     * @param confirmCancel ”本当にキャンセルする？”の確認をするか
     */
    public CheckOptionPane(String title, String message, int messageType, boolean confirmCancel) {

        super(title + "\n\n" + message, JOptionPane.WARNING_MESSAGE,
                messageType, null,
                options, options[1]);
        //JOptionPane.OK_OPTIONの時は”確認”ボタンだけを表示
        if (messageType == JOptionPane.OK_OPTION) {
            setOptions(new String[]{options[2]});
            setInitialValue(options[2]);
        }

        this.title = title;
        this.text = message;
        this.confirmCancel = confirmCancel;
        JDialog dialog = this.createDialog("yaml検証結果");
        dialog.setSize(900, 500);
        dialog.pack();
        dialog.setVisible(true);

        revalidate();
        repaint();
        setVisible(true);
        validate();
    }


    /**
     * 選択結果をリターン
     *
     * @return
     */
    @Override
    public Object getValue() {
        Object value = super.getValue();
        //キャンセルを選択した
        if (value == options[0] || value == options[2] || value == null) {
            //”本当にキャンセルする？”の確認をする
            if (confirmCancel) {
                int confirm = JOptionPane.showConfirmDialog(null, "操作がキャンセルされます。よろしいですか？", "Exit", JOptionPane.YES_NO_OPTION, 1);
                if (confirm == 0) {
                    //本当にキャンセルする
                    return CANCEL;
                } else {
                    //もう一度表示する
                    return new CheckOptionPane(title, text, messageType, true).getValue();
                }
            } else {
                //直接キャンセルする
                return CANCEL;
            }

        }
        //続ける
        return CONTINUE;
    }
}
