package io.github.nnkwrik.astah.view.serviceCreator;

import io.github.nnkwrik.astah.Activator;
import org.apache.commons.io.IOUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

/**
 * サービス作成拡張タブのgui
 *
 * @author Reika Nonokawa
 */
public abstract class ServiceCreator extends JPanel {

    private RSyntaxTextArea textArea;

    private Container submitButtonPane;

    private String defaultText;

    public ServiceCreator() {
        try {
            //新規サービスのテンプレートを読み込む
            InputStream in = Activator.class.getClassLoader().getResourceAsStream("sample-service.yml");
            this.defaultText = IOUtils.toString(in, "UTF-8");

        } catch (Exception e) {
            System.out.println("サービス作成のためのテンプレートの読みとりに失敗しました。");
            e.printStackTrace();
            this.defaultText = "";
        }

        //textArea
        this.textArea = createTextArea();

        //作成button
        this.submitButtonPane = createSubmitButton();

        //レイアウト
        setLayout(new BorderLayout());
        add(new RTextScrollPane(textArea), BorderLayout.CENTER);
        add(submitButtonPane, BorderLayout.SOUTH);
    }

    /**
     * YAMLをhighlightで表示でできるtextareaを作成
     * @return
     */
    private RSyntaxTextArea createTextArea() {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_YAML);
        textArea.setCodeFoldingEnabled(true);

        textArea.setEditable(true);
        textArea.setText(defaultText);
        return textArea;
    }

    /**
     * 作成ボタンを作成
     * @return
     */
    private Container createSubmitButton() {
        JPanel buttonPane = new JPanel(new BorderLayout());
        JButton submitButton = new JButton("作成");
        submitButton.addActionListener(e -> {
            String newText = textArea.getText();
            create(newText);
        });
        buttonPane.add(submitButton, BorderLayout.EAST);
        return buttonPane;
    }

    /**
     * 新たなサービスを作成したときの操作
     * @param newRaw
     */
    public abstract void create(String newRaw);


}
