package jp.swest.ledcamp.generator;

import java.util.*;
import javax.swing.JOptionPane;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.view.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.exception.*;
import com.change_vision.jude.api.inf.ui.*;

import javax.swing.JOptionPane;

public class Generatecheck{
	
	IActivityDiagram nowprobdgm;
	List<IClass> iClassList = new ArrayList<IClass>();
	List<IActivity> iActivityList = new ArrayList<IActivity>();
	public static Map<String , Map<IFlow , IFlow>> slicematch;
	public static Map<IActivityDiagram , Map<String , Map<IFlow , IFlow>>> slicematchact
	 = new LinkedHashMap<IActivityDiagram , Map<String , Map<IFlow , IFlow>>>();
	List<String> slicelist;
	int slicedepth;
	List<Integer> slicedepthlist;
	public static Map<IActivityDiagram , List<String>> slicelistdgm
	 = new HashMap<IActivityDiagram , List<String>>();
	public static Map<IActivityDiagram , List<Integer>> slicedepthlistdgm
	 = new HashMap<IActivityDiagram , List<Integer>>();
	List<IActivityNode> problist;
	public static List<String> typelist;
	public static Map<IActivityDiagram , List<IActivityNode>> problistmap
	 = new HashMap<IActivityDiagram , List<IActivityNode>>();
	public static Map<IActivityDiagram , Map<ArrayList<ArrayList<IPartition>> , List<IActivityNode>>> partitionmap
	 = new HashMap<IActivityDiagram , Map<ArrayList<ArrayList<IPartition>> , List<IActivityNode>>>();

  public void umlcheckaction(){
	try {
	  AstahAPI api = AstahAPI.getAstahAPI();
      ProjectAccessor projectAccessor = api.getProjectAccessor();
	  IModel iCurrentProject = projectAccessor.getProject();
	  INamedElement element = iCurrentProject;
	  INamedElement[] iNamedElements = iCurrentProject.getOwnedElements();
	  INamedElement projectele = null;
	  for(INamedElement ele : iNamedElements){
		String str = ele.getName();
		if(str.equals("project") || str.equals("Project")){
			projectele = ele;
			break;
		}
	  }
	  getAstahDiagrams(projectele);

	  /*
	  cross check
	  */

	  //--------ActivityDiagramStructre-------
	  for(IActivity iActivity : iActivityList){
		System.out.println("---" + iActivity + "---");
		IActivityDiagram nowdgm = iActivity.getActivityDiagram();
		nowprobdgm = nowdgm;
		//if(iActivity.getCallBehaviorActions().length == 0){
			IActivityNode[] iNodes = iActivity.getActivityNodes();
			/*
			if(checkFlow(iNodes) == false){
				System.out.println("bad");
			}
			*/
			for(IActivityNode iNode : iNodes){
				nodeflag.put(iNode , false);
			}
			for(IActivityNode iActivityNode : iNodes){
				if(iActivityNode instanceof IControlNode){
					IControlNode iControlNode = (IControlNode)iActivityNode;
					if(iControlNode.isInitialNode()){
						if(iControlNode.getSupplierDependencies().length == 0){
							nstack.push(iActivityNode);
							setvariableinitialize(iActivityNode);
							probnextnode();
							setvariablepost(nowdgm);
						}
					}
				}
				else{
					//System.out.println("action:" + iActivityNode);
				}
			}
			//check
		//}
	  }
	  

	  //--------------------------------
	  
	  /*
	  IAttribute = wf
	  IPartition = xr
	  IClass = wi
	  IActivityNode =  vW
	  IOperation =  xn
	  IParameter = xq
	  IFow = wR
	  IActivity = vY
	  IClassDiagram = wh
	  */
	  
	  //------------------------------------------------------
	  
	  /*
	  for(String cid : iEvaluationMap.keySet()){
		Map<IElementSt , Double> cmap = iEvaluationMap.get(cid);
		for(IElementSt cele : cmap.keySet()){
			String cn = cele.iElementName;
			Class cc = cele.iElementType;
			System.out.println(cc + " : " + cmap.values() + " : " + cid + " : " + cn);
		}
	  }
	  System.out.println("--------------------------");
	  */

	  //-----------------------------DiagramReflection--------------------------
	  
	  /*
	  DiagramReflection df = new DiagramReflection();
	  df.reflectionEvaluation(iEvaluationMap);
	  */
	  
	  
	  //------------------------------------------------------
    } catch (Exception e) {
      System.out.println("UMLcheck failed");
	  System.out.println(e);
	  JOptionPane.showMessageDialog(null,e);
    }
  }
  //------------------------------------------------------------------//
  
  private void setvariableinitialize(IActivityNode node){
	slicematch = new TreeMap<String , Map<IFlow , IFlow>>();
	slicelist = new ArrayList<String>();
	problist = new ArrayList<IActivityNode>();
	typelist = new ArrayList<String>();
	slicedepth = 0;
	slicedepthlist = new ArrayList<Integer>();
	keepflowmMap = new HashMap<String , IFlow>();
	switchlist = new ArrayList<IActivityNode>();
	breaklabelList = new ArrayList<String>();
	eachpartnodemap = new HashMap<ArrayList<ArrayList<IPartition>> , List<IActivityNode>>();
	if(node.getContainers().length >= 2){
		nowhoripartition = (IPartition)(node.getContainers()[0]);
		nowvertpartition = (IPartition)(node.getContainers()[1]);
	}
	sortpartnodelist = new ArrayList<IActivityNode>();
  }

  private void setvariablepost(IActivityDiagram dgm){
	slicematchact.put(dgm , slicematch);
	slicelistdgm.put(dgm , slicelist);
	slicedepthlistdgm.put(dgm , slicedepthlist);
	problistmap.put(dgm , problist);
	partitionmap.put(dgm , eachpartnodemap);
	finsortpart();
	restack(pstack);
	restack(nstack);
	restack(fstack);
	restack(flowstack);
	restack(loopflagstack);
	nodeflag.clear();
  }

  private void getAstahDiagrams(INamedElement element){
	 try{
	  IDiagram[] dgms = element.getDiagrams();
	  for(IDiagram dgm : dgms){
		  if(dgm instanceof IActivityDiagram){
			iActivityList.add(((IActivityDiagram)dgm).getActivity());	
		  }
		  else if(dgm instanceof IClassDiagram){
			IPresentation[] pres = dgm.getPresentations();
			boolean classflag = false;
			for(IPresentation pre : pres){
				if(pre.getType().equals("Class")){
					iClassList.add((IClass)(pre.getModel()));
					classflag = true;
				}
			}
			if(classflag == false){
				addEvaluationMap(dgm.getId(),dgm.getName(),dgm.getClass(),1.11);
			}
		  }
	  }
	  if(element instanceof IPackage){
		INamedElement[] iNamedElements = ((IPackage)element).getOwnedElements();
		for(INamedElement ele : iNamedElements){
			getAstahDiagrams(ele);
		}
	  }
	} catch(Exception e){
		System.out.println("getAstahDiagrams failed");
		System.out.println(e);
	}
  }

  class IParameterSt{
  	  String iParameterSt;
	  String iParameterTypeSt;
  }

  class IElementSt{
  	  String iElementName;
	  Class iElementType;
  }

  Map<String , Map<IElementSt , Double>> iEvaluationMap
		 = new HashMap<String , Map<IElementSt , Double>>();

  private void addEvaluationMap(String elementid , String elementname , Class elementtype ,Double evaluation){
	IElementSt iElementSt = new IElementSt();
	iElementSt.iElementName = elementname;
	iElementSt.iElementType = elementtype;
	iEvaluationMap.put(elementid , new HashMap<IElementSt , Double>());
	iEvaluationMap.get(elementid).put(iElementSt , evaluation);
  }
  //---------------
  private boolean checkFlow(IActivityNode[] iNodes){
	boolean boo = true;
	List<IActivityNode> nodelist = new ArrayList<IActivityNode>();
	for(IActivityNode inode : iNodes){
		int innum = inode.getIncomings().length;
		int outnum = inode.getOutgoings().length;
		if(inode instanceof IControlNode){
			IControlNode iControlNode = (IControlNode)inode;
			if(iControlNode.isInitialNode()){
				if(!(outnum == 1)){
					nodelist.add(inode);
				}
			}
			else if(iControlNode.isFinalNode()){
				if(!(innum == 1)){
					nodelist.add(inode);
				}
			}
			else if(iControlNode.isForkNode()){
				if(!(outnum > 1 && innum == 1)){
					nodelist.add(inode);
				}
			}
			else if(iControlNode.isJoinNode()){
				if(!(innum > 1 && outnum == 1)){
					nodelist.add(inode);
				}
			}
			else if(iControlNode.isMergeNode()){
			/*
				int coun = 0;
				if(innum == 1 && outnum == 2){
					for(IFlow outflow : inode.getOutgoings()){
						if(outflow.getGuard().equals("true") || outflow.getGuard().equals("false")){
							coun ++;
						}
					}
					if(coun == 2){
					
					}
					else if(coun == 1){
					
					}
				}
				else if(innum == 2 && outnum == 2){
					for(IFlow inflow : inode.getIncomings()){
						if(inflow.getGuard().equals("true") || inflow.getGuard().equals("false")){
							coun ++;
						}
					}
					if(coun == 2){
					
					}
					else if (coun == 1){
					
					}
					else{
					
					}
				}
			*/
			}
		}
		else{
			if(((IAction)inode).isConnector()){
				if(outnum > 0){
					nodelist.add(inode);
				}
			}
			else if(!(innum == 1 && outnum == 1)){
				System.out.println(inode);
				nodelist.add(inode);
			}
		}
	 }
	 if(nodelist.size() != 0){
		boo = false;
		for(IActivityNode badnode : nodelist){
			addEvaluationMap(badnode.getId(),badnode.getName(),badnode.getClass(),1.32);
		}
	 }
	 return boo;
  }
  /*

  in : fi
  ac : if_n / ib_n : do / db : fo / fb

  */
  //----
  IFlow beforef;
  List<IFlow> finflow = new ArrayList<IFlow>();
  Boolean finallyemp = true;
  List<IActivityNode> switchlist;
  List<String> breaklabelList;
  //----
  Stack nstack = new Stack();
  Stack pstack = new Stack();
  HashMap<IActivityNode , Boolean> nodeflag
   = new HashMap<IActivityNode , Boolean>();
  //---
  HashMap<IActivityNode , Integer> mergeroot
   = new HashMap<IActivityNode , Integer>();
  HashMap<IActivityNode , IActivityNode> mergematch
   = new HashMap<IActivityNode , IActivityNode>();//(decision , merge)
  //---
  HashMap<IActivityNode , Integer> joinroot
   = new HashMap<IActivityNode , Integer>();
  HashMap<IActivityNode , IActivityNode> joinmatch
   = new HashMap<IActivityNode , IActivityNode>();//(join , fork)
  //------------
  HashMap<IActivityNode , IActivityNode> loopmatch
   = new HashMap<IActivityNode , IActivityNode>();//(back , loop)
  //------------
  HashMap<IActivityNode , Integer> decbre
   = new HashMap<IActivityNode , Integer>();
  Stack<String> loopflagstack = new Stack<>();
  //slice---------
  Stack<String> flowstack = new Stack<>();
  Map<String , IFlow> keepflowmMap;
  int nownum;
//----------------------------------------------------------------
  private void probnextnode()
		  throws InvalidEditingException , InvalidUsingException , ClassNotFoundException{
   IActivityNode finallyflag = null;
   nownum = 0;
   while(!(nstack.empty())){
	IActivityNode nownode = (IActivityNode)(nstack.pop());
	sortpartition(nownode);
	//JOptionPane.showMessageDialog(null,nownode);
	System.out.println(nownode);
	if(nownode instanceof IControlNode){
		IControlNode iControlNode = (IControlNode)nownode;
//------InitialNode-------------------------
		if(iControlNode.isInitialNode()){
			beforenext(nownode , 0);
			problist.add(nownode);
			typelist.add("in");
			IFlow outFlow = nownode.getOutgoings()[0];
			String s_name = slicenum(nownum , 0);
			slicedepthlist.add(slicedepth);
			slicedepth ++;
			slicelist.add(s_name);
			keepflowmMap.put(s_name ,  outFlow);
			flowstack.push(s_name);
		}
//------FinalNode--------------------------
		else if(iControlNode.isFinalNode()){
			nodeflag.put(nownode , true);
			if(nownode.getSupplierDependencies().length > 0){
				INamedElement supp = nownode.getSupplierDependencies()[0].getClient();
				if(((IElement)pstack.peek()).getId().equals(supp.getId())){
					Object e = pstack.pop();
					IActivityNode tryblock = (IActivityNode)pstack.peek();
					nstack.push(tryblock);
				}
			}
			else{
				//System.out.println("Final");
				problist.add(nownode);
				typelist.add("fi");
				if(!(flowstack.isEmpty())){
					String s_name = flowstack.pop();
					IFlow iflow = keepflowmMap.get(s_name);
					IFlow oflow = nownode.getIncomings()[0];
					slicedepth --;
					if(slicedepth != 0){
						
					}
					slicematch.put(s_name , new HashMap<IFlow , IFlow>());
					slicematch.get(s_name).put(iflow , oflow);
				}
				finact();
			}
		}
//------ForkNode---------------------------
		else if(iControlNode.isForkNode()){
			if(pstack.empty()){
				pstack.push(nownode);
				slicedepthlist.add(slicedepth);
				slicedepth ++;
				joinroot.put(nownode , 0);
				beforenext(nownode , 0);
			}
			else{
			//second time
				if(((IActivityNode)(pstack.peek())).getId().equals(nownode.getId())){
					if(nownode.getOutgoings().length > joinroot.get(nownode)){
						Integer rootnum = joinroot.get(nownode) + 1;
						joinroot.put(nownode , rootnum);
						beforenext(nownode , (int)rootnum);
					}
					else{
						System.out.println("Flow is broken at " + nownode);//1.31
						addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.31);
					}
				}
			//first time
				else{
					slicedepthlist.add(slicedepth);
					slicedepth ++;
					pstack.push(nownode);
					joinroot.put(nownode , 0);
					beforenext(nownode , 0);
				}
			}
		}
//------JoinNode-------------------------
		else if(iControlNode.isJoinNode()){
			IActivityNode matchjoin = (IActivityNode)(pstack.peek());
			//second time
			if(joinmatch.containsKey(matchjoin)){
				if(joinmatch.get(matchjoin).getId().equals(nownode.getId())){
					if(joinroot.get(matchjoin) + 1 == matchjoin.getOutgoings().length ){
						Object e = pstack.pop();
						//
						slicedepth--;
						beforenext(nownode , 0);
					}
					else{
						nstack.push(matchjoin);
					}
				}
				else{
					System.out.println("Unable to match any node with " + nownode);//1.41
					addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
				}
			}
			//first time
			else{
				if(matchjoin.getOutgoings().length == nownode.getIncomings().length){
					joinmatch.put(matchjoin , nownode);
					nodeflag.put(nownode , true);
					nstack.push(matchjoin);
				}
				else{
					System.out.println("Unable to match any node with " + nownode);//1.41
					addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
				}
			}
		}
//------Decision/MergeNode---------------------------------
		else if(iControlNode.isMergeNode()){
			int innum = nownode.getIncomings().length;
			int outnum = nownode.getOutgoings().length;
			/*
			if(nownode.getOutgoings().length > 1){
				for(IFlow iflow : nownode.getOutgoings()){
					if(iflow.getTarget() instanceof isConnector){
						
					} 
				}
			}
			*/
		
		//for/while-back or merge----------------------------------
			if(innum == 1 && outnum == 1){
				//for/while-back
				if(loopmatch.containsKey(nownode)){
					if(loopmatch.get(nownode).getId().equals(((IActivityNode)(pstack.peek())).getId())){
						problist.add(nownode);
						typelist.add("fb");
						IActivityNode forloopnode = (IActivityNode)pstack.peek();
						boolean guardflag = false;
						for(int i = 0; i < 2 ; i++){
							if(forloopnode.getOutgoings()[i].getGuard().equals("false")){
								guardflag = true;
								Object ef = pstack.pop();
								nodeflag.put(nownode , true);
								Object el = loopflagstack.pop();
								String s_name = flowstack.pop();
								IFlow iflow = keepflowmMap.get(s_name);
								IFlow oflow = forloopnode.getOutgoings()[i];
								slicedepth --;
								slicematch.put(s_name , new HashMap<IFlow , IFlow>());
								slicematch.get(s_name).put(iflow , oflow);
								beforenext(forloopnode , i);
							}
						}
						if(!guardflag){
							System.out.println("There is no conditional statement in " + nownode);//1.42
							addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.42);
						}
					}
					else{
						System.out.println("Unable to match any node with " + nownode);//1.41
						addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
					}
				}
				/*

				*/

				else{
					boolean blf = true;
				//break label
					if(nownode.getSupplierDependencies().length == 1){
						IActivityNode client = (IActivityNode)(nownode.getSupplierDependencies()[0].getClient());
						if(client.getClientDependencies().length == 2){
							if(breaklabelList.contains(client.getName())){
								breaklabelList.remove(client.getName());
								slicedepth --;
								beforenext(nownode , 0);
								problist.add(nownode);
								typelist.add("ble");
								blf = false;
							}
							else{
								breaklabelList.add(client.getName());
								for(IDependency sup : client.getClientDependencies()){
									if(!(nownode.getId().equals(sup.getSupplier().getId()))){
										IActivityNode supnode = (IActivityNode)(sup.getSupplier());
										if(supnode.getIncomings().length == 1 && supnode.getOutgoings().length == 1){	
											mergematch.put(supnode , nownode);
											String s_name = slicenum(nownum , 6);
											slicelist.add(s_name);
											slicedepthlist.add(slicedepth);
											slicedepth ++;
											IFlow iflow = nownode.getIncomings()[0];
											IFlow oflow = supnode.getOutgoings()[0];
											slicematch.put(s_name , new HashMap<IFlow , IFlow>());
											slicematch.get(s_name).put(iflow , oflow);
											problist.add(nownode);
											typelist.add("bli");
											beforenext(nownode , 0);
											blf = false;
										}
										else{
											//critical error missmatch
										}
									}
								}
							}
						}
					}
				//merge
					if(blf){
						IActivityNode matchdecision = (IActivityNode)(pstack.peek());
						problist.add(nownode);
						typelist.add("ib");
					//first
						if(!(mergematch.containsKey(matchdecision))){
							mergematch.put(matchdecision , nownode);
							nodeflag.put(nownode , true);
							nstack.push(matchdecision);
						}
					//second
						else{
							if(mergematch.get(matchdecision).getId().equals(nownode.getId())){
								int doutnum = matchdecision.getOutgoings().length;
									if(doutnum -1  > mergeroot.get(matchdecision)){
										nstack.push(matchdecision);
									}
									else{
										IActivityNode matchmerge = nownode;
										Integer breaknum = decbre.get(matchdecision);
										if(doutnum - (int)breaknum == nownode.getIncomings().length){
											Object e = pstack.pop();
											String s_name = flowstack.pop();
											IFlow iflow = keepflowmMap.get(s_name);
											IFlow oflow = nownode.getOutgoings()[0];
											slicedepth --;
											slicematch.put(s_name , new HashMap<IFlow , IFlow>());
											slicematch.get(s_name).put(iflow , oflow);
											beforenext(nownode, 0);
										}
										else{
											addEvaluationMap(matchdecision.getId(),matchdecision.getName(),matchdecision.getClass(),1.32);
										}
									}
							}
							else{
								System.out.println("Unable to match any node with " + nownode);//1.41
								addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
							}
						}
					}
				}
			}
		//decision or do-while-back--------------------------------
			else if(innum == 1 && outnum >= 2){			
				//do-while-back
				if(loopmatch.containsKey(nownode)){
					if(loopmatch.get(nownode).getId().equals(((IActivityNode)(pstack.peek())).getId())){
						problist.add(nownode);
						typelist.add("db");
						for(int i = 0; i < 2 ; i++){
							if(nownode.getOutgoings()[i].getGuard().equals("false")){
								Object ef = pstack.pop();
								Object el = loopflagstack.pop();
								String s_name = flowstack.pop();
								IFlow iflow = keepflowmMap.get(s_name);
								IFlow oflow = nownode.getOutgoings()[i];
								slicedepth --;
								slicematch.put(s_name , new HashMap<IFlow , IFlow>());
								slicematch.get(s_name).put(iflow , oflow);
								beforenext(nownode , i);
							}
						}
					}
					else{
						System.out.println("Unable to match any node with " + nownode);//1.41
						addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
					}
				}
			//decision
				else{
				//first
					problist.add(nownode);
					Boolean switchflag = false;
					if(nownode.getTaggedValues().length > 0){
						ITaggedValue[] tags = nownode.getTaggedValues();
						for(int i = 0; i<tags.length; i++){
							if(tags[i].getKey().equals("switch") && tags[i].getValue().equals("1")){
								switchflag = true;
								break;
							}
						}
					}
					if(switchflag){
						typelist.add("sw_" + listcount(nownode));
						switchlist.add(nownode);
					}
					else{	
						typelist.add("if_" + listcount(nownode));
					}
					if(pstack.empty()){
						pstack.push(nownode);
						mergeroot.put(nownode , 0);
						decbre.put(nownode , 0);
						IFlow inflow = nownode.getIncomings()[0];
						String s_name = slicenum(nownum , 2);
						slicedepthlist.add(slicedepth);
						slicedepth ++;
						slicelist.add(s_name);
						keepflowmMap.put(s_name ,  inflow);
						flowstack.push(s_name);
						beforenext(nownode , 0);
					}
					else{
					//first
						if(!(((IActivityNode)(pstack.peek())).getId().equals(nownode.getId()))){
							pstack.push(nownode);
							mergeroot.put(nownode,0);
							decbre.put(nownode , 0);
							IFlow inflow = nownode.getIncomings()[0];
							String s_name = slicenum(nownum , 2);
							slicedepthlist.add(slicedepth);
							slicedepth ++;
							slicelist.add(s_name);
							keepflowmMap.put(s_name ,  inflow);
							flowstack.push(s_name);
							beforenext(nownode,0);
						}
					//second
						else{
						//Flow still
							if(outnum -1  > mergeroot.get(nownode)){
								Integer rootnum = mergeroot.get(nownode) + 1;
								mergeroot.put(nownode, rootnum);
								beforenext(nownode, (int)rootnum);
							}
						//Flow fin - go match merge
							
							else{
								/*
								IActivityNode matchmerge = mergematch.get(nownode);
								Integer breaknum = decbre.get(nownode);
								if(outnum - (int)breaknum == matchmerge.getIncomings().length){
									Object e = pstack.pop();
									String s_name = flowstack.pop();
									IFlow iflow = keepflowmMap.get(s_name);
									IFlow oflow = matchmerge.getOutgoings()[0];
									slicedepth --;
									slicematch.put(s_name , new HashMap<IFlow , IFlow>());
									slicematch.get(s_name).put(iflow , oflow);
									beforenext(matchmerge, 0);
								}
								else{
									addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.32);
								}
								*/
							}
						}
					}
				}
			}
		//do-while-loop or merge------------------------------------
			else if(innum >= 2 && outnum == 1){
			//do loop------------------
				if(checkloop(nownode , 0)){
					problist.add(nownode);
					typelist.add("do");
					loopflagstack.push("true");
					IFlow inflow = beforef;
					String s_name = slicenum(nownum , 3);
					slicedepthlist.add(slicedepth);
					slicedepth ++;
					slicelist.add(s_name);
					keepflowmMap.put(s_name ,  inflow);
					flowstack.push(s_name);
					beforenext(nownode , 0);
				}
			//merge
				else{
					if(pstack.empty()){
						System.out.println("Unable to match any node with " + nownode);//1.41
						addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
					}
					else{
						IActivityNode matchdecision = (IActivityNode)(pstack.peek());
						problist.add(nownode);
						if(switchlist.contains(matchdecision)){
							typelist.add("sb");
						}
						else{
							typelist.add("ib");
						}
					//first
						if(!(mergematch.containsKey(matchdecision))){
							mergematch.put(matchdecision , nownode);
							nodeflag.put(nownode , true);
							nstack.push(matchdecision);
						}
					//second
						else{
							if(mergematch.get(matchdecision).getId().equals(nownode.getId())){
								///*
								int doutnum = matchdecision.getOutgoings().length;
								if(doutnum -1  > mergeroot.get(matchdecision)){
									nstack.push(matchdecision);
								}
								else{
									IActivityNode matchmerge = nownode;
									Integer breaknum = decbre.get(matchdecision);
									if(doutnum - (int)breaknum == nownode.getIncomings().length){
										Object e = pstack.pop();
										String s_name = flowstack.pop();
										IFlow iflow = keepflowmMap.get(s_name);
										IFlow oflow = nownode.getOutgoings()[0];
										slicedepth --;
										slicematch.put(s_name , new HashMap<IFlow , IFlow>());
										slicematch.get(s_name).put(iflow , oflow);
										if(switchlist.contains(matchdecision)){
											problist.add(nownode);
											typelist.add("sn");
										}
										beforenext(nownode, 0);
									}
									else{
										addEvaluationMap(matchdecision.getId(),matchdecision.getName(),matchdecision.getClass(),1.32);
									}
								}
								//*/
								//nstack.push(matchdecision);
								//syntaxmap.put(peekflow.getTarget() , new HashMap<IActivityNode , String>());
								//syntaxmap.get(peekflow.getTarget()).put(nowflow.getSource() , splittag[0]);
							}
							else{
								System.out.println("Unable to match any node with " + nownode);//1.41
								addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
							}
						}
					}
				}
			}
		//for/while---------------------------------------------
			else if(innum == 2 && outnum == 2){
				if(checkloop(nownode , 1)){
					problist.add(nownode);
					Boolean whileflag = false;
					if(nownode.getTaggedValues().length > 0){
						ITaggedValue[] tags = nownode.getTaggedValues();
						for(int i = 0; i<tags.length; i++){
							if(tags[i].getKey().equals("while") && tags[i].getValue().equals("1")){
								whileflag = true;
								break;
							}
						}
					}
					if(whileflag){
						typelist.add("wh");
					}
					else{	
						typelist.add("fo");
					}
					loopflagstack.push("true");
					boolean guardflag = false;
					for(int i = 0; i < 2 ; i++){
						if(nownode.getOutgoings()[i].getGuard().equals("true")){
							guardflag = true;
							IFlow inflow = beforef;
							String s_name = slicenum(nownum , 4);
							slicedepthlist.add(slicedepth);
							slicedepth ++;
							slicelist.add(s_name);
							keepflowmMap.put(s_name ,  inflow);
							flowstack.push(s_name);
							beforenext(nownode , i);
						}
					}
					if(!guardflag){
						System.out.println("There is no conditional statement in " + nownode);//1.42
						addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.42);
					}
				}
				else{
					System.out.println("Unable to match any node with " + nownode);//1.41
					addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.41);
				}
			}
		//worong number---------------------------------------------
			else{
				System.out.println("Worong the number of the " + nownode + "'s Flow");//1.32
				addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.32);
			}
	//-----------------------------------------------------------------------------------
		}
	}
//--------ActionNode-----------------------------
	else{
	//-----connecter-----
		if(((IAction)nownode).isConnector()){
			if(!(pstack.empty()) && !(loopflagstack.empty())){
				IActivityNode matchdecision = (IActivityNode)pstack.peek();
				if(nownode.getName().equals("Break") || nownode.getName().equals("break") || breaklabelList.contains(nownode.getName())){
					if(decbre.containsKey(matchdecision)){
						Integer brenum = decbre.get(matchdecision) + 1;
						decbre.put(matchdecision , brenum);
						nodeflag.put(nownode,true);
						if(mergematch.containsKey(matchdecision)){
							IActivityNode matchmerge = mergematch.get(matchdecision);
							nstack.push(matchmerge);
						}
						else{
							//
						}
						problist.add(nownode);
						if(breaklabelList.contains(nownode.getName())){
							typelist.add("bl");
						}
						else{
							typelist.add("br");
						}
					}
				}
				else{
					JOptionPane.showMessageDialog(null,"nazo");
				}
			}
			/*
			else{
				System.out.println("break point is imprecise");
				addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.44);
			}
			*/
		}
	//------actionnode-----
		else{
			//pin
			if(((IAction)nownode).getInputs().length > 0){
				System.out.println(nownode + " have InputPin");
				if(((IAction)nownode).isCallBehaviorAction()){
					for(IInputPin inPin : ((IAction)nownode).getInputs()){
						IActivityDiagram calldgm = ((IAction)nownode).getCallingActivity().getActivityDiagram();
						IPresentation[] callpres = calldgm.getPresentations();
						int inPinflag = 0;
						for(IPresentation callpre : callpres){
							String callpretype = callpre.getType();
							if(callpretype.equals("Connector")){
								INamedElement callnode = (INamedElement)(callpre.getModel());
								String nodename = callnode.getName();
								if(nodename.equals(inPin.getName())){
									inPinflag += 1;
								}
							}
						}
						if(inPinflag == 0){
							System.out.println("There is no connector that matches " + inPin);
							addEvaluationMap(inPin.getId(),inPin.getName(),inPin.getClass(),1.51);
						}
						else if(inPinflag > 1){

						}
					}
				}
				else{
					System.out.println("Unable to connect InputPin to this " + nownode);
					addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.52);
				}
			}
			if(((IAction)nownode).getOutputs().length > 0){
				System.out.println(nownode + " have OutputPin");
			}
		//if(nodeflag.get(nownode) == false){
			if(nownode.getHyperlinks().length == 1){
				IHyperlink link = nownode.getHyperlinks()[0];
				if(link.isModel()){
					String linkpath = link.getPath();
				
				}
			}
			else if(nownode.getHyperlinks().length > 1){
				System.out.println("Wrong the number of the " + nownode + "'s Hyperlink");
			}
			else{
				//------try-catch-------
				if(nownode.getStereotypes().length > 0){
					String stereo = nownode.getStereotypes()[0];
					System.out.println("Block : " + stereo);
					if(stereo.equals("TryBlock") || stereo.contains("CatchEx") || stereo.equals("FinallyBlock")){
						if(!(pstack.isEmpty())){
							if(((IElement)pstack.peek()).getId().equals(nownode.getId())){
								Integer root = 0;
								if(mergeroot.containsKey(nownode)){
									root = mergeroot.get(nownode) + 1;
									if(nownode.getOutgoings().length == root){
										Object e = pstack.pop();
										if(finallyflag == null){
											System.out.println("there are no FinallyBlock");
										}
										else{
											if(finallyemp){
												problist.add(nownode);
												typelist.add("en");
											}
											else{
												finallyemp = true;
											}
											beforenext(finallyflag , 0);
											finallyflag = null;
										}
									}
									else{
										mergeroot.put(nownode , root);
										beforenext(nownode, root);
										problist.add(nownode);
										typelist.add("en");
									}
								}
								else{
									mergeroot.put(nownode , 0);
									beforenext(nownode, root);
									problist.add(nownode);
									typelist.add("en");
								}
							}
							else{
								if(stereo.equals("TryBlock")){
									if(checktryact(nownode)){
										pstack.push(nownode);
										IFlow inflow = nownode.getIncomings()[0];
										String s_name = slicenum(nownum , 5);
										slicedepthlist.add(slicedepth);
										slicedepth ++;
										slicelist.add(s_name);
										keepflowmMap.put(s_name ,  inflow);
										flowstack.push(s_name);
										problist.add(nownode);
										typelist.add("tr");
									}
									else{
										JOptionPane.showMessageDialog(null,"false try act!!");
									}
								}
								else if(stereo.equals("FinallyBlock")){
									finallyflag = nownode;
									String s_name = flowstack.pop();
									IFlow iflow = keepflowmMap.get(s_name);
									IFlow oflow = nownode.getOutgoings()[0];
									slicedepth --;
									slicematch.put(s_name , new HashMap<IFlow , IFlow>());
									slicematch.get(s_name).put(iflow , oflow);
									///*
									IDependency[] iDepends = nownode.getSupplierDependencies();
									for(IDependency iDepend : iDepends){
										INamedElement client = iDepend.getClient();
										if(((IControlNode)client).isInitialNode()){
											IFlow isini = ((IActivityNode)client).getOutgoings()[0];
											IActivityNode isfin = isini.getTarget();
											if(isfin instanceof IControlNode){	
												if(((IControlNode)isfin).isFinalNode()){
													finallyemp = false;
												}
											}
										}
									}
									if(finallyemp){
										problist.add(nownode);
										typelist.add("fn");
									}
									//*/
								}
								else{
									problist.add(nownode);
									typelist.add("ca");
								}
								pstack.push(nownode);
								IDependency[] iDepends = nownode.getClientDependencies();
								for(IDependency iDepend : iDepends){
									INamedElement client = iDepend.getSupplier();
									if(((IControlNode)client).isInitialNode()){
										beforenext((IActivityNode)client, 0);
									}
								}
							}
						}
						else{
							if(stereo.equals("TryBlock")){
								if(checktryact(nownode)){
									pstack.push(nownode);
									IFlow inflow = nownode.getIncomings()[0];
									String s_name = slicenum(nownum , 5);
									slicedepthlist.add(slicedepth);
									slicedepth ++;
									slicelist.add(s_name);
									keepflowmMap.put(s_name ,  inflow);
									flowstack.push(s_name);
									problist.add(nownode);
									typelist.add("tr");
								}
								else{
									JOptionPane.showMessageDialog(null,"false try act!!");
								}
							}
							pstack.push(nownode);
							IDependency[] iDepends = nownode.getClientDependencies();
							for(IDependency iDepend : iDepends){
								INamedElement client = iDepend.getSupplier();
								if(((IControlNode)client).isInitialNode()){
									beforenext((IActivityNode)client, 0);
								}
							}
						}
					}
					/*
					else{
						problist.add(nownode);
						typelist.add("ac");
						String s_name = slicenum(nownum , 1);
						slicelist.add(s_name);
						slicedepthlist.add(slicedepth);
						slicematch.put(s_name , new HashMap<IFlow , IFlow>());
						slicematch.get(s_name).put(nownode.getIncomings()[0], nownode.getOutgoings()[0]);
						beforenext(nownode , 0);
					}
					*/
				}
				else{
					problist.add(nownode);
					typelist.add("ac");
					String s_name = slicenum(nownum , 1);
					slicelist.add(s_name);
					slicedepthlist.add(slicedepth);
					slicematch.put(s_name , new HashMap<IFlow , IFlow>());
					slicematch.get(s_name).put(nownode.getIncomings()[0], nownode.getOutgoings()[0]);
					beforenext(nownode , 0);
				}
			}
			/*
			}
			else{
				System.out.println(nownode + " has already passed");//1.34
				addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.34);
			}
			*/
		}
	}
   }
//slice view--------------------------------------------
   /*
   System.out.print(slicematch.size());
   for(int i = 0;i < slicelist.size();i++){
	String slicename = slicelist.get(i);
	System.out.println(slicename);
	System.out.println("slice depth : " + slicedepthlist.get(i));
	Map<IFlow , IFlow> sfm = slicematch.get(slicename);
	for(IFlow sf : sfm.keySet()){
		System.out.println("   " + sf + " : " + sfm.get(sf));
	}
   }
   */
  }


//-------------------------------------------------------
  private String slicenum(int num , int type){
	String slicename = "s_" + String.valueOf(num);
	nownum ++;
	String typename = null;
	switch(type){
		case 0:
			typename = "_MAIN";
			break;
		case 1: 
			typename = "_Action";
			break;
		case 2: 
			typename = "_IF";
			break;
		case 3: 
			typename = "_Do-while";
			break;
		case 4: 
			typename = "_For-while";
			break;
		case 5: 
			typename = "_Try-catch";
			break;
		case 6:
			typename = "_Break-label";
			break;
		default:
			//
	}
	slicename = slicename + typename;
	return slicename;
  }

//---------------------------------------------------
Stack fstack = new Stack();
static HashMap<IActivityNode , Map<IActivityNode , String>> syntaxmap
 = new HashMap<IActivityNode , Map<IActivityNode , String>>();

  private void beforenext (IActivityNode nownode , int root){
      nodeflag.put(nownode , true);
  	  if(root < nownode.getOutgoings().length){
		IFlow nowflow = nownode.getOutgoings()[root];
		//System.out.println(nowflow);
		if(((IElement)nowflow).getTaggedValues().length > 0){
			ITaggedValue eletag[] = ((IElement)nowflow).getTaggedValues();
			for(ITaggedValue tag : eletag){
				String tagkey = tag.getKey();
				String[] splittag = tagkey.split("-", 0);
				if(splittag.length == 3){
					if(splittag[2].equals("flag")){
						if(splittag[1].equals("Initial")){
							fstack.push(nowflow);
						}
						else if(splittag[1].equals("Final")){
							IFlow peekflow = (IFlow)fstack.pop();
							syntaxmap.put(peekflow.getTarget() , new HashMap<IActivityNode , String>());
							syntaxmap.get(peekflow.getTarget()).put(nowflow.getSource() , splittag[0]);
						}
					}
				}
			}
		}
		beforef = nowflow;
		finflow.add(nowflow);
		nstack.push(nowflow.getTarget());
	  }
	  else{
		System.out.println("Flow is broken at " + nownode);//1.31
		addEvaluationMap(nownode.getId(),nownode.getName(),nownode.getClass(),1.31);
	  }
  }
//--------------------------------------------------------
  private boolean checkloop (IActivityNode loopnode , int looptype){
  //looptype = 0 : do-loop , looptype = 1 : for/while-loop
    boolean boo = false;
	if(loopnode.getIncomings().length == 2){
	  for(IFlow inFlow : loopnode.getIncomings()){
	    IActivityNode inSource = inFlow.getSource();
	    if(!(nodeflag.get(inSource)) && inSource instanceof IControlNode){
		  if(((IControlNode)inSource).isMergeNode()){
			switch(looptype){
			  case 0:
				if(inSource.getIncomings().length == 1 && inSource.getOutgoings().length == 2){
				  int guardcount = 0;
				  if(inFlow.getGuard().equals("true")){
					  for(IFlow outFlow : inSource.getOutgoings()){
						  if(outFlow != inFlow){
							if(outFlow.getGuard().equals("false")){
								boo = true;
							}
						  }
					  }
				  }
				}
				break;
			  case 1:
				if(inSource.getIncomings().length == 1 && inSource.getOutgoings().length == 1){
				  boo = true;
				}
				break;
		      default:
			}
			if(boo){
			  loopmatch.put(inSource , loopnode);
			  pstack.push(loopnode);
			}
		  }
		}
	  }
	}
	return boo;
  }
//------------------------
  private void restack (Stack res){
	while(!res.empty()){
		Object e = res.pop();
	}
  }
//------------------------
  private void finact (){
	while(!pstack.empty()){
		addEvaluationMap(((IActivityNode)pstack.peek()).getId(),((IActivityNode)pstack.peek()).getName(),((IActivityNode)pstack.peek()).getClass(),1.43);
		System.out.println("There is no node that matches " + pstack.pop());//1.43
	}
	/*
	for(IActivityNode passnode : nodeflag.keySet()){
		if(!(nodeflag.get(passnode))){
			System.out.println(passnode + " does not appear until FinalNode");//2.31
			addEvaluationMap(passnode.getId(),passnode.getName(),passnode.getClass(),2.31);
		}
	}
	*/
  }

  private String listcount(IActivityNode node){
	  int con = 0;
	  for(IActivityNode prob : problist){
		  if(prob.getId().equals(node.getId())){
			  con++;
		  }
	  } 
	  return Integer.toString(con);
  }

  List<IActivityNode> sortpartnodelist;
  public static IPartition nowhoripartition;
  public static IPartition nowvertpartition;
  IPartition befhoripartition;
  IPartition befvertpartition;
  int befdepth;

  Map<ArrayList<ArrayList<IPartition>> , List<IActivityNode>> eachpartnodemap;

  private void sortpartition(IActivityNode nownode){
	if(nownode.getContainers().length >= 2){
		IPartition horipartition = (IPartition)(nownode.getContainers()[0]);
		IPartition vertpartition = (IPartition)(nownode.getContainers()[1]);
		if((nowhoripartition.getId().equals(horipartition.getId())) && (nowvertpartition.getId().equals(vertpartition.getId()))){
			sortpartnodelist.add(nownode);
		}
		else{
			/*
			if(befhoripartition.getid().equals(horipartition.getId()) && befvertpartition.getid().equals(vertpartition.getId())){
				ArrayList<ArrayList<IPartition>> arrays = new ArrayList<ArrayList<IPartition>>();
				ArrayList<IPartition> array = new ArrayList<IPartition>();
				array.add(horipartition);
				array.add(vertpartition);
				arrays.add(array);
				List<IActivityNode> befpartnodelist = eachpartnodemap.get(arrays);

			}
			else{
			*/
			finsortpart();
			sortpartnodelist = new ArrayList<IActivityNode>();
			befhoripartition = nowhoripartition;
			befvertpartition = nowvertpartition;
			nowhoripartition = horipartition;
			nowvertpartition = vertpartition;
			sortpartnodelist.add(nownode);
			//}
		}
	}
  }

  private void finsortpart(){
	ArrayList<ArrayList<IPartition>> arrays = new ArrayList<ArrayList<IPartition>>();
	ArrayList<IPartition> array = new ArrayList<IPartition>();
	array.add(nowhoripartition);
	array.add(nowvertpartition);
	arrays.add(array);
	if(!(pstack.isEmpty())){
		JOptionPane.showMessageDialog(null,"block collapse!!");
	}
	eachpartnodemap.put(arrays , sortpartnodelist);
  }

  private Boolean checktryact(IActivityNode trynode){
	Boolean ff = false;
	Boolean cf = false;
	System.out.println("checktryact s");
	for(IFlow outflow : trynode.getOutgoings()){
		IActivityNode target = outflow.getTarget();
		if(target.getStereotypes().length > 0){
			List<String> stereolist = new ArrayList<String>(Arrays.asList(target.getStereotypes()));
			if(stereolist.contains("FinallyBlock")){
				ff = true;
			}
		}
		if(target.getSupplierDependencies().length == 1){
			IActivityNode client = (IActivityNode)(target.getSupplierDependencies()[0].getClient());
			if(client.getStereotypes().length > 0){
				List<String> stereolist = new ArrayList<String>(Arrays.asList(client.getStereotypes()));
				if(stereolist.contains("CatchBlock")){
					cf = true;
				}
			}	
		}
	}
	System.out.println("checktryact f");
	if(!(ff && cf)){
		ff = false;
	}
	return ff;
  }
//////////////////////////////////////////////////////////////////////////////////
 }