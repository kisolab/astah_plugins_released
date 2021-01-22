package io.github.nnkwrik.astah.astah.creator.element;

import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ModelEditorFactory;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.*;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import java.util.Map;

/**
 * astahでモデルを作成するクリエイター
 *
 * @author Reika Nonokawa
 */
public class ModelCreator {

    private BasicModelEditor modelEditor;

    public ModelCreator()  {
        try {
            this.modelEditor = ModelEditorFactory.getBasicModelEditor();
        } catch (Exception e) {
            throw new AstahAPIRuntimeException(e);
        }
    }

    /**
     * クラスを作成する
     *
     * @param parent
     * @param name
     * @return
     */
    public IClass createClass(IPackage parent, String name)  {
        IClass aClass = null;
        try {
            aClass = modelEditor.createClass(parent, name);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return aClass;
    }

    /**
     * パートを作成する
     *
     * @param iClass partを作成するiClass
     * @param tag
     * @param name
     * @return
     */
    public IAttribute createPart(IClass iClass, String tag, String name)  {
        IAttribute part = null;
        try {
            part = modelEditor.createAttribute(
                    iClass,
                    tag,
                    modelEditor.createClass(iClass, name));
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return part;

    }

    /**
     * ポートを作成する
     *
     * @param iClass
     * @param name
     * @return
     */
    public IPort createPort(IClass iClass, String name)  {
        IPort port = null;
        try {
            port = modelEditor.createPort(iClass, name);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return port;
    }

    /**
     * 依存を作成する
     *
     * @param source
     * @param target
     * @param name
     * @return
     */
    public IDependency createDependency(INamedElement source, INamedElement target, String name)  {
        IDependency dependency = null;
        try {
            dependency = modelEditor.createDependency(source, target, name);
        } catch (InvalidEditingException e) {
            throw new AstahAPIRuntimeException(e);
        }
        return dependency;
    }


    /**
     * タグ付き値を作成する
     *
     * @param element
     * @param taggedValueMap
     */
    public void createTaggedValues(INamedElement element, Map<String, String> taggedValueMap)  {
        for (Map.Entry<String, String> entry : taggedValueMap.entrySet()) {
            try {
                modelEditor.createTaggedValue(element, entry.getKey(), entry.getValue());
            } catch (InvalidEditingException e) {
                throw new AstahAPIRuntimeException(e);
            }
        }
    }
}
