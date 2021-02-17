package umlcheker;;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;

public class Serialize extends DiagramReflection{
	
	//Map<String , Map<String , String>> secolormap
	public void seri(ArrayList<String> senotelist){
		try{
			ObjectOutputStream objOutStream = 
			new ObjectOutputStream(new FileOutputStream(projectName()));
			objOutStream.writeObject(senotelist);
         } catch (Exception e){
			 System.out.println("seri failed");
             System.out.println(e);
         }
     }

	 public void deseri()
		throws ClassNotFoundException , InvalidEditingException, InvalidUsingException{		
		ArrayList<String> denotelist = new ArrayList<String>();
		try {
            ObjectInputStream objInStream 
              = new ObjectInputStream(new FileInputStream(projectName()));
			denotelist = (ArrayList)objInStream.readObject();
		}catch(EOFException e){
			//
		} catch(Exception e){
			System.out.println("deseri failed");
			System.out.println(e);
		}
		DiagramReflection dr = new DiagramReflection();
		dr.setdel(denotelist);
	 }

	 private String projectName(){
	 	 String projectname = "";
		 try{
			String path = new File(".").getAbsoluteFile().getParent();
			AstahAPI api = AstahAPI.getAstahAPI();
			ProjectAccessor projectAccessor = api.getProjectAccessor();
			IModel iCurrentProject = projectAccessor.getProject();
			String modelname = iCurrentProject.getName();
			projectname = path + "/plugins/serialize/" + modelname + "_Serialize.bin";
		}
		 catch(Exception e){
			 System.out.println("projectName failed");
		 	 System.out.println(e);
		 }
		 return projectname;
	 }
}