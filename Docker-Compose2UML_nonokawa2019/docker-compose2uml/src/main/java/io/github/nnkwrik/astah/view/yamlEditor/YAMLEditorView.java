package io.github.nnkwrik.astah.view.yamlEditor;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import com.change_vision.jude.api.inf.view.IEntitySelectionEvent;
import com.change_vision.jude.api.inf.view.IEntitySelectionListener;
import com.change_vision.jude.api.inf.view.IProjectViewManager;
import io.github.nnkwrik.astah.astah.ComposeDiagramManager;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.model.Element;
import io.github.nnkwrik.astah.model.Service;
import io.github.nnkwrik.astah.view.YAMLEditorAdaptor;

import java.awt.*;
import java.io.IOException;

/**
 * サービスの編集/削除のYAMLエディタータブ
 *
 * @author Reika Nonokawa
 */
public class YAMLEditorView extends YAMLEditor
        implements IPluginExtraTabView {

    private ProjectAccessor projectAccessor;
    private IProjectViewManager treeViewManager;
    private IDiagramViewManager diagramViewManager;

    private YAMLEditorAdaptor yamlEditorAdaptor;


    public YAMLEditorView() {
        try {
            this.projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
            this.treeViewManager = projectAccessor.getViewManager().getProjectViewManager();
            this.diagramViewManager = projectAccessor.getViewManager().getDiagramViewManager();
            this.yamlEditorAdaptor = new YAMLEditorAdaptor();
            addProjectEventListener();
        } catch (Exception e) {
            throw new AstahAPIRuntimeException(e);
        }
    }

    @Override
    public void apply(Element element, String newRaw) {
        try {
            //yamlEditorAdaptorで編集した部分を反映する
            Element newElement = yamlEditorAdaptor.edit(element, newRaw);
            if (newElement != null) {
                //表示中のYAMLを変更する
                setElement(newElement);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Element element) {
        try {
            //yamlEditorAdaptorで削除した部分を反映する
            Compose newCompose = yamlEditorAdaptor.delete((Service)element);
            if (newCompose != null) {
                //表示中のYAMLを変更する
                setElement(newCompose);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addProjectEventListener() {
        //ツリー上の要素を選択したときのリスナーを追加
        //ツリー上の要素を選択したら、エディターで要素のYAMLを表示する
        treeViewManager.addEntitySelectionListener(new IEntitySelectionListener() {
            @Override
            public void entitySelectionChanged(IEntitySelectionEvent iEntitySelectionEvent) {
                IEntity[] selectedEntity = treeViewManager.getSelectedEntities();
                if (selectedEntity.length <= 0) {
                    return;
                }

                for (IEntity iEntity : selectedEntity) {
                    if (iEntity instanceof IClass) {
                        //iclassが押された時、押されたサービスを表示する
                        Service service = ComposeDiagramManager.getService((IClass) iEntity);
                        setElement(service);
                    } else if (iEntity instanceof IDiagram) {
                        //IDiagramが押された時、全体を表示する
                        Compose compose = ComposeDiagramManager.getCompose();
                        setElement(compose);
                    }
                }
            }
        });

        //図上の要素を選択したときのリスナーを追加
        //図上の要素を選択したら、エディターで要素のYAMLを表示する
        diagramViewManager.addEntitySelectionListener(new IEntitySelectionListener() {

            @Override
            public void entitySelectionChanged(IEntitySelectionEvent iEntitySelectionEvent) {
                IEntity[] selectedEntity = diagramViewManager.getSelectedElements();
                if (selectedEntity.length <= 0) {
                    return;
                }

                for (IEntity iEntity : selectedEntity) {
                    IClass iClass = null;
                    if (iEntity instanceof IClass) {
                        //iclassが押された時、押されたサービスを表示する
                        iClass = (IClass) iEntity;
                    } else if (iEntity instanceof IPort && ((IPort) iEntity).getContainer() instanceof IClass) {
                        //IPortが押された時、押されたIPortに対応のサービスを表示する
                        iClass = (IClass) ((IPort) iEntity).getContainer();
                    } else if (iEntity instanceof IAttribute && ((IAttribute) iEntity).getContainer() instanceof IClass) {
                        //IAttributeが押された時、押されたIAttributeに対応のサービスを表示する
                        iClass = (IClass) ((IAttribute) iEntity).getContainer();
                    }

                    if (iClass != null) {
                        Service service = ComposeDiagramManager.getService(iClass);
                        setElement(service);
                    } else {
                        setElement(null);
                    }
                }

            }
        });

    }

    @Override
    public void addSelectionListener(ISelectionListener listener) {
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public String getDescription() {
        return "モデルの詳細情報を元のYAMLで示す。編集可能";
    }

    @Override
    public String getTitle() {
        return "YAML 詳細情報";
    }

    @Override
    public void activated() {
    }

    @Override
    public void deactivated() {
    }


}
