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

public class SliceViewer extends Thread{

  CreateTemplate ct = new CreateTemplate();
  Activator act = new Activator();
  Dialog dia = new Dialog();
  ChangedSliceColor chc = new ChangedSliceColor();

  public void run(){
    try{
		chc.changeSliceColor();
	} catch(Exception e){
	  System.out.println("Modelinsert failed");
	  System.out.println(e);
	}
  }

}
