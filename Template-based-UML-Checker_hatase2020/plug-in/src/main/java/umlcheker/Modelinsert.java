package umlcheker;;

import java.util.*;
import java.util.List;
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

public class Modelinsert extends Thread{

  CreateTemplate ct = new CreateTemplate();
  Activator act = new Activator();
  Dialog dia = new Dialog();

  public void run(){
    try{
		ct.createTemplate();
		/*
		if(act.selectnum == 1){
			ct.createTemplate();
		}
		else{
			//System.out.println("[Warning]Unable to insert into two or more elements");
			dia.warningdialog("Unable to insert into two or more elements");
		}
		*/
		
	} catch(Exception e){
	  System.out.println("Modelinsert failed");
	  System.out.println(e);
	}
  }

}
