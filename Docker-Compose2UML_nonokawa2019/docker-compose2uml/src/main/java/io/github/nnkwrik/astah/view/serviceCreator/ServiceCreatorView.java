package io.github.nnkwrik.astah.view.serviceCreator;

import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import io.github.nnkwrik.astah.view.YAMLEditorAdaptor;

import java.awt.*;
import java.io.IOException;

/**
 * サービス作成拡張タブ
 *
 * @author Reika Nonokawa
 */
public class ServiceCreatorView extends ServiceCreator implements IPluginExtraTabView {

    //エディターでの変更を反映するためのアダプター
    private YAMLEditorAdaptor yamlEditorAdaptor;

    public ServiceCreatorView() {
        this.yamlEditorAdaptor = new YAMLEditorAdaptor();
    }

    @Override
    public void create(String newRaw) {
        try {
            //yamlEditorAdaptorで作成したサービスを反映する
            yamlEditorAdaptor.create(newRaw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return "サービスの追加";
    }

    @Override
    public String getDescription() {
        return "新たなDockerサービスを作成し図に追加";
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void addSelectionListener(ISelectionListener iSelectionListener) {

    }

    @Override
    public void activated() {

    }

    @Override
    public void deactivated() {

    }
}
