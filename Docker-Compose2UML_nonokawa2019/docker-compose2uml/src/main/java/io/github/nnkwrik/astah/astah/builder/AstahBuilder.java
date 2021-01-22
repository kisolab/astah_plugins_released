package io.github.nnkwrik.astah.astah.builder;

import com.change_vision.jude.api.inf.model.IElement;
import io.github.nnkwrik.astah.exception.AstahAPIRuntimeException;

import java.util.Map;

/**
 * astahに図/モデルを作成するビルダー
 *
 * @author Reika Nonokawa
 */
public interface AstahBuilder {

    /**
     * astahの図/モデルを作成する
     * @return
     */
    IElement build() throws AstahAPIRuntimeException;

    /**
     * 作成する図/モデルに名前を設定する
     * @param name
     * @return
     */
    AstahBuilder name(String name);

    /**
     * 作成する図/モデルにタグ付き値を設定する
     * @param taggedValues
     * @return
     */
    AstahBuilder taggedValues(Map<String, String> taggedValues);
}
