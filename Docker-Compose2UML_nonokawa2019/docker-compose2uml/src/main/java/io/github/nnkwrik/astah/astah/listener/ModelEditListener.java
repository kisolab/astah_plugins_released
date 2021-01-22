package io.github.nnkwrik.astah.astah.listener;

import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.model.IEntity;
import com.change_vision.jude.api.inf.project.EntityEditListener;
import io.github.nnkwrik.astah.astah.ComposeDiagramManager;
import io.github.nnkwrik.astah.model.Compose;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * astahのダイアグラムでモデルが編集された際禁止するリスナー
 *
 * @author Reika Nonokawa
 */
public class ModelEditListener implements EntityEditListener {
    private Component parent;
    private ScheduledExecutorService scheduledExecutor;

    public ModelEditListener(Component parent) {
        this.parent = parent;
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }


    /**
     * モデルが削除される時
     *
     * @param iEntities
     */
    @Override
    public void preDeleteEntity(IEntity[] iEntities) {
        if (ComposeDiagramManager.getEditSate() == ComposeDiagramManager.EditSate.SYSTEM) {
            return;
        }
        TransactionManager.endTransaction();
        String msg = "図から直接での削除は禁止されています。拡張ビューから操作を行ってください。";
        JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.WARNING_MESSAGE);
        Compose compose = ComposeDiagramManager.getCompose();
        try {
            //composeをダイアグラムに設置しなおす
            ComposeDiagramManager.clearDiagram();
            ComposeDiagramManager.creatDiagram(compose);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TransactionManager.beginTransaction();
        }

    }

    /**
     * モデルがリネームされる時
     *
     * @param iEntity
     * @param s
     */
    @Override
    public void preRenameEntity(IEntity iEntity, String s) {
        if (ComposeDiagramManager.getEditSate() == ComposeDiagramManager.EditSate.SYSTEM) {
            return;
        }
        TransactionManager.endTransaction();
        String msg = "図から直接での編集は禁止されています。拡張ビューから操作を行ってください。";
        JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.WARNING_MESSAGE);
        Compose compose = ComposeDiagramManager.getCompose();
        //直接行うとTransactionでエラーが発生するため、Threadを起動して実行
        scheduledExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    //composeをダイアグラムに設置しなおす
                    ComposeDiagramManager.clearDiagram();
                    ComposeDiagramManager.creatDiagram(compose);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 100, TimeUnit.MILLISECONDS);

        TransactionManager.beginTransaction();

    }
}
