package io.github.nnkwrik.astah.astah.builder;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;

import java.util.Map;

/**
 * iclassに構造化クラスを作成するビルダー
 *
 * @author Reika Nonokawa
 */
public class SClassBuilder extends CsCommonBuilder {

    private IPackage parentElement;

    private SClassBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                          IPackage parentElement) {
        super(csDiagramEditor);
        this.parentElement = parentElement;
    }

    public static SClassBuilder getBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                                           IPackage parentElement) {
        return new SClassBuilder(csDiagramEditor, parentElement);
    }

    @Override
    public SClassBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SClassBuilder taggedValues(Map<String, String> taggedValues) {
        this.taggedValues = taggedValues;
        return this;
    }

    @Override
    public IClass build() {

        //クラスを作る
        IClass sClass = modelCreator.createClass(parentElement, name);

        //タグ付き値を設置する
        if (taggedValues != null && !taggedValues.isEmpty()) {
            modelCreator.createTaggedValues(sClass, taggedValues);
        }
        //クラスを図に設置
        presentationCreator.createSClassPresentation(csDiagramEditor, sClass);
        return sClass;
    }
}
