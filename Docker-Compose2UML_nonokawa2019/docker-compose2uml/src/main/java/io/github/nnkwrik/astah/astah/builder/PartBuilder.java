package io.github.nnkwrik.astah.astah.builder;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import java.awt.geom.Point2D;
import java.util.Map;

/**
 * iclassにpartを作成するビルダー
 *
 * @author Reika Nonokawa
 */
public class PartBuilder extends CsCommonBuilder {

    private IClass targetClass;
    private String tag;
    private String color;
    private boolean needCollectLocation;
    private int locationOffset;

    private PartBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                        IClass targetClass) {
        super(csDiagramEditor);
        this.targetClass = targetClass;
        this.tag = this.name;
    }

    public static PartBuilder getBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                                         IClass targetClass) {
        return new PartBuilder(csDiagramEditor, targetClass);
    }

    @Override
    public PartBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public PartBuilder taggedValues(Map<String, String> taggedValues) {
        this.taggedValues = taggedValues;
        return this;
    }

    public PartBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    public PartBuilder color(String hexColor) {
        this.color = hexColor;
        return this;
    }

    public PartBuilder location(int offset) {
        this.needCollectLocation = true;
        this.locationOffset = offset;
        return this;
    }


    @Override
    public IAttribute build() {
        INodePresentation targetPresentation = null;
        try {
            //クラスのpresentationを得る
            targetPresentation = (INodePresentation) targetClass.getPresentations()[0];
        } catch (InvalidUsingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        //パートを作る
        IAttribute part = modelCreator.createPart(targetClass, tag, name);

        //パートを図に設置し、presentationを得る
        INodePresentation partPresentation =
                presentationCreator.createElementPresentation(csDiagramEditor, part, targetPresentation);

        //位置を調節する
        if (needCollectLocation) {
            correctLocation(targetPresentation, partPresentation, locationOffset);
        }
        //色を変える
        if (color != null && color.length() > 0) {
            changeColor(partPresentation, color);
        }
        //タグ付き値を設置する
        if (taggedValues != null && !taggedValues.isEmpty()) {
            modelCreator.createTaggedValues(part, taggedValues);
        }
        return part;
    }

    /**
     * パートの位置を構造クラスの中心に位置を調節する
     *
     * @param parent
     * @param part
     * @param offset
     */
    private void correctLocation(INodePresentation parent, INodePresentation part, int offset) {

        Point2D location = part.getLocation();

        //中央に配置する
        double x = (parent.getWidth() - part.getWidth()) / 2;
        double y = (parent.getHeight() / 2) * (offset + 1);

        location.setLocation(x, y);
        try {
            part.setLocation(location);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
    }
}
