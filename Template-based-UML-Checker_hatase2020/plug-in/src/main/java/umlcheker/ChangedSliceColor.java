package umlcheker;

import java.util.*;
import java.util.List;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.Point2D;

import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.view.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.exception.*;

public class ChangedSliceColor {


    public void changeSliceColor()
            throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
        try{
            INodePresentation nps = null;
            ILinkPresentation lps = null;
            ITaggedValue tvs = null;
            ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
            BasicModelEditor bme = AstahAPI.getAstahAPI().getProjectAccessor().getModelEditorFactory().getBasicModelEditor();
            TransactionManager.beginTransaction();
            for(IActivityDiagram slicedgm : UMLcheck.slicematchact.keySet()){
				Map<String , Map<IFlow , IFlow>> sff = UMLcheck.slicematchact.get(slicedgm);	
				for(String slicename : sff.keySet()){
					Map<IFlow , IFlow> sfm = sff.get(slicename);
					for(IFlow sf1 : sfm.keySet()){
						IFlow sf2 = sfm.get(sf1);
                        IPresentation pf1 = sf1.getPresentations()[0];
                        IPresentation pf2 = sf2.getPresentations()[0];
                        String colorvalue = "#000000";
                        ChangedSliceColor.cmflag = false;
                        if(ExtensionView.sliceviewerflag == 1){
                            colorvalue = "#00FF99";
                        }
                        pf1.setProperty("line.color" , colorvalue);
                        pf2.setProperty("line.color" , colorvalue);
                    }
                }
			}
            TransactionManager.endTransaction();
        }catch(Exception e){
            System.out.println(e);
            TransactionManager.abortTransaction();
        }
    }


    public static boolean cmflag = false;

    public void changematchColor(IFlow selectflow)
            throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
        try{
            INodePresentation nps = null;
            ILinkPresentation lps = null;
            ITaggedValue tvs = null;
            ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
            BasicModelEditor bme = AstahAPI.getAstahAPI().getProjectAccessor().getModelEditorFactory().getBasicModelEditor();
            TransactionManager.beginTransaction();
            getdepthmatch(selectflow);
            cmflag = true;
            for(IFlow cf : depthmatchlist){
                IPresentation pf = cf.getPresentations()[0];
                String colorvalue = "#FF0000";
                pf.setProperty("line.color" , colorvalue);
            }
            TransactionManager.endTransaction();
        }catch(Exception e){
            System.out.println(e);
            TransactionManager.abortTransaction();
        }
    }


    public static List<IFlow> depthmatchlist;

    public void getdepthmatch(IFlow selectflow)
            throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
        depthmatchlist = new ArrayList<IFlow>();
        depthmatch(selectflow);
    }

    Stack<IFlow> fstack = new Stack<>();
    Queue<IFlow> fque = new ArrayDeque<>();

    private void depthmatch(IFlow selectflow)
            throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
        IDiagram dgm = Activator.actdgm;
        fque.add(selectflow);
        for(IActivityDiagram slicedgm : UMLcheck.slicematchact.keySet()){
            if(slicedgm == dgm){
                while(!(fque.isEmpty())){
                    IFlow probflow = fque.peek();
                    Map<String , Map<IFlow , IFlow>> sff = UMLcheck.slicematchact.get(slicedgm);
                    for(String slicename : sff.keySet()){
                        Map<IFlow , IFlow> sfm = sff.get(slicename);
                        for(IFlow sf1 : sfm.keySet()){
                            IFlow sf2 = sfm.get(sf1);
                            if(probflow.getId().equals(sf1.getId()) || probflow.getId().equals(sf2.getId())){
                                if(!((depthmatchlist.contains(sf1)) && (depthmatchlist.contains(sf2)))){
                                    if(!(depthmatchlist.contains(sf1))){
                                        depthmatchlist.add(sf1);
                                    }
                                    if(!(depthmatchlist.contains(sf2))){
                                        depthmatchlist.add(sf2);
                                    }
                                    fque.add(sf1);
                                    fque.add(sf2);
                                }
                            }
                        }
                    }
                    Object e = fque.poll();
                }
            }
        }
    }

    /*
    private void depthmatchfow(IFlow selectflow) 
            throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
        /*
        AstahAPI api = AstahAPI.getAstahAPI();
        ProjectAccessor projectAccessor = api.getProjectAccessor();
    	IDiagramViewManager vm = projectAccessor.getViewManager().getDiagramViewManager();
        IDiagram dgm = vm.getCurrentDiagram();
        
        IDiagram dgm = Activator.actdgm;
        for(IActivityDiagram slicedgm : UMLcheck.slicematchact.keySet()){
            if(slicedgm == dgm){
                Map<String , Map<IFlow , IFlow>> sff = UMLcheck.slicematchact.get(slicedgm);	
                probflow = selectflow;
                for(String slicename : sff.keySet()){
                    Map<IFlow , IFlow> sfm = sff.get(slicename);
                    for(IFlow sf1 : sfm.keySet()){
                        IFlow sf2 = sfm.get(sf1);
                        if(!(depthmatchlist.contains(sf2))){
                            if(probflow.getId().equals(sf1.getId()) || probflow.getId().equals(sf2.getId())){
                                if(!(depthmatchlist.contains(sf1))){
                                    depthmatchlist.add(sf1);
                                }
                                if(!(depthmatchlist.contains(sf2))){
                                    depthmatchlist.add(sf2);
                                }
                                depthmatchfow(sf2);
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    */

}