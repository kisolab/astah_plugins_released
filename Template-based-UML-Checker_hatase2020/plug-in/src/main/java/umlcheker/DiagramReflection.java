package umlcheker;

import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.Map.Entry;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.view.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.exception.*;


import com.change_vision.jude.api.inf.project.ProjectAccessorFactory;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;

public class DiagramReflection extends UMLcheck{
	public void DiagramReflection(){
	}

	ArrayList<String> notelist = new ArrayList<String>();
	HashMap<String, Map<String,String>> colormap
	 = new HashMap<String, Map<String,String>>();

	public ILinkPresentation createNoteAnchorPresentation(IDiagram dgm, IPresentation ip , String note, Point2D location , Double dv)
		throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
			ILinkPresentation ps = null;
			ClassDiagramEditor cde = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getClassDiagramEditor();
			try{
				TransactionManager.beginTransaction();
				cde.setDiagram(dgm);
				INodePresentation cn = cde.createNote(note , location);
				notelist.add(cn.getID());
				Serialize sl = new Serialize();
				sl.seri(notelist);
				//notecolor(cn , dv);
				ps = cde.createNoteAnchor(cn, ip);
				TransactionManager.endTransaction();
			}
			catch(Exception e){
				System.out.println(e);
				TransactionManager.abortTransaction();
			}
			return ps;
	}

	public INodePresentation createNotePresentation(IDiagram dgm, String note, Point2D location , Double ev)
		throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
			INodePresentation ps = null;
			ClassDiagramEditor cde = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getClassDiagramEditor();
			try{
				TransactionManager.beginTransaction();
				cde.setDiagram(dgm);
				INodePresentation cn = cde.createNote(note , location);
				notelist.add(cn.getID());
				Serialize sl = new Serialize();
				sl.seri(notelist);
				//notecolor(cn , ev);
				TransactionManager.endTransaction();
			}
			catch(Exception e){
				System.out.println("createNoteAnchorPresentation failed");
				System.out.println(e);
				TransactionManager.abortTransaction();
			}
			return ps;
	}

	//default color #FFFFCC
	public void changecolor(IPresentation ipr , String colorvalue){
		try{
			TransactionManager.beginTransaction();
			//System.out.println(ipr);
			ipr.setProperty("fill.color" , colorvalue);
			TransactionManager.endTransaction();
			}
		catch(Exception e){
			System.out.println("changecolor failed");
			System.out.println(e);
			TransactionManager.abortTransaction();
		}
	}

	public void reflectionEvaluation(Map<String , Map<IElementSt , Double>> evMap){
	  try{
		EachNote en = new EachNote();
		en.diagramLocation();
	    AstahAPI api = AstahAPI.getAstahAPI();
		ProjectAccessor projectAccessor = api.getProjectAccessor();
		IModel iCurrentProject = projectAccessor.getProject();
		INamedElement element = iCurrentProject;
		for(String id : evMap.keySet()){
			Map<IElementSt , Double> evMapval = evMap.get(id);
			for(IElementSt ele : evMapval.keySet()){
				Double ev = evMapval.get(ele);
				if(ev != 0){
					String elementname = ele.iElementName;
					Class elementclass = ele.iElementType;
					String elementstr = elementclass.getName();
					String ecn = "";
					List<INamedElement> ines = new ArrayList<INamedElement>();
					System.out.println(elementname +" : "+ elementstr);
					switch(elementstr){
					case "wf": 
						ecn = "IAttribute";
						ines = Arrays.asList(projectAccessor.findElements(IAttribute.class , elementname));
						break;
					case "xC": 
						ecn = "IPattition";
						ines = Arrays.asList(projectAccessor.findElements(IPartition.class , elementname));
						break;
					case "wi": 
						ecn = "IClass";
						ines = Arrays.asList(projectAccessor.findElements(IClass.class , elementname));
						break;
					case "vW": 
						ecn = "IActivityNode";
						ines = Arrays.asList(projectAccessor.findElements(IActivityNode.class , elementname));
						break;
					case "wD":
						ecn = "IControlNode";
						ines = Arrays.asList(projectAccessor.findElements(IControlNode.class , elementname));
						break;
					case "xn": 
						ecn = "IOperation";
						ines = Arrays.asList(projectAccessor.findElements(IOperation.class , elementname));
						break;
					case "xq": 
						ecn = "IParameter";
						ines = Arrays.asList(projectAccessor.findElements(IParameter.class , elementname));
						break;
					case "wR": 
						ecn = "IFlow";
						ines = Arrays.asList(projectAccessor.findElements(IFlow.class , elementname));
						break;
					case "vY": 
						ecn = "IActivity";
						ines = Arrays.asList(projectAccessor.findElements(IActivity.class , elementname));
						break;
					case "wh":
						ecn = "IClassDiagram";
						ines = Arrays.asList(projectAccessor.findElements(IClassDiagram.class , elementname));
						break;
					case "ws":
						ecn = "IControlNode";
						ines = Arrays.asList(projectAccessor.findElements(IControlNode.class , elementname));
						break;
					case "xh":
						ecn = "IInputPin";
						ines = Arrays.asList(projectAccessor.findElements(IInputPin.class , elementname));
						break;
					default:
						//
					}
					if(ines.size() != 0){
						String[] nid = id.split("\\(",0);
						for(INamedElement ine : ines){
							String eid = ine.getId();
							if(nid[0].equals(eid)){
								//System.out.println("[" + ev + "]" + " : " + ecn + " : " + ine);
								IElement el = ine;
								if(elementstr.equals("wR")){
									if(nid[1].equals("inFlow")){
										el = (IElement)((IFlow)(ine)).getTarget();
										}
									else if(nid[1].equals("outFlow")){
										el = (IElement)((IFlow)(ine)).getSource();
									}
								}
								else if(elementstr.equals("wi") || elementstr.equals("xn") || elementstr.equals("xq")){
									if(ine.getContainer().getClass().getName() != "xi"){
										el = ine.getContainer();
									}
								}
								else if(elementstr.equals("vY")){
									el = (IElement)((IActivity)(ine)).getActivityDiagram();
								}
								IPresentation pre = el.getPresentations()[0];
								//changecolor(pre,"#FF0000");
								en.eachnote(pre , ev , ine);
								if(elementstr.equals("xr")||elementstr.equals("vW")||
									elementstr.equals("wR")||elementstr.equals("ws")){
									//colormap.put(eid , new HashMap<String , String>());
									//colormap.get(eid).put(elementname,elementclass.getName());
								}
							}
						}
					}
					else{
						System.out.println("findElements failed");
					}
				}
			}
		}
		en.setnote();
	  }
	  catch(Exception e){
	  	  System.out.println("reflectionEvaluation failed");
		  System.out.println(e);
		  e.printStackTrace();
	  }
	}

	public void deletePre (IDiagram dgm, IPresentation ps)
		throws InvalidEditingException , ClassNotFoundException , InvalidUsingException{
			ERDiagramEditor editor = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getERDiagramEditor();
			try{
				TransactionManager.beginTransaction();
				editor.setDiagram(dgm);
				editor.deletePresentation(ps);
				TransactionManager.endTransaction();
			}
			catch(Exception e){
				System.out.println("deletePresentation failed");
				System.out.println(e);
			}
	}

	public void setdel(ArrayList<String> delnotelist){
		//throws
		ArrayList<IComment> comentList = new ArrayList<IComment>();
		try{
			AstahAPI api = AstahAPI.getAstahAPI();
			ProjectAccessor projectAccessor = api.getProjectAccessor();
			IModel iCurrentProject = projectAccessor.getProject();
			INamedElement element = iCurrentProject;
			IDiagram[] dgms = element.getDiagrams();
			for(IDiagram dgm : dgms){
				IComment[] comes = dgm.getComments();
				for(IComment come : comes){
					comentList.add(come);	
				}
			}
			for(IComment come : comentList){
				IPresentation comepre = come.getPresentations()[0];
				String comeid = comepre.getID();
				if(delnotelist.contains(comeid)){
					deletePre(comepre.getDiagram(),comepre);
				}
			}
		}
		catch(Exception e){
			System.out.println("setdele failed");
			System.out.println(e);
		}
	}

	public void recolor(HashMap<String, HashMap<String, String>> delcolor){
		try{
			AstahAPI api = AstahAPI.getAstahAPI();
			ProjectAccessor projectAccessor = api.getProjectAccessor();
			IModel iCurrentProject = projectAccessor.getProject();
			for(String id : delcolor.keySet()){
				HashMap<String , String> dcvaluemap = delcolor.get(id);
				for(String elementname : dcvaluemap.keySet()){
					String classname = dcvaluemap.get(elementname);
					List<INamedElement> eles = new ArrayList<INamedElement>();
					switch(classname){
					case "vW":
						eles = Arrays.asList(projectAccessor.findElements(IActivityNode.class , elementname));
						break;
					case "xr":
						eles = Arrays.asList(projectAccessor.findElements(IPartition.class , elementname));
						break;
					case "ws":
						eles = Arrays.asList(projectAccessor.findElements(IControlNode.class , elementname));
						break;
					default:
						//
					}
					for(INamedElement ele : eles){
						if((ele.getId()).equals(id)){
							IPresentation ipr = ele.getPresentations()[0];
							changecolor(ipr , "#FFFFCC");
						}
					}
				}
			}
		}
		catch(Exception e){
			System.out.println("recolor failed");
			System.out.println(e);
		}
	}

	private void notecolor(INodePresentation notepre , Double dv){
		try{
			IElement el = notepre.getModel();
			String sv = dv.toString().substring(0,1);
			String stereo = "";
			if(sv.equals("1")){
				stereo = "Critical";
			}
			else if(sv.equals("2")){
				stereo = "Warning";
			}
			else{
				stereo = "Comment";
			}
			el.addStereotype(stereo);
		}
		catch(Exception e){
			System.out.println("notecolor failed");
			System.out.println(e);
		}
	}

	


}