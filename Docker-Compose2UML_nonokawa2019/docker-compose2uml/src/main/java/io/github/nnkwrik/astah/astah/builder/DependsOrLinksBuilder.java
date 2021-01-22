package io.github.nnkwrik.astah.astah.builder;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import java.util.Map;


/**
 * astahの合成構造図において依存を作成するビルダー
 *
 * @author Reika Nonokawa
 */
public class DependsOrLinksBuilder extends CsCommonBuilder {

    //依存元
    private IClass from;
    //依存先
    private IClass to;
    //依存矢印の色
    private String color;

    private DependsOrLinksBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                                  IClass from,
                                  IClass to) {
        super(csDiagramEditor);
        this.from = from;
        this.to = to;
    }

    public static DependsOrLinksBuilder getBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                                                   IClass from,
                                                   IClass to) {
        return new DependsOrLinksBuilder(csDiagramEditor, from, to);
    }

    @Override
    public DependsOrLinksBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DependsOrLinksBuilder taggedValues(Map<String, String> taggedValues) {
        this.taggedValues = taggedValues;
        return this;
    }

    /**
     * 依存矢印の色を設定する
     *
     * @param hexColor 16進数カラー
     * @return
     */
    public DependsOrLinksBuilder color(String hexColor) {
        this.color = hexColor;
        return this;
    }

    @Override
    public INamedElement build() {
        INodePresentation sourcePresentation = null;
        INodePresentation targetPresentation = null;
        try {
            //依存の矢印(linkedPresentation)はattributeかpartのみが持つことができるため、ここではiClassのattributeに依存を設置する
            sourcePresentation = (INodePresentation) from.getAttributes()[0].getPresentations()[0];
            targetPresentation = (INodePresentation) to.getAttributes()[0].getPresentations()[0];
        } catch (InvalidUsingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        //依存を作る
        INamedElement iLinkModel = modelCreator.createDependency(from, to, name);

        //依存を図に設置し、presentationを得る
        ILinkPresentation dependencyPresentation
                = presentationCreator.createLinkPresentation(csDiagramEditor, iLinkModel,
                targetPresentation, sourcePresentation);

        //依存の色を変える
        if (color != null && color.length() > 0) {
            changeColor(dependencyPresentation, color);
        }

        //タグ付き値を設置する
        if (taggedValues != null && !taggedValues.isEmpty()) {
            modelCreator.createTaggedValues(iLinkModel, taggedValues);
        }
        return iLinkModel;
    }

}
