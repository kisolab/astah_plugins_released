package io.github.nnkwrik.astah.compose.reverser;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IModel;
import io.github.nnkwrik.astah.astah.builder.DiagramBuilder;
import io.github.nnkwrik.astah.astah.builder.PartBuilder;
import io.github.nnkwrik.astah.astah.builder.PortBuilder;
import io.github.nnkwrik.astah.astah.builder.SClassBuilder;
import io.github.nnkwrik.astah.constants.AstahElement;
import io.github.nnkwrik.astah.constants.ModelColor;
import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.model.Element;
import io.github.nnkwrik.astah.model.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * compose リバーサー
 *
 * @author Reika Nonokawa
 */
public class ComposeReverser {
    private IModel parent;
    private CompositeStructureDiagramEditor csDiagramEditor;

    public ComposeReverser(CompositeStructureDiagramEditor csDiagramEditor, IModel parent) {
        this.csDiagramEditor = csDiagramEditor;
        this.parent = parent;
    }


    /**
     * composeから合成構造図を作成する
     *
     * @param compose
     * @return
     */
    public ICompositeStructureDiagram reverseCompose(Compose compose) {
        ICompositeStructureDiagram csDiagram = DiagramBuilder.getBuilder(csDiagramEditor, parent)
                .name(compose.getName())
                .taggedValues(compose.getDetailInfo())
                .build();
        return csDiagram;
    }

    /**
     * サービスをastahモデルにリバースする。
     *
     * @param service
     * @return
     */
    public List<IElement> reverseService(Service service) {
        List<IElement> elementList = new ArrayList<>();

        //serviceをリバース
        IClass sClass = SClassBuilder.getBuilder(csDiagramEditor, parent)
                .name(service.getName())
                .taggedValues(service.getDetailInfo())
                .build();
        elementList.add(sClass);

        int offset = 0; // TODO correct offset auto
        //buildをリバース
        if (service.getBuild() != null) {
            IElement build = reverseBuild(sClass, service.getBuild(), offset++);
            elementList.add(build);
        }
        //imageをリバース
        if (service.getImage() != null) {
            IElement image = reverseImage(sClass, service.getImage(), offset++);
            elementList.add(image);
        }
        //ポートをリバース
        if (service.getPorts() != null && service.getPorts().size() > 0) {
            List<Element> ports = service.getPorts();
            for (int i = 0; i < ports.size(); i++) {
                Element p = ports.get(i);
                IElement port = reversePort(sClass, p, i);
                elementList.add(port);
            }
        }
        return elementList;
    }

    /**
     * buildをリバースする
     *
     * @param sClass
     * @param build
     * @param offset
     * @return
     */
    private IElement reverseBuild(IClass sClass, Element build, int offset) {
        return PartBuilder.getBuilder(csDiagramEditor, sClass)
                .tag(AstahElement.PART_BUILD)
                .name(build.getName())
                .color(ModelColor.COLOR_BUILD_PART)
                .location(offset)
                .taggedValues(build.getDetailInfo()) // コンプレックスシンタックスの時のみ設置
                .build();
    }

    /**
     * imageをリバースする
     *
     * @param sClass
     * @param image
     * @param offset
     * @return
     */
    private IElement reverseImage(IClass sClass, Element image, int offset) {
        return PartBuilder.getBuilder(csDiagramEditor, sClass)
                .tag(AstahElement.PART_IMAGE)
                .name(image.getName())
                .color(ModelColor.COLOR_IMAGE_PART)
                .location(offset)
                .build();
    }

    /**
     * portsをリバースする
     *
     * @param sClass
     * @param port
     * @param offset
     * @return
     */
    private IElement reversePort(IClass sClass, Element port, int offset) {
        return PortBuilder.getBuilder(csDiagramEditor, sClass)
                .name(port.getName())
                .color(ModelColor.COLOR_PORT)
                .location(offset)
                .taggedValues(port.getDetailInfo()) // コンプレックスシンタックスの時のみ設置
                .build();
    }

}
