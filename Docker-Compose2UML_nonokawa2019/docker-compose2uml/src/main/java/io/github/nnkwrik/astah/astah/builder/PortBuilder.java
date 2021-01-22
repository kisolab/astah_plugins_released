package io.github.nnkwrik.astah.astah.builder;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import java.awt.geom.Point2D;
import java.util.Map;

/**
 * iclassにportを作成するビルダー
 *
 * @author Reika Nonokawa
 */
public class PortBuilder extends CsCommonBuilder {

    private IClass targetClass;
    private String color;
    private boolean needCorrectLocation;
    private int locationOffset;

    private PortBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                        IClass targetClass) {
        super(csDiagramEditor, "");
        this.targetClass = targetClass;
    }

    public static PortBuilder getBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                                         IClass targetClass) {
        return new PortBuilder(csDiagramEditor, targetClass);
    }

    @Override
    public PortBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public PortBuilder taggedValues(Map<String, String> taggedValues) {
        this.taggedValues = taggedValues;
        return this;
    }


    public PortBuilder color(String hexColor) {
        this.color = hexColor;
        return this;
    }

    public PortBuilder location(int offset) {
        this.needCorrectLocation = true;
        this.locationOffset = offset;
        return this;
    }


    @Override
    public IPort build()  {
        INodePresentation targetPresentation = null;
        try {
            //クラスのpresentationを得る
            targetPresentation = (INodePresentation) targetClass.getPresentations()[0];
        } catch (InvalidUsingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        //パートを作る
        IPort port = modelCreator.createPort(targetClass, name);

        //パートを図に設置し、presentationを得る
        INodePresentation portPresentation =
                presentationCreator.createPortPresentation(csDiagramEditor, port, targetPresentation);

        //位置を調節する
        if (needCorrectLocation) {
            location(targetPresentation, portPresentation, locationOffset);
        }
        //色を変える
        if (color != null && color.length() > 0) {
            changeColor(portPresentation, color);
        }
        //タグ付き値を設置する
        if (taggedValues != null && !taggedValues.isEmpty()) {
            modelCreator.createTaggedValues(port, taggedValues);
        }
        return port;
    }


    public static int PORT_ELEMENT_SIZE = 27;

    /**
     * ポートの位置を構造クラスの外周に時計回りに位置を調節する
     *
     * @param parent
     * @param port
     * @param offset
     */
    private void location(INodePresentation parent, INodePresentation port, int offset)  {
        //時計回りに配置をするためのオフセット
        offset++;

        Double hc = parent.getHeight() / PORT_ELEMENT_SIZE;
        int hCapacity = hc.intValue();

        Double wc = parent.getWidth() / PORT_ELEMENT_SIZE;
        int wCapacity = wc.intValue();

        while (offset > 2 * hCapacity + 2 * wCapacity) {
            offset = offset - (2 * hCapacity + 2 * wCapacity);
        }

        //位置座標を計算する
        double x = 0;
        double y = 0;

        if (offset <= hCapacity) {
            //left
            x = 0;
            y = PORT_ELEMENT_SIZE * offset;
        } else if (offset <= hCapacity + wCapacity) {
            //bottom
            x = PORT_ELEMENT_SIZE * (offset - hCapacity);
            y = parent.getHeight();
        } else if (offset <= 2 * hCapacity + wCapacity) {
            //right
            x = parent.getWidth();
            y = parent.getHeight() - (offset - (hCapacity + wCapacity)) * PORT_ELEMENT_SIZE;
        } else {
            //top
            x = parent.getWidth() - (offset - (2 * hCapacity + wCapacity)) * PORT_ELEMENT_SIZE;
            y = 0;
        }

        Point2D location = port.getLocation();
        location.setLocation(x, y);
        try {
            port.setLocation(location);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
    }
}
