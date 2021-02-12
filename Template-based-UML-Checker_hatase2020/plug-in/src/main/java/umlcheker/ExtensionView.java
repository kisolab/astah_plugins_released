package umlcheker;;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.lang.Object;
import java.lang.Thread;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
 
import javax.swing.*;
import javax.swing.event.*;
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

public class ExtensionView extends JPanel implements IPluginExtraTabView , ActionListener , ProjectEventListener , ChangeListener{

  public static void main(String[] args) {
    new ExtensionView();
  }

  JButton btn1, btn2;
  JToggleButton btn3;
  Activator acti = new Activator();
  
  int changedcount = 0;
  int insertcount = 0;

  public ExtensionView(){

    addProjectEventListener();
    JPanel p1 = new JPanel();
    JPanel p2 = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    p1.setLayout(new BorderLayout());
    p2.setLayout(new GridLayout(3,1));

    btn1 = new JButton("Check");
    btn1.setActionCommand("Check");
    btn1.addActionListener(this);
    btn1.setVisible(true);

    btn2 = new JButton("Insert");
    btn2.setActionCommand("Insert");
    btn2.addActionListener(this);
    btn2.setVisible(true);
    
    btn3 = new JToggleButton("View",false);
    btn3.setActionCommand("View");
    btn3.addChangeListener(this);
    btn3.setVisible(true);
    

    OutputTextArea out = new OutputTextArea();
    out.setToSystemOut();
    p1.add(out,BorderLayout.CENTER);
    
    p2.add(btn1);
    p2.add(btn2);
    p2.add(btn3);

    p1.add(p2,BorderLayout.WEST);

    setLayout(new GridLayout(1,1));
    add(p1);
  }

  public void actionPerformed(ActionEvent ev){
    String cmd = ev.getActionCommand();
    if(cmd.equals("Check")){
      try {
        UMLcheck uc = new UMLcheck();
        Thread t = new Thread(uc);
        uc.start();
      }catch(Exception ex){
        System.out.println("Object run failed");
      }
    }
    else if(cmd.equals("Insert")){
      insertcount++ ;
      acti.addcount = acti.addcount - 2 ;
      System.out.println("insertcount : " + insertcount);
      try{
        Modelinsert mi = new Modelinsert();
        Thread t = new Thread(mi);
        mi.start();
      }catch(Exception ex){
        System.out.println("Object run failed");
      }
    }
  }

  public static int sliceviewerflag = 0;

  public void stateChanged(ChangeEvent e){
    if(btn3.isSelected()){
      if(sliceviewerflag == 0 || sliceviewerflag == 2){
        try{
          System.out.println("--------slice viewer ON---------");
          SliceViewer sv = new SliceViewer();
          Thread t = new Thread(sv);
          sliceviewerflag = 1;
          sv.start();
        }catch(Exception ex){
          System.out.println("Object run failed");
        }
      }
    }
    else{
      if(sliceviewerflag == 1){
        try{
          System.out.println("--------slice viewer OFF--------");
          SliceViewer ss = new SliceViewer();
          Thread t = new Thread(ss);
          sliceviewerflag = 2;
          ss.start();
        }catch(Exception ex){
          System.out.println("Object run failed");
        }
      }
    }
  }

  private void addProjectEventListener() {
    try {
      AstahAPI api = AstahAPI.getAstahAPI();
      ProjectAccessor projectAccessor = api.getProjectAccessor();
      projectAccessor.addProjectEventListener(this);
    } catch (ClassNotFoundException e) {
      System.out.println(e);
    }
  }
  
  //IPluginExtraTabView Override---------
  @Override
  public void projectChanged(ProjectEvent e) {
    //changedcount ++;
    //System.out.println("changedcount : " + changedcount);
    ProjectChangedAction pca = new ProjectChangedAction();
    pca.changedAction(e);
  }

  @Override
  public void projectClosed(ProjectEvent e) {
  }

  @Override
  public void projectOpened(ProjectEvent e) {
  }

  @Override
  public void addSelectionListener(ISelectionListener listener) {
  }

  public Component getComponent() {
    return this;
  }
  public String getDescription() {
    return "UMLcheck";
  }
  public String getTitle() {
    return "UMLcheck";
  }
  public void activated(){
  }
  public void deactivated(){
  }
  //-----------------------------------------
}

class OutputTextArea extends TextArea {
  private TextAreaOutputStream out;
  
  public OutputTextArea() throws HeadlessException {
    super();
    this.setEditable(false);
    out = new TextAreaOutputStream(this);
  }
  
  public void setToSystemOut(){
    System.setOut(new PrintStream(this.getOut()));
  }

  public void setToSystemErr(){
    System.setErr(new PrintStream(this.getOut()));
  }

  public TextAreaOutputStream getOut() {
    return out;
  }

  public void flush(){
    this.append(out.toString());
    out.reset();
  }
  
}

class TextAreaOutputStream extends ByteArrayOutputStream {
  private OutputTextArea textarea;
  
  public TextAreaOutputStream(OutputTextArea textarea) {
    super();
    this.textarea = textarea;
  }

  public synchronized void write(byte[] b, int off, int len) {
    super.write(b, off, len);
    textarea.flush();
  }

  public synchronized void write(int b) {
    super.write(b);
    textarea.flush();
  }

  public void write(byte[] b) throws IOException {
    super.write(b);
    textarea.flush();
  }

}