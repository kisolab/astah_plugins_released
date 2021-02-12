package umlcheker;;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.lang.Object;
import java.lang.Thread;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
 
import javax.swing.*;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.view.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.exception.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;

import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;



public class ProjectChangedAction {

    public void changedAction(ProjectEvent e){
        Activator acti = new Activator();
        try{
            System.out.println("--------------Project Changed Event--------------");
            ProjectEditUnit[] editunits = e.getProjectEditUnit();
            //System.out.println(editunits.length);
            
            for(ProjectEditUnit unit : editunits){
                //ADD 0, MODIFY 1, REMOVE 2
                int operationnum = unit.getOperation();
                IEntity entity = unit.getEntity();
                //System.out.println("  " + operationnum + " : " + entity);
                if(entity != null){
                    if(entity instanceof IPresentation){
                        IPresentation unitpre = (IPresentation)entity;
                        String operationname = null;
                        if(operationnum == 0){
                            operationname = "ADD   ";
                            acti.addcount ++;
                            System.out.println("add : " + acti.addcount);
                            break;
                        }
                    }
                }
                /*
                if(entity != null){
                    if(entity instanceof IPresentation){
                        IPresentation unitpre = (IPresentation)entity;
                        String operationname = null;
                        if(operationnum == 0){
                            operationname = "ADD   ";
                            acti.addcount ++;
                            System.out.println("add : " + acti.addcount);
                            
                        }
                        else if(operationnum == 1){
                            operationname = "MODIFY";
                            acti.modifycount ++;
                            System.out.println("mod : " + acti.modifycount);
                        }
                        else{
                            operationname = "REMOVE";
                            acti.removecount ++;
                            System.out.println("rem : " + acti.removecount);
                        }
                        System.out.println(operationname + " : " + unitpre + " : " + unitpre.getID());
                    }
                }
                */
            }
            
        }
        catch(Exception ex){
            System.out.println("changedAction failed");
        }
    }
}