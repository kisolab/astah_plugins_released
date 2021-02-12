package umlcheker;;

import java.util.*;
import java.awt.*;
import java.lang.reflect.*;
import java.awt.geom.Point2D;

import com.change_vision.jude.api.inf.project.ProjectAccessor;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.view.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.exception.*;

public class NodeGenerater{
  public void NodeGenerater(){
	//for(nodestack.empty())
  }

  //create decidion/mergeNode
  public INodePresentation createDMP(IDiagram dgm,IPresentation iNode, Point2D location)
	 throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	INodePresentation ps = null;
	ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
	try {
		TransactionManager.beginTransaction();
		ade.setDiagram(dgm);
	    ps = ade.createDecisionMergeNode((INodePresentation)iNode, location);
		TransactionManager.endTransaction();
	} catch (InvalidEditingException e) {
	    e.printStackTrace();
	    TransactionManager.abortTransaction();
    }
	return ps;
  }

	//create actionNode
  public INodePresentation createAP(IDiagram dgm, String name, Point2D location)
	 throws ClassNotFoundException, InvalidUsingException, InvalidEditingException {
	INodePresentation ps = null;
    ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
    try {
		TransactionManager.beginTransaction();
        ade.setDiagram(dgm);
        ps = ade.createAction(name, location);
        TransactionManager.endTransaction();
    } catch (InvalidEditingException e) {
        System.out.println(e);
        TransactionManager.abortTransaction();
    }
    return ps;
  }

	//create InitialNode
  public INodePresentation createINP(IDiagram dgm,String name, Point2D location)
	 throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	INodePresentation ps = null;
	ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
	try {
		TransactionManager.beginTransaction();
		ade.setDiagram(dgm);
	    ps = ade.createInitialNode(name, location);
		TransactionManager.endTransaction();
	} catch (InvalidEditingException e) {
	    e.printStackTrace();
	    TransactionManager.abortTransaction();
    }
	return ps;
  }

	//create FinalNode
  public INodePresentation createFNP(IDiagram dgm,String name, Point2D location)
	 throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	INodePresentation ps = null;
	ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
	try {
		TransactionManager.beginTransaction();
		ade.setDiagram(dgm);
	    ps = ade.createFinalNode(name, location);
		TransactionManager.endTransaction();
	} catch (InvalidEditingException e) {
	    e.printStackTrace();
	    TransactionManager.abortTransaction();
    }
	return ps;
  }

   //link presentation
  public ILinkPresentation createFw(IDiagram dgm, INodePresentation source , INodePresentation target)
	 throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	ILinkPresentation ps = null;
	ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
	try{
		TransactionManager.beginTransaction();
		ade.setDiagram(dgm);
		ps = ade.createFlow(source, target);
		TransactionManager.endTransaction();
	} catch(Exception e){
		System.out.println(e);
		TransactionManager.abortTransaction();
	}
	return ps;
  }

	//set Tagged
  public ITaggedValue createTg(IElement element, String tag, String value)
	 throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	ITaggedValue ps = null;
	BasicModelEditor bme = AstahAPI.getAstahAPI().getProjectAccessor().getModelEditorFactory().getBasicModelEditor();
	try{
		TransactionManager.beginTransaction();
        ps = bme.createTaggedValue(element,tag,value);
        TransactionManager.endTransaction();
	} catch(Exception e){
		System.out.println(e);
		TransactionManager.abortTransaction();
	}
	return ps;
  }

	//set Stereotype
  public void addSp(IElement element, String name)
	 throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	//ITaggedValue ps = null;
	try{
		TransactionManager.beginTransaction();
        element.addStereotype(name);
        TransactionManager.endTransaction();
	} catch(Exception e){
		System.out.println(e);
		TransactionManager.abortTransaction();
	}
	//return ps;
  }

   //setLocation
   public void moveNode(INodePresentation ipre, Point2D location)
     throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	try{
		TransactionManager.beginTransaction();
		ipre.setLocation(location);
		TransactionManager.endTransaction();
	} catch(Exception e){
		System.out.println(e);
		TransactionManager.abortTransaction();
	}
   }

   //setWidth&Height
   public void setWH(INodePresentation ipre, Double width , Double height)
     throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	try{
		TransactionManager.beginTransaction();
		ipre.setWidth(width);
		ipre.setHeight(height);
		TransactionManager.endTransaction();
	} catch(Exception e){
		System.out.println(e);
		TransactionManager.abortTransaction();
	}
   }
}