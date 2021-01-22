package io.github.nnkwrik.astah.action;


import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;
import io.github.nnkwrik.astah.astah.ComposeDiagramManager;
import io.github.nnkwrik.astah.checker.ComposeConfirmer;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.util.YamlFileChooser;

import javax.swing.*;
import java.io.IOException;

/**
 * 現在のダイアグラムからYAMLを保存する
 *
 * @author Reika Nonokawa
 */
public class ExportAction implements IPluginActionDelegate {


    public Object run(IWindow window) {


        JFrame frame = (JFrame) window.getParent();
        Compose compose = ComposeDiagramManager.getCompose();
        if (compose == null) {
            String message = "現在表示中の対象UMLはありません,既存のYAMLから生成するか,新規で作成してください";
            JOptionPane.showMessageDialog(window.getParent(), message, "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            //リファレンス検査と妥当性の検証
            if (ComposeConfirmer.confirm(compose)) {
                try {
                    //指定ファイルに保存する
                    YamlFileChooser.save(frame, compose.getRaw());
                    JOptionPane.showMessageDialog(window.getParent(), "終了しました", "終了しました", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
