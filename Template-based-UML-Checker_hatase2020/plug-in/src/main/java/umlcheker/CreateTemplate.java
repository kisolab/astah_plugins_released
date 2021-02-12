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

public class CreateTemplate{
	
	DiagramReflection dr = new DiagramReflection();
	NodeGenerater ng = new NodeGenerater();
	Dialog dia = new Dialog();
	ChangedSliceColor csc = new ChangedSliceColor();

	public void createTemplate()
		  throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
	  INodePresentation nps = null;
	  ILinkPresentation lps = null;
	  ITaggedValue tvs = null;
	  ActivityDiagramEditor ade = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getActivityDiagramEditor();
	  BasicModelEditor bme = AstahAPI.getAstahAPI().getProjectAccessor().getModelEditorFactory().getBasicModelEditor();
	  ERDiagramEditor ere = ProjectAccessorFactory.getProjectAccessor().getDiagramEditorFactory().getERDiagramEditor();
	  try{
		AstahAPI api = AstahAPI.getAstahAPI();
		IDiagramViewManager iDVM = api.getViewManager().getDiagramViewManager();
		Double sourcex = 0.0;
		Double sourcey = 0.0;
		Double targetx = 0.0;
		Double targety = 0.0;
		IDiagram dgm = Activator.actdgm;
		ade.setDiagram(dgm);
		IPresentation[] selectpres = Activator.actpre;
		if(selectpres.length == 1){
			IFlow se1 = (IFlow)(selectpres[0].getModel());
			boolean probflag = false;
			for(IActivityDiagram slicedgm : UMLcheck.slicematchact.keySet()){
				System.out.print(slicedgm);
				if(slicedgm == dgm){
					Map<String , Map<IFlow , IFlow>> sff = UMLcheck.slicematchact.get(slicedgm);	
					for(String slicename : sff.keySet()){
						Map<IFlow , IFlow> sfm = sff.get(slicename);
						for(IFlow sf1 : sfm.keySet()){
							IFlow sf2 = sfm.get(sf1);
							if(se1.getId().equals(sf1.getId()) || se1.getId().equals(sf2.getId())){
								probflag = true;
								break;
							}
						}
						if(probflag){
							break;
						}
					}
				}
			}
			if(probflag){
				IPresentation selectpre = Activator.actpre[0];
				IElement selectele = selectpre.getModel();
				if(selectpre.getType().equals("ControlFlow/ObjectFlow")){
					IPresentation sourcepre = ((ILinkPresentation)selectpre).getSourceEnd();
					INodePresentation seleparent = ((INodePresentation)sourcepre).getParent();
					IPresentation targetpre = ((ILinkPresentation)selectpre).getTargetEnd();
					Point2D sourcelocat = ((INodePresentation)sourcepre).getLocation();
					sourcex = sourcelocat.getX() + ((INodePresentation)sourcepre).getWidth();
					sourcey = sourcelocat.getY() + ((INodePresentation)sourcepre).getHeight();
					targetx = ((INodePresentation)targetpre).getLocation().getX();
					targety = ((INodePresentation)targetpre).getLocation().getY();
					//--------------------------------------------------------
					boolean initialflag = false;
					boolean finalflag = false;
					Map<IPresentation , IPresentation> nodematch = new HashMap<IPresentation , IPresentation>();
					Map<IPresentation , ILinkPresentation> flowmatch = new HashMap<IPresentation , ILinkPresentation>();
					Double tx = 0.0;
					Double ty = 0.0;
					Point2D inilocat = new Point2D.Double(0.0,0.0);
					Point2D finlocat = new Point2D.Double(0.0,0.0);
					//---------------------------------------------------------
					IDiagram indgm = iDVM.getCurrentDiagram();
					System.out.println(indgm + " insert");
					IPresentation ipres[] = indgm.getPresentations();
					IPresentation initialpre = null;
					//------------flag catch-------------------------------------
					for(IPresentation ipre : ipres){
						iDVM.unselectAll();
						IElement ele = ipre.getModel();
						String nodetype = ipre.getType();
						if(nodetype.equals("InitialNode") || nodetype.equals("ActivityFinal")){
							if(ele.getTaggedValues().length > 0){
								ITaggedValue eletag[] = ele.getTaggedValues();
								for(ITaggedValue tag : eletag){
									String tagkey = tag.getKey();
									String tagvalue = tag.getValue();
									//System.out.println(tagkey + " : "  + tagvalue);
									if(tagkey.equals("Initial") && tagvalue.equals("1")){
										initialflag = true;
										inilocat = ((INodePresentation)ipre).getLocation();
										nodematch.put(ipre , sourcepre);
										initialpre = ipre;

									}
									else if(tagkey.equals("Final") && tagvalue.equals("1")){
										finalflag = true;
										IElement finele = ipre.getModel();
										IActivityNode lastnode = ((IActivityNode)finele).getIncomings()[0].getSource();
										INodePresentation laspre = (INodePresentation)lastnode.getPresentations()[0];
										finlocat.setLocation(laspre.getLocation().getX() + laspre.getWidth() , 
																laspre.getLocation().getY() + laspre.getHeight());
										/*
										finlocat = ((INodePresentation)ipre).getLocation();
										*/
										nodematch.put(ipre , targetpre);
									}
								}
								if(initialflag && finalflag){
									tx = finlocat.getX() - inilocat.getX();
									ty = finlocat.getY() - inilocat.getY();
								}
							}
						}
					}
					//----------------shift node-----------------------------------
					if(initialflag && finalflag){
						TransactionManager.beginTransaction();
						//-------
						Double temprangex = sourcex + tx;
						Double temprangey = sourcey + ty;
						INodePresentation horizonpar = null;
						INodePresentation varticalpar = null;
						INodePresentation[] seleparents = ((INodePresentation)sourcepre).getParents();
						for(INodePresentation selepa : seleparents){
							IElement parti = selepa.getModel();
							if(!((IPartition)parti).isHorizontal()){
								Point2D vartilocation = selepa.getLocation();
								Double partilimit = vartilocation.getX() + selepa.getWidth();
								varticalpar = selepa;
								if(temprangex > partilimit){
									selepa.setWidth(selepa.getWidth() + tx);
								}
							}
							else{
								horizonpar = selepa;
							}
						}
						Double horilimit = horizonpar.getLocation().getY() + horizonpar.getHeight();
						List<INodePresentation> shiftlist = new ArrayList<INodePresentation>();
						/*
						if((targetx - sourcex) > tx && (targety - sourcey) > ty){
							//
						}else{
						*/
							for(IPresentation ipre : dgm.getPresentations()){
								String nodetype = ipre.getType();
								if(! (nodetype.equals("Frame") || nodetype.equals("Partition") || 
										nodetype.equals("ControlFlow/ObjectFlow") || nodetype.equals("Dependency") || nodetype.equals("Comment"))){
									Point2D locat = ((INodePresentation)ipre).getLocation();
									if(horilimit > locat.getY()){
										if(sourcey <= locat.getY() && varticalpar.getLocation().getX() <= locat.getX()){
											shiftlist.add((INodePresentation)ipre);
										}
									}
								}
							}
							horizonpar.setHeight(horizonpar.getHeight() + ty);
						//}
						if(shiftlist.size() > 0){
							for(INodePresentation nodepre : shiftlist){
								Point2D newlocat = new Point2D.Double(0.0,0.0);
								newlocat.setLocation(nodepre.getLocation().getX() , nodepre.getLocation().getY() + ty);
								nodepre.setLocation(newlocat);
							}
						}
						//TransactionManager.endTransaction();
						//-----------------create node-------------------------
						//TransactionManager.beginTransaction();
						for(IPresentation ipre : ipres){
							IElement ele = ipre.getModel();
							String nodetype = ipre.getType();
							if(nodetype.equals("Action") || nodetype.equals("Connector")
									|| nodetype.equals("SendSignalAction") || nodetype.equals("AcceptEventAction")){
								INodePresentation nodepre = (INodePresentation)ipre;
								Point2D locat = nodepre.getLocation();
								Point2D addlocat = new Point2D.Double(locat.getX() + sourcex , locat.getY() + sourcey);
								INodePresentation crpre = null;
								String elename = ((INamedElement)ele).getName();
								if(nodetype.equals("Action")){
									if(!(elename.isEmpty())){
										crpre = ade.createAction(elename,addlocat);
									}
									else{
										crpre = ade.createAction(" ",addlocat);
									}
								}
								else if(nodetype.equals("Connector")){
									if(!(elename.isEmpty())){
										crpre = ade.createConnector(elename,addlocat);
									}
									else{
										crpre = ade.createConnector(" ",addlocat);
									}
								}
								else if(nodetype.equals("SendSignalAction")){
									crpre = ade.createSendSignalAction(elename,addlocat);
								}
								else if(nodetype.equals("AcceptEventAction")){
									crpre = ade.createAcceptEventAction(elename,addlocat);
								}
								nodematch.put(ipre, (IPresentation)crpre);
							}
							else if(nodetype.equals("InitialNode") || nodetype.equals("ActivityFinal")){
								if(ele.getTaggedValues().length == 0){
									INodePresentation nodepre = (INodePresentation)ipre;
									Point2D locat = nodepre.getLocation();	
									Point2D addlocat = new Point2D.Double(locat.getX() + sourcex , locat.getY() + sourcey);
									if(nodetype.equals("InitialNode")){
										INodePresentation crpre = ade.createInitialNode(" ",addlocat);
										nodematch.put(ipre, (IPresentation)crpre);
									}
									else{
										INodePresentation crpre = ade.createFinalNode(" ",addlocat);
										nodematch.put(ipre, (IPresentation)crpre);
									}
								}
							}
							else if(nodetype.equals("Decision Node & Merge Node")){
								INodePresentation nodepre = (INodePresentation)ipre;
								Point2D locat = nodepre.getLocation();
								Point2D addlocat = new Point2D.Double(locat.getX() + sourcex , locat.getY() + sourcey);
								INodePresentation crpre = ade.createDecisionMergeNode(seleparent,addlocat);
								nodematch.put(ipre,(IPresentation)crpre);
								String colorvalue = nodepre.getProperty("fill.color"); 
								crpre.setProperty("fill.color" , colorvalue);
							}
						}
						//System.out.println("create template");
						TransactionManager.endTransaction();
						//-------------set width & height & stereotype-----
						TransactionManager.beginTransaction();
						if(shiftlist.size() > 0){
							for(INodePresentation nodepre : shiftlist){
								IFlow[] shiftflows = ((IActivityNode)(nodepre.getModel())).getOutgoings();
								if(shiftflows.length > 0){
									for(IFlow shiftflow : shiftflows){
										ILinkPresentation shiftlink = (ILinkPresentation)(shiftflow.getPresentations()[0]);
										Point2D[] shiftpoints = shiftlink.getAllPoints();
										if(shiftpoints.length > 2){
											for(int i = 0; i < shiftpoints.length; i++){
												Double pointy = shiftpoints[i].getY();
												if(i > 0 && i < shiftpoints.length - 1){
													pointy += ty;
												}
												shiftpoints[i].setLocation(shiftpoints[i].getX(),pointy);
											}
											shiftlink.setAllPoints(shiftpoints);
										}
									}
								}
							}
						}
						for(IPresentation ipre : nodematch.keySet()){
							INodePresentation inodepre = (INodePresentation)ipre;
							INodePresentation crpre = (INodePresentation)(nodematch.get(ipre));
							IElement ele = ipre.getModel();
							IElement crele = crpre.getModel();
							if(ipre.getType().equals("Action") || ipre.getType().equals("SendSignalAction")
									|| ipre.getType().equals("AcceptEventAction")){
								Double nodeWidth = inodepre.getWidth();
								Double nodeHeight = inodepre.getHeight();
								crpre.setWidth(nodeWidth);
								crpre.setHeight(nodeHeight);
								if(ele.getStereotypes().length > 0){
									for(String str : ele.getStereotypes()){
										crele.addStereotype(str);
									}
								}
							}
							else if(ipre.getType().equals("Decision Node & Merge Node")){
								if(!(((INamedElement)ele).getDefinition().isEmpty())){
									((INamedElement)crele).setDefinition(((INamedElement)ele).getDefinition());
								}
							}
							//------color-------------
						}
						//TransactionManager.endTransaction();
						//--------------link---------------------
						//TransactionManager.beginTransaction();
						ILinkPresentation selectlps = null;
						for(IPresentation ipre : ipres){
							String nodetype = ipre.getType();
							IElement ele = ipre.getModel();
							if(nodetype.equals("ControlFlow/ObjectFlow") || nodetype.equals("Dependency")){
								IPresentation bspre = ((ILinkPresentation)ipre).getSourceEnd();
								IPresentation btpre = ((ILinkPresentation)ipre).getTargetEnd();
								INodePresentation aspre = (INodePresentation)(nodematch.get(bspre));
								INodePresentation atpre = (INodePresentation)(nodematch.get(btpre));
								if(nodetype.equals("ControlFlow/ObjectFlow")){
									lps = ade.createFlow(aspre , atpre);
									flowmatch.put(ipre , lps);
									if(bspre == initialpre){
										selectlps = lps;
									}
								}
								else if(nodetype.equals("Dependency")){
									lps = ade.createDependency(" ",atpre , aspre);
								}
							}
						}
						TransactionManager.endTransaction();
						//---------------set flow---------------
						TransactionManager.beginTransaction();
						boolean initagflag = false;
						ITaggedValue initag = null;
						IElement fintagele = null;
						for(Map.Entry<IPresentation , ILinkPresentation> entry : flowmatch.entrySet()){
							ILinkPresentation bflink = (ILinkPresentation)(entry.getKey());
							ILinkPresentation aflink = entry.getValue();
							IFlow bfflow = (IFlow)(entry.getKey().getModel());
							IFlow afflow = (IFlow)(entry.getValue().getModel());
							IElement bfele = (IElement)bfflow;
							IElement afele = (IElement)afflow;
							if(!(bflink.getSourceEnd().getType().equals("InitialNode") || bflink.getTargetEnd().getType().equals("ActivityFinal"))){
								if(bflink.getAllPoints().length > 2){
									Point2D[] bflocats = bflink.getAllPoints();
									Point2D[] aflocats = aflink.getAllPoints();
									Double shiftx = aflocats[0].getX() - bflocats[0].getX();
									Double shifty = aflocats[0].getY() - bflocats[0].getY();
									Point2D[] shiftlocats = new Point2D[bflink.getAllPoints().length];
									shiftlocats[0] = aflocats[0];
									shiftlocats[bflink.getAllPoints().length - 1] = aflocats[aflocats.length - 1];
									for(int i = 1; i < bflink.getAllPoints().length - 1; i++){
										Point2D shiftlocat = new Point2D.Double(bflocats[i].getX() + shiftx,bflocats[i].getY() + shifty);
										shiftlocats[i] = shiftlocat;
									}
									aflink.setAllPoints(shiftlocats);
								}
							}
							if(!(bfflow.getGuard().isEmpty())){
								afflow.setGuard(bfflow.getGuard());
							}
							if(bfele.getTaggedValues().length > 0){
								for(ITaggedValue bftag : bfele.getTaggedValues()){
									ITaggedValue aftag = bme.createTaggedValue(afele,bftag.getKey(),bftag.getValue());
									String tagkey = bftag.getKey();
									String[] splittag = tagkey.split("-", 0);
									if(splittag.length == 3){
										if(splittag[2].equals("flag")){
											if(splittag[1].equals("Final")){
												fintagele = afele;
											}
										}
									}
								}
							}
							if(entry.getValue() == selectlps){
								IFlow selectflow = (IFlow)selectele; 
								if(!(selectflow.getGuard().isEmpty())){
									afflow.setGuard(selectflow.getGuard());
								}
								if(selectele.getTaggedValues().length > 0){
									for(ITaggedValue bftag : selectele.getTaggedValues()){
										String tagkey = bftag.getKey();
										String[] splittag = tagkey.split("-", 0);
										if(splittag.length == 3){
											if(splittag[2].equals("flag")){
												if(splittag[1].equals("Initial")){
													initagflag = true;
													initag = bftag;
													continue;
												}
											}
										}
										ITaggedValue aftag = bme.createTaggedValue(afele,bftag.getKey(),bftag.getValue());
									}
								}
							}
							/*
							if(((ILinkPresentation)bfflow).getAllPoints().length >= 3){
								Point2D[] rectanglelocats = ((ILinkPresentation)bfflow).getAllPoints();
							}
							*/
						}
						if(initagflag){
							ITaggedValue aftag = bme.createTaggedValue(fintagele,initag.getKey(),initag.getValue());
						}
						//System.out.println("link node");
						ere.setDiagram(dgm);
						ere.deletePresentation(selectpre);
						TransactionManager.endTransaction();
					}
					else{
						dia.warningdialog("Unable to insert non-template diagram");
					}
				}
				else{
					dia.warningdialog("Unable to insert into non-flow elements");
				}
			}
			else{
				dia.warningdialog("Unable to insert into this flow");
			}
		}
		//------type 2---------------------
		else if(selectpres.length == 2){
			IFlow se1 = (IFlow)(selectpres[0].getModel());
			IFlow se2 = (IFlow)(selectpres[1].getModel());
			boolean probflag = false;
			for(String slicename : UMLcheck.slicematch.keySet()){
				Map<IFlow , IFlow> sfm = UMLcheck.slicematch.get(slicename);
				for(IFlow sf1 : sfm.keySet()){
					IFlow sf2 = sfm.get(sf1);
					if((se1.getId().equals(sf1.getId()) && se2.getId().equals(sf2.getId())) || 
						    (se1.getId().equals(sf2.getId()) && se2.getId().equals(sf1.getId()))){
						probflag = true;
						break;
					}
				}
				if(probflag){
					break;
				}
			}
			if(!(probflag)){
				csc.getdepthmatch(se1);
				if(ChangedSliceColor.depthmatchlist.size() > 0){
					System.out.print(se2);
					System.out.print(ChangedSliceColor.depthmatchlist.size() + "\n");
					for(IFlow dmflow : ChangedSliceColor.depthmatchlist){
						System.out.print(dmflow);
						if(dmflow.getId().equals(se2.getId())){
							probflag = true;
							break;
						}
					}
				}
			}
			if(probflag){
				int insertflag = 0;
				IDiagram indgm = iDVM.getCurrentDiagram();
				INodePresentation crconnector = null;
				if(((IElement)indgm).getTaggedValues().length > 0){
					ITaggedValue[] tags = ((IElement)indgm).getTaggedValues();
					//-------catch temp------
					for(ITaggedValue tag : tags){
						String tagkey = tag.getKey();
						String tagvalue = tag.getValue();
						if(tagkey.equals("loopflag") && tagvalue.equals("1")){
							insertflag = 1;
							break;
						}
						else if(tagkey.equals("loopflag") && tagvalue.equals("2")){
							insertflag = 2;
							break;
						}
						else if(tagkey.equals("labelbreakflag") && tagvalue.equals("1")){
							insertflag = 3;
							break;
						}
						else{
							dia.warningdialog("Unable to insert this diagram");
						}
					}
					//-------create node-----
					TransactionManager.beginTransaction();
					ArrayList<INodePresentation> crlist = new ArrayList<INodePresentation>();
					for(IPresentation selectpre : selectpres){
						if(selectpre.getType().equals("ControlFlow/ObjectFlow")){
							IPresentation sourcepre = ((ILinkPresentation)selectpre).getSourceEnd();
							IPresentation targetpre = ((ILinkPresentation)selectpre).getTargetEnd();
							INodePresentation seleparent = ((INodePresentation)sourcepre).getParent();
							Point2D[] linkpoint = ((ILinkPresentation)selectpre).getAllPoints();
							Double addx = (linkpoint[linkpoint.length-1].getX() + linkpoint[linkpoint.length - 2].getX())/2 - 15;
							Double addy = (linkpoint[linkpoint.length-1].getY() + linkpoint[linkpoint.length - 2].getY())/2 - 10;
							Point2D addlocat = new Point2D.Double(addx,addy);
							INodePresentation crpre = ade.createDecisionMergeNode(seleparent,addlocat);
							String colorvalue = "#FFCC33";
							if(insertflag == 1 && crlist.size() == 1){						
								crpre.setProperty("fill.color" , colorvalue);
							}
							else if(insertflag == 2 && crlist.size() == 0){
								crpre.setProperty("fill.color" , colorvalue);
							}
							else if(insertflag == 3 && crlist.size() == 0){
								crpre.setProperty("fill.color" , "#99FF66");
								Double cox = (linkpoint[linkpoint.length-1].getX() + linkpoint[linkpoint.length - 2].getX())/2 + 100;
								Double coy = (linkpoint[linkpoint.length-1].getY() + linkpoint[linkpoint.length - 2].getY())/2 - 10;
								Point2D colocat = new Point2D.Double(cox,coy);
								crconnector = ade.createConnector("label",colocat);
							}
							crlist.add((INodePresentation)crpre);
						}
						else{
							dia.warningdialog("Unable to insert into non-flow elements");
							break;
						}
					}
					TransactionManager.endTransaction();
					//------create flow------
					TransactionManager.beginTransaction();
					int i = 0;
					ArrayList<ILinkPresentation> flowlist = new ArrayList<ILinkPresentation>();
					for(IPresentation selectpre : selectpres){
						IPresentation sourcepre = ((ILinkPresentation)selectpre).getSourceEnd();
						IPresentation targetpre = ((ILinkPresentation)selectpre).getTargetEnd();
						lps = ade.createFlow((INodePresentation)sourcepre,crlist.get(i));
						flowlist.add(lps);
						if(insertflag == 2 && i == 1){
							lps = ade.createFlow(crlist.get(i - 1),(INodePresentation)targetpre);
						}
						else{
							lps = ade.createFlow(crlist.get(i),(INodePresentation)targetpre);
						}
						flowlist.add(lps);
						i++;
					}
					if(insertflag < 3){
						lps = ade.createFlow(crlist.get(1),crlist.get(0));
						flowlist.add(lps);
					}
					else{
						for(INodePresentation dec : crlist){
							lps = ade.createDependency(" ",crconnector , dec);
						}
					}
					TransactionManager.endTransaction();
					//---------set flow--------
					TransactionManager.beginTransaction();
					int j = 0;
					if(insertflag < 3){
						for(IPresentation selectpre : selectpres){
							IFlow selectflow = (IFlow)(selectpre.getModel());
							if(!(selectflow.getGuard().isEmpty())){
								if(j == 0){
									((IFlow)(flowlist.get(0).getModel())).setGuard(selectflow.getGuard());
								}
								else{
									((IFlow)(flowlist.get(2).getModel())).setGuard(selectflow.getGuard());
								}
							}
							//ere.setDiagram(dgm);
							//ere.deletePresentation(selectpre);
							j++;
						}
						((IFlow)(flowlist.get(3).getModel())).setGuard("false");
						setFlowPoints(flowlist.get(4), 100);
						if(insertflag == 1){
							((IFlow)(flowlist.get(4).getModel())).setGuard("true");
						}
						else if(insertflag == 2){
							((IFlow)(flowlist.get(1).getModel())).setGuard("true");
							setFlowPoints(flowlist.get(3), -100);
						}
					}
					ere.setDiagram(dgm);
					ere.deletePresentation(selectpres[0]);
					ere.deletePresentation(selectpres[1]);
					TransactionManager.endTransaction();
					System.out.println(indgm + " insert");
					//-------------------------------------------------------
				}
				else{
					dia.warningdialog("Unable to insert this diagram");
				}
			}
			else{
				dia.warningdialog("Unable to insert into these flows");
				
			}
		}
		else{
			dia.warningdialog("Unable to insert into two or more elements");
		}
	  //----------------------------------------------
	  /*
	  }catch(InvalidEditingException e){
		if(e.getKey().equals("outOfContainer")){
			dia.warningdialog("There are enough space to insert template");
		}
		TransactionManager.abortTransaction();
	  */
	  }catch(Exception e){
		System.out.println(e);
		TransactionManager.abortTransaction();
	  }
	}

	private void setFlowPoints(ILinkPresentation flow, int shiftsize)
			throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
		Point2D[] allpoints = flow.getAllPoints();
		Point2D point1 = new Point2D.Double(allpoints[0].getX() - shiftsize , allpoints[0].getY());
		Point2D point2 = new Point2D.Double(allpoints[0].getX() - shiftsize , allpoints[1].getY());
		Point2D[] setpoints = new Point2D[4];
		setpoints[0] = allpoints[0];
		setpoints[1] = point1;
		setpoints[2] = point2;
		setpoints[3] = allpoints[1];
		flow.setAllPoints(setpoints);
	}
}