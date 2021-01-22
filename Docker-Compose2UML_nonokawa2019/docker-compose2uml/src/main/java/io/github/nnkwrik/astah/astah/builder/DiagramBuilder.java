package io.github.nnkwrik.astah.astah.builder;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;

import java.util.Map;

/**
 * astahの合成構造図において図を作成するビルダー
 *
 * @author Reika Nonokawa
 */
public class DiagramBuilder extends CsCommonBuilder {

    private INamedElement parentElement;

    private DiagramBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                           INamedElement parentElement) {
        super(csDiagramEditor);
        this.parentElement = parentElement;
    }

    public static DiagramBuilder getBuilder(CompositeStructureDiagramEditor csDiagramEditor,
                                            INamedElement parentElement) {
        return new DiagramBuilder(csDiagramEditor, parentElement);
    }

    @Override
    public DiagramBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DiagramBuilder taggedValues(Map<String, String> taggedValues) {
        this.taggedValues = taggedValues;
        return this;
    }


    @Override
    public ICompositeStructureDiagram build() {
        //合成構造図を作る
        ICompositeStructureDiagram csDiagram
                = diagramCreator.createCompositeStructureDiagram(csDiagramEditor, parentElement, name);
        //タグ付き値を設置する
        if (taggedValues != null && !taggedValues.isEmpty()) {
            modelCreator.createTaggedValues(csDiagram, taggedValues);
        }
        return csDiagram;
    }
}
