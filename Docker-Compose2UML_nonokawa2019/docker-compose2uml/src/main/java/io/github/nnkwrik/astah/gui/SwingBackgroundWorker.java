package io.github.nnkwrik.astah.gui;

import com.change_vision.jude.api.inf.AstahAPI;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

/**
 * JDialogを表示しながら、バックグラウンドで処理するworker
 *
 * @param <T> バックグラウンド処理の戻り値の型
 * @author Reika Nonokawa
 */
public abstract class SwingBackgroundWorker<T> extends JDialog {

    private String msg;
    private SwingWorker<T, Void> worker;

    public SwingBackgroundWorker(String msg) {
        super();
        this.msg = msg;
        init();
    }

    private void init() {

        final JDialog loading = new JDialog();
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(new JLabel(msg), BorderLayout.CENTER);
        loading.setUndecorated(true);
        loading.getContentPane().add(p1);
        loading.pack();
        JFrame frame = null;
        try {
            frame = AstahAPI.getAstahAPI().getProjectAccessor().getViewManager().getMainFrame();
        } catch (Exception e) {
            throw new AstahAPIRuntimeException(e);
        }
        loading.setLocationRelativeTo(frame);
        loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loading.setModal(true);

        this.worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                //バックグラウンドで実行するメソッド
                return SwingBackgroundWorker.this.doInBackground();
            }

            @Override
            protected void done() {
                //実行終了したら、diagramを閉じる
                loading.dispose();
            }
        };
        //workerを実装する
        worker.execute();
        loading.setVisible(true);
    }

    protected abstract T doInBackground();

    /**
     * バックグラウンドの実行結果を取得
     *
     * @return
     */
    public T get() {
        try {
            return worker.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
