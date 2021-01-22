package io.github.nnkwrik.astah.action;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;
import io.github.nnkwrik.astah.astah.ComposeDiagramManager;
import io.github.nnkwrik.astah.astah.listener.ModelEditListener;
import io.github.nnkwrik.astah.checker.ComposeConfirmer;
import io.github.nnkwrik.astah.compose.parser.ComposeYamlParser;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.util.YamlFileChooser;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * 既存のYAMLファイルからダイアグラムを生成する
 *
 * @author Reika Nonokawa
 */
public class FromExistYamlAction implements IPluginActionDelegate {
    @Override
    public Object run(IWindow window) throws UnExpectedException {
        try {

            ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
            if (!projectAccessor.hasProject()) {
                //プロジェクトを作成
                projectAccessor.create();
            }

            JFrame frame = (JFrame) window.getParent();
            //ファイルをチョイス
            File file = YamlFileChooser.open(frame);
            //リファレンスをチェック
            if (ComposeConfirmer.confirmReference(file)) {
                //composeオブジェクトにパースする
                Compose compose = ComposeYamlParser.parse(file);
                //妥当性をチェック
                if (ComposeConfirmer.confirmValidation(compose)) {
                    //ダイアグラムを作成
                    ComposeDiagramManager.creatDiagram(compose);
                    //ダイアグラム編集を禁止するリスナー
                    AstahAPI.getAstahAPI().getProjectAccessor().addEntityEditListener(new ModelEditListener(frame));
                }

            }

        } catch (IOException e) {
            String message = "Error occurred when load yaml file.";
            JOptionPane.showMessageDialog(window.getParent(), message, "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(window.getParent(), "Unexpected error has occurred." + e, "Alert", JOptionPane.ERROR_MESSAGE);
            throw new UnExpectedException();
        }

        return null;
    }

}
