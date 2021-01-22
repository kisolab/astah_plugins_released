package io.github.nnkwrik.astah.astah.creator.element;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.ICompositeStructureDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

/**
 * astahで合成構造図を作成するクリエイター
 *
 * @author Reika Nonokawa
 */
public class CsDiagramCreator {

    /**
     * 合成構造図を作成する
     *
     * @param csDiagramEditor
     * @param parent
     * @param name
     * @return
     */
    public ICompositeStructureDiagram createCompositeStructureDiagram(
            CompositeStructureDiagramEditor csDiagramEditor,
            INamedElement parent,
            String name)  {

        ICompositeStructureDiagram csDiagram = null;
        try {
            csDiagram = csDiagramEditor.createCompositeStructureDiagram(parent, name);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return csDiagram;
    }
}
