package io.github.nnkwrik.astah.astah;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.editor.ModelEditorFactory;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.model.Service;
import io.github.nnkwrik.astah.compose.reverser.ComposeReverser;
import io.github.nnkwrik.astah.compose.reverser.DependsReverser;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * composeと図要素を管理するマネージャー
 *
 * @author Reika Nonokawa
 */
public class ComposeDiagramManager {

    //編集状態、USER か SYSTEMか
    private static EditSate editSate = EditSate.USER;

    //idiagramとcompsoeの対応付け
    private static Pair<IDiagram, Compose> iDiagramComposePair = null;

    //iclassとサービスの対応付け
    private final static Map<IClass, Service> iClassServiceMap = new HashMap<>();


    /**
     * composeからastahダイアグラムを作成
     *
     * @param compose
     */
    public static void creatDiagram(Compose compose) {
        creatDiagram(compose, null);
    }

    public static void creatDiagram(Compose compose, Map<Service, Point2D> serviceLocation) {
        try {

            ProjectAccessor projectAccessor = getProjectAccessor();

            //トランザクションを開始
            begin();
            //apiからCompositeStructureDiagramEditorを得る
            CompositeStructureDiagramEditor csDiagramEditor = getCsDiagramEditor();

            //composeから合成構造図を作成する
            ComposeReverser reverser = new ComposeReverser(csDiagramEditor, projectAccessor.getProject());
            ICompositeStructureDiagram csDiagram = reverser.reverseCompose(compose);
            iDiagramComposePair = Pair.of(csDiagram, compose);

            Map<Service, List<IElement>> serviceElements = new HashMap<>();
            if (compose.getServices() != null) {
                compose.getServices().forEach(s -> {
                    //serviceからastahモデルをリバース
                    List<IElement> elementList = reverser.reverseService(s);
                    serviceElements.put(s, elementList);

                    //iclass と serviceの対応を格納
                    IClass iClass = (IClass) elementList.stream()
                            .filter(e -> e instanceof IClass).findFirst().get();
                    putService(iClass, s);

                });
            }

            //networksとdepends on/linksをリバース
            DependsReverser dependsReverser = new DependsReverser(csDiagramEditor);
            dependsReverser.reverse(serviceElements, serviceLocation);

            //トランザクションを終了
            end();
            //作成した
            projectAccessor.getViewManager().getDiagramViewManager().open(csDiagram);
        } catch (Exception e) {
            throw new AstahAPIRuntimeException(e);
        }
    }


    /**
     * astahに表示されているダイヤグラムをクリア
     *
     * @throws InvalidEditingException
     * @throws ClassNotFoundException
     * @throws InvalidUsingException
     */
    public static void clearDiagram() {
        try {
            begin();


            if (iDiagramComposePair != null) {
                getCsDiagramEditor().deleteDiagram();
                iDiagramComposePair = null;
            }
            if (iClassServiceMap != null && iClassServiceMap.size() > 0) {
                BasicModelEditor basicModelEditor = getBasicModelEditor();
                for (IClass iClass : iClassServiceMap.keySet()) {
                    basicModelEditor.delete(iClass);
                }
                iClassServiceMap.clear();
            }
            end();
        } catch (Exception e) {
            throw new AstahAPIRuntimeException(e);
        }

    }

    /**
     * astahで表示されているserviceの位置を得る
     *
     * @return
     * @throws InvalidUsingException
     */
    public static Map<Service, Point2D> getServiceLocation() {
        Map<Service, Point2D> locationMap = new HashMap<>();
        for (Map.Entry<IClass, Service> entry : iClassServiceMap.entrySet()) {
            IClass iclass = entry.getKey();
            Service service = entry.getValue();
            INodePresentation presentation = null;
            try {
                presentation = (INodePresentation) iclass.getPresentations()[0];
            } catch (InvalidUsingException e) {
                throw new AstahAPIRuntimeException(e);
            }
            Point2D location = presentation.getLocation();
            locationMap.put(service, location);

        }
        return locationMap;
    }


    private static ProjectAccessor getProjectAccessor() throws ClassNotFoundException {
        return AstahAPI.getAstahAPI().getProjectAccessor();
    }

    private static CompositeStructureDiagramEditor getCsDiagramEditor() throws ClassNotFoundException, InvalidUsingException {
        return getProjectAccessor().getDiagramEditorFactory().getCompositeStructureDiagramEditor();
    }

    private static BasicModelEditor getBasicModelEditor() throws InvalidEditingException, ClassNotFoundException {
        return ModelEditorFactory.getBasicModelEditor();
    }

    private static void begin() {
        TransactionManager.beginTransaction();
        editSate = EditSate.SYSTEM;
    }

    private static void end() {
        TransactionManager.endTransaction();
        editSate = EditSate.USER;
    }


    public static Compose getCompose() {
        if (iDiagramComposePair == null) {
            return null;
        }
        return iDiagramComposePair.getRight();
    }

    private static void putService(IClass iClass, Service service) {
        iClassServiceMap.put(iClass, service);
    }

    public static Service getService(IClass iClass) {
        return iClassServiceMap.get(iClass);
    }

    public static EditSate getEditSate() {
        return editSate;
    }

    public enum EditSate {
        SYSTEM, USER
    }


}
