package io.github.nnkwrik.astah.checker;


import io.github.nnkwrik.astah.exception.ComposeCheckException;
import io.github.nnkwrik.astah.exception.ComposeCheckException.Type;
import io.github.nnkwrik.astah.gui.CheckOptionPane;
import io.github.nnkwrik.astah.gui.SwingBackgroundWorker;
import io.github.nnkwrik.astah.model.Compose;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Composeのリファレンスと妥当性を確認
 */
public class ComposeConfirmer {

    /**
     * リファレンスと妥当性を確認する
     *
     * @param compose
     * @return
     */
    public static boolean confirm(Compose compose) {
        return confirmReference(compose.getRaw()) && confirmValidation(compose);
    }


    /**
     * リファレンスを確認する
     *
     * @param raw
     * @return
     */
    public static boolean confirmReference(String raw) {
        try {
            getReferenceCheckerInBack().check(raw);
        } catch (ComposeCheckException e) {
            return confirmException(e);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * リファレンスを確認する
     *
     * @param file
     * @return
     */
    public static boolean confirmReference(File file) {
        try {
            getReferenceCheckerInBack().check(file);
        } catch (ComposeCheckException e) {
            return confirmException(e);
        }

        return true;


    }

    /**
     * Diagramを表示し、バックグラウンドでComposeReferenceCheckerのインスタンスを取得
     *
     * @return ComposeReferenceCheckerのインスタンス
     */
    private static ComposeReferenceChecker getReferenceCheckerInBack() {
        SwingBackgroundWorker<ComposeReferenceChecker> setup =
                new SwingBackgroundWorker<ComposeReferenceChecker>("yaml検証プログラムセットアップ中、しばらくお待ちください。(約数10秒)") {
                    @Override
                    protected ComposeReferenceChecker doInBackground() {
                        //ComposeReferenceCheckerのインスタンスを取得
                        return ComposeReferenceChecker.getInstance();
                    }
                };
        //バックグラウンドでComposeReferenceCheckerのインスタンスを取得
        return setup.get();

    }

    /**
     * 妥当性を確認する
     *
     * @param compose
     * @return
     */
    public static boolean confirmValidation(Compose compose) {
        try {
            //セットアップ時間かからないので、そのままインスタンスを取得しチェックする
            ComposeValidationChecker.getInstance().check(compose);
        } catch (ComposeCheckException e) {
            return confirmException(e);
        }
        return true;
    }

    /**
     * チェックに引っかかった時、ユーザーの承認を得る
     *
     * @param e
     * @return
     */
    private static boolean confirmException(ComposeCheckException e) {
        String title = null;
        //リファレンスチェックで引っかかった時
        if (e.getType() == Type.REFERENCE) {
            title = CheckOptionPane.REFERENCE_CHECK;
            //チェック結果を表示
            CheckOptionPane optionPane = new CheckOptionPane(title, e.getMessage(), JOptionPane.OK_OPTION, false);
            optionPane.getValue();
            //続行できないため、必ずfalse
            return false;
        } else if (e.getType() == Type.VALIDATION) {
            title = CheckOptionPane.VALIDATION_CHECK;
            //チェック結果を表示
            CheckOptionPane optionPane = new CheckOptionPane(title, e.getMessage(), JOptionPane.OK_CANCEL_OPTION, true);
            Object value = optionPane.getValue();
            if (value == CheckOptionPane.CONTINUE) {
                //ユーザーが了承するなら、trueを返す
                return true;
            }
            return false;
        }

        return false;

    }


}