package io.github.nnkwrik.astah.action;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate;
import com.change_vision.jude.api.inf.ui.IWindow;
import io.github.nnkwrik.astah.Activator;
import io.github.nnkwrik.astah.astah.ComposeDiagramManager;
import io.github.nnkwrik.astah.astah.listener.ModelEditListener;
import io.github.nnkwrik.astah.compose.parser.ComposeYamlParser;
import io.github.nnkwrik.astah.model.Compose;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * テンプレートから新規ででダイアグラムを生成する
 *
 * @author Reika Nonokawa
 */
public class FromNewYamlAction implements IPluginActionDelegate {
    @Override
    public Object run(IWindow window) throws UnExpectedException {
        try {

            JFrame frame = (JFrame) window.getParent();

            ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
            if (!projectAccessor.hasProject()) {
                //プロジェクトを作成
                projectAccessor.create();
            }
            //テンプレートから新規ででダイアグラムを生成する
            String templateName = "sample-compose.yml";

            InputStream in = Activator.class.getClassLoader().getResourceAsStream(templateName);
            String rawCompose = IOUtils.toString(in, "UTF-8");
            Compose compose = ComposeYamlParser.parse(templateName, rawCompose);
            //図を作成する
            ComposeDiagramManager.creatDiagram(compose);
            //ダイアグラム編集を禁止するリスナー
            AstahAPI.getAstahAPI().getProjectAccessor().addEntityEditListener(new ModelEditListener(frame));

        } catch (IOException e) {
            String message = "Error occurred when load yaml file.";
            JOptionPane.showMessageDialog(window.getParent(), message, "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(window.getParent(), "Unexpected error has occurred.", "Alert", JOptionPane.ERROR_MESSAGE);
            throw new UnExpectedException();
        }

        return null;
    }
}
