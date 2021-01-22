package io.github.nnkwrik.astah.astah.builder;

import com.change_vision.jude.api.inf.editor.CompositeStructureDiagramEditor;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.PresentationPropertyConstants;
import io.github.nnkwrik.astah.astah.creator.element.CsDiagramCreator;
import io.github.nnkwrik.astah.astah.creator.element.ModelCreator;
import io.github.nnkwrik.astah.astah.creator.presentation.PresentationCreator;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import java.util.Map;

/**
 * astahの合成構造図において図/モデルを作成する共通ビルダー
 *
 * @author Reika Nonokawa
 */
public abstract class CsCommonBuilder implements AstahBuilder {

    protected CompositeStructureDiagramEditor csDiagramEditor;
    //名前
    protected String name;
    //タグ付き値
    protected Map<String, String> taggedValues;

    //astah要素を作成するクリエイター
    protected CsDiagramCreator diagramCreator;
    protected ModelCreator modelCreator;
    //presentationを作成するクリエイター
    protected PresentationCreator presentationCreator;

    public CsCommonBuilder(CompositeStructureDiagramEditor csDiagramEditor) {
        this(csDiagramEditor, "UNKNOWN");
    }

    public CsCommonBuilder(CompositeStructureDiagramEditor csDiagramEditor, String name) {
        this.csDiagramEditor = csDiagramEditor;
        this.name = name;
        this.diagramCreator = new CsDiagramCreator();
        this.modelCreator = new ModelCreator();
        this.presentationCreator = new PresentationCreator();
    }


    /**
     * node要素の色を変更する
     *
     * @param presentation パート、ポートなど
     * @param color
     */
    protected void changeColor(INodePresentation presentation, String color) throws AstahAPIRuntimeException {
        try {
            presentation.setProperty(PresentationPropertyConstants.Key.FILL_COLOR, color);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
    }

    /**
     * link要素の色を変更する
     *
     * @param presentation 依存など
     * @param color
     */
    protected void changeColor(ILinkPresentation presentation, String color) throws AstahAPIRuntimeException {
        try {
            presentation.setProperty(PresentationPropertyConstants.Key.LINE_COLOR, color);
            presentation.setProperty(PresentationPropertyConstants.Key.FONT_COLOR, color);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
    }


}
