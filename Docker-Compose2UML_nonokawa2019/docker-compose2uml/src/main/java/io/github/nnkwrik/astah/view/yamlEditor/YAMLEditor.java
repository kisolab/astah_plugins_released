package io.github.nnkwrik.astah.view.yamlEditor;

import io.github.nnkwrik.astah.model.Compose;
import io.github.nnkwrik.astah.model.Element;
import io.github.nnkwrik.astah.model.Service;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * サービスの編集/削除のYAMLエディターのgui
 *
 * @author Reika Nonokawa
 */
public abstract class YAMLEditor extends JPanel {
    private RSyntaxTextArea textArea;

    private Container editButtonPane;
    private JButton editButton;
    private JButton deleteButton;

    private Container applyButtonPane;
    private JButton cancelButton;
    private JButton applyButton;

    //今textareaに表示しているElement
    private Element curElement;


    public YAMLEditor() {
        //textarea
        this.textArea = createTextArea();
        //2つのボタンパネル
        //ブラウス状態で表示するボタンパネル(default)
        this.editButtonPane = createEditButton();
        //編集状態で表示するボタンパネル
        this.applyButtonPane = createApplyButton();

        //レイアウト
        setLayout(new BorderLayout());
        add( new RTextScrollPane(textArea), BorderLayout.CENTER);

        resetScrollPosition();

        //defaultのボタンパネルにリセットする
        resetButton();
    }

    /**
     * YAMLをhighlightで表示でできるtextareaを作成
     * @return
     */
    private RSyntaxTextArea createTextArea() {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_YAML);
        textArea.setCodeFoldingEnabled(true);
        textArea.setEditable(false);

        return textArea;
    }

    /**
     * ブラウス状態で表示する 編集と削除ボタンを持つボタンパネルの作成
     * @return
     */
    private Container createEditButton() {

        JPanel buttonPane = new JPanel(new BorderLayout());
        JPanel rightButtonPane = new JPanel(new BorderLayout());


        this.editButton = new JButton("編集");
        editButton.addActionListener(e -> {
            changeEditorState(State.EDIT);
        });

        this.deleteButton = new JButton("削除");
        deleteButton.addActionListener(e -> {
            //削除
            delete(curElement);
            changeEditorState(State.BROWSE);
        });

        rightButtonPane.add(editButton, BorderLayout.WEST);
        rightButtonPane.add(deleteButton, BorderLayout.EAST);

        buttonPane.add(rightButtonPane, BorderLayout.EAST);
        return buttonPane;
    }


    /**
     * 編集状態で表示する キャンセルと適応ボタンを持つボタンパネルの作成
     * @return
     */
    private Container createApplyButton() {
        JPanel buttonPane = new JPanel(new BorderLayout());
        JPanel rightButtonPane = new JPanel(new BorderLayout());


        this.cancelButton = new JButton("キャンセル");
        cancelButton.addActionListener(e -> {
            changeEditorState(State.BROWSE);
        });

        this.applyButton = new JButton("適応");
        applyButton.addActionListener(e -> {
            String newText = textArea.getText();
            apply(curElement, newText);
            changeEditorState(State.BROWSE);
        });

        rightButtonPane.add(cancelButton, BorderLayout.WEST);
        rightButtonPane.add(applyButton, BorderLayout.EAST);

        buttonPane.add(rightButtonPane, BorderLayout.EAST);
        return buttonPane;
    }

    /**
     * エディターの状態(編集/ブラウス)が変更された時の表示ボタンとテキストの変更
     * @param nextState
     */
    private void changeEditorState(State nextState) {
        switch (nextState) {
            case BROWSE:
                //保存したtextに戻す(更新する)
                textArea.setText(curElement.getRaw());
                //スクロール位置を戻す
                resetScrollPosition();
                //編集不可にする
                textArea.setEditable(false);
                //編集ボタンに入れ替える
                applyButtonPane.setVisible(false);
                editButtonPane.setVisible(true);
                add(editButtonPane, BorderLayout.SOUTH);
                break;
            case EDIT:
                //編集前のtext
                //textAreaを編集可能にする
                textArea.setEditable(true);
                //適応ボタンに入れ替える
                editButtonPane.setVisible(false);
                applyButtonPane.setVisible(true);
                add(applyButtonPane, BorderLayout.SOUTH);
                break;
        }

        flushTextarea();
    }

    /**
     * YAMLエディターで表示する要素を設定する
     * @param curElement
     */
    protected void setElement(Element curElement) {
        this.curElement = curElement;
        if (curElement != null) {
            textArea.setText(curElement.getRaw());
        } else {
            textArea.setText("");
        }
        resetScrollPosition();
        resetButton();

        if (curElement instanceof Compose) {
            deleteButton.setVisible(false);
        } else if (!(curElement instanceof Service)) {
            editButtonPane.setVisible(false);
        }

        flushTextarea();
    }


    /**
     * スクロールバーを一番上に戻す
     */
    private void resetScrollPosition() {
        textArea.setCaretPosition(0);
    }

    /**
     * ボタンを初期状態（ブラウス状態）の表示にする
     */
    private void resetButton() {
        this.editButton.setVisible(true);
        this.deleteButton.setVisible(true);
        this.applyButton.setVisible(true);
        this.cancelButton.setVisible(true);

        applyButtonPane.setVisible(false);
        editButtonPane.setVisible(true);

        add(editButtonPane, BorderLayout.SOUTH);
    }

    /**
     * textareaの表示を更新する
     */
    private void flushTextarea() {
        revalidate();
        repaint();
    }


    /**
     * エディターでの編集が適応されるときの操作
     * @param element
     * @param newRaw
     */
    public abstract void apply(Element element, String newRaw);

    /**
     * サービスが削除されたときの操作
     * @param element
     */
    public abstract void delete(Element element);

    /**
     * YAMLエディターの状態
     */
    private enum State {
        BROWSE,//ブラウス中
        EDIT//編集中
    }
}
