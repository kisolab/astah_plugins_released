package io.github.nnkwrik.astah.astah.creator.presentation;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import java.awt.geom.Point2D;

/**
 * モデルを図に設置し、プレゼンテーションを作成するクリエイター
 *
 * @author Reika Nonokawa
 */
public class PresentationCreator {

    /**
     * 構造化クラス図に設置する
     *
     * @param csDiagramEditor
     * @param aClass
     * @return
     */
    public INodePresentation createSClassPresentation(CompositeStructureDiagramEditor csDiagramEditor, IClass aClass)  {
        INodePresentation presentation = null;
        try {
            presentation = csDiagramEditor.createStructuredClassPresentation(aClass, new Point2D.Double(0, 0));
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return presentation;
    }

    /**
     * ポートを図に設置する
     *
     * @param csDiagramEditor
     * @param port
     * @param parentPresentation
     * @return
     */
    public INodePresentation createPortPresentation(CompositeStructureDiagramEditor csDiagramEditor,
                                                    IPort port,
                                                    INodePresentation parentPresentation)  {
        INodePresentation presentation = null;
        try {
            presentation = csDiagramEditor.createPortPresentation(parentPresentation, port, new Point2D.Double(0, 0));
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return presentation;
    }

    /**
     * 要素(パートなど)を図に設置する
     *
     * @param csDiagramEditor
     * @param element
     * @param parentPresentation
     * @return
     */
    public INodePresentation createElementPresentation(CompositeStructureDiagramEditor csDiagramEditor,
                                                       IElement element,
                                                       INodePresentation parentPresentation)  {
        INodePresentation presentation = null;
        try {
            presentation = csDiagramEditor.createNodePresentation(element, parentPresentation, new Point2D.Double(0, 0));
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return presentation;

    }

    /**
     * リンク(依存など)を図に設置する
     *
     * @param csDiagramEditor
     * @param element
     * @param linkEnd0        ターゲット
     * @param linkEnd1        リソース
     * @return
     */
    public ILinkPresentation createLinkPresentation(CompositeStructureDiagramEditor csDiagramEditor,
                                                    IElement element,
                                                    INodePresentation linkEnd0,
                                                    INodePresentation linkEnd1)  {
        ILinkPresentation linkPresentation = null;
        try {
            linkPresentation = csDiagramEditor.createLinkPresentation(element, linkEnd1, linkEnd0);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return linkPresentation;
    }

}
