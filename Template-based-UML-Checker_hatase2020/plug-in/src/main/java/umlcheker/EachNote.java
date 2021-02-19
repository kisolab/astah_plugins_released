package umlcheker;;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.awt.Point;
import java.awt.geom.Point2D;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;

public class EachNote extends DiagramReflection{
	public static void main(String[] args) throws Exception{
	}
	
	DiagramReflection dr = new DiagramReflection();

	Map<Double , String> notecomment 
	 = new HashMap<Double , String>(){
		{
	//1.Critical-----------
		//1-----------
			put(1.11,"[Critical]Unable to find the Class inside this ClassDiagram");
		//2----------
			put(1.12,"[Critical]Unable to find Partition in this ActivityDiagram");
			put(1.121,"[Critical]Unable to find initial node in this ActivityDiagram");
			put(1.122,"[Critical]Unable to find final node in this ActivityDiagram");
			put(1.21,"[Critical]Unable to find the [/] in ClassDiagrams");
		//3----------
			put(1.31,"[Critical]Flow is broken at [/]");
			put(1.32,"[Critical]Worong the number of the [/]'s Flow");
			put(1.33,"[Critical][/]");
			put(1.34,"[Critical][/] has already passed");
		//4----------
			put(1.41,"[Critical]Unable to match any node with [/]");
			put(1.42,"[Critical]There is no conditional statment in [/]");
			put(1.43,"[Critical]Finished before node appeared that matched [/]");
			put(1.44,"[Critical]This break point is imprecise");
		//5----------
			put(1.51,"[Critical]There is no connector that matches [/]");
			put(1.52,"[Critical]Unable to connect InputPin to this [/]");
			put(1.53,"[Critical]Warong the try-catch convention");
	//2.Warning---------------
		//1----------
			put(2.1,"[Warning]Unable to find the [/] in ActivityDiagrams");
			put(2.21,"[Warning]Unable to match the number of arguments [/] and the method");
			put(2.2121,"[Warning]Unable to match the arguments [/] of the method");
			put(2.2122,"[Warning]Unable to return the value [/] of the method");
		//2----------
			put(2.31,"[Warning]Unable to find the [/] until FinalNode");
		}
	};

	Map<IPresentation , String> elementnote
	  = new HashMap<IPresentation , String>();
	Map<IPresentation , Double> elementev
	 = new HashMap<IPresentation , Double>();

	public void eachnote(IPresentation pre , Double ev , IElement el){
		String comment = notecomment.get(ev);
		String[] str = comment.split("\\/",0);
		Double mostlow = ev;
		if(str.length == 2){
			comment = str[0] + el + str[1];
		}
		if(elementnote.containsKey(pre)){
			String come = elementnote.get(pre);
			comment += "\n";

			comment += come;
			mostlow = elementev.get(pre);
			if(mostlow.compareTo(ev) == 1){
				mostlow = ev;
			}

		}
		elementnote.put(pre, comment);
		elementev.put(pre, mostlow);
	}

	public void setnote()
		throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
		for(IPresentation pre : elementnote.keySet()){
			String pretype = pre.getType();
			String notecomment = elementnote.get(pre);
			IDiagram dgm = pre.getDiagram();
			Double pointx = 0.0;
			if(diagramlocation.containsKey(dgm)){
				pointx = diagramlocation.get(dgm);
			}
			Point2D point = ((INodePresentation)pre).getLocation();
			Double pointy = point.getY();
			if(pretype.equals("Frame")){
				dr.createNotePresentation(dgm, notecomment, new Point2D.Double(pointx,pointy) , elementev.get(pre));
			}
			else{
				dr.createNoteAnchorPresentation(dgm, pre , notecomment, new Point2D.Double(pointx,pointy) , elementev.get(pre));
			}
		}
	}

	Map<IDiagram , Double> diagramlocation
	 = new HashMap<IDiagram , Double>();

	public void diagramLocation(){
		try{
			AstahAPI api = AstahAPI.getAstahAPI();
			ProjectAccessor projectAccessor = api.getProjectAccessor();
			IModel iCurrentProject = projectAccessor.getProject();
			IDiagram[] dgms = ((INamedElement)iCurrentProject).getDiagrams();
			for(IDiagram dgm : dgms){
				Double loc = 0.0;
				IPresentation[] pres = dgm.getPresentations();
				for(IPresentation pre : pres){
					if(!((pre.getType().equals("ControlFlow/ObjectFlow") || pre.getType().equals("Decision Node & Merge Node")))){
					//if(pre.getType().equals("Class") || pre.getType().equals("Action") || pre.getType().equals("Partition")){
						Double pointx = ((INodePresentation)pre).getLocation().getX();
						if(pointx.compareTo(loc) == 1){
							loc = pointx;
						}
					}
				}
				if(loc != 0.0){
					loc += 200;
				}
				diagramlocation.put(dgm , loc);
			}
		} catch(Exception e){
			System.out.println("diagramLocation");
			System.out.println(e);
		}
	}


}