package umlcheker;

import javax.swing.JOptionPane;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.project.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.view.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.exception.*;
import com.change_vision.jude.api.inf.view.IEntitySelectionListener;
import com.change_vision.jude.api.inf.project.ProjectEventListener;

import java.awt.geom.Point2D;

public class Activator implements BundleActivator {

	MySelectionListener selectionListener = new MySelectionListener();
	public static IPresentation[] actpre;
	public static IDiagram actdgm;
	public static int selectnum;
	ChangedSliceColor csc = new ChangedSliceColor();

	public static int addcount = 0;
	public static int modifycount = 0;
	public static int removecount = 0;

	public void start(BundleContext context) {
		try {
			getDiagramViewManager().addEntitySelectionListener(selectionListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop(BundleContext context) {
		try {
			getDiagramViewManager().removeEntitySelectionListener(selectionListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private IDiagramViewManager getDiagramViewManager()
			throws ClassNotFoundException, InvalidUsingException {
		AstahAPI api = AstahAPI.getAstahAPI();
		ProjectAccessor projectAccessor = api.getProjectAccessor();
		IDiagramViewManager vm = projectAccessor.getViewManager().getDiagramViewManager();
		return vm;
	}

	/**
	 * My SelectionListener.
	 */
	boolean catchflag = false;
	IPresentation keepele = null;
	int selectcount = 0;

	public class MySelectionListener implements IEntitySelectionListener{
		@Override
		public void entitySelectionChanged(IEntitySelectionEvent arg0) {
			try{
				IDiagramViewManager iDVM = getDiagramViewManager();
				IDiagram dgm = iDVM.getCurrentDiagram();
				selectnum = iDVM.getSelectedPresentations().length;
				if(selectnum == 0 && ExtensionView.sliceviewerflag == 1){
					csc.changeSliceColor();
				}
				if(selectnum == 0 && catchflag == true){
					//System.out.println(keepele.getID());
					catchflag = false;
				}
				if(selectnum > 0){
					IPresentation[] selectpres = iDVM.getSelectedPresentations();
					//System.out.println(selectpres.length);
					//System.out.println(selectpres[0].getProperty("fill.color"));
					for(IPresentation selectpre : selectpres){
						//System.out.println(selectpre + " select");
						keepele = selectpre;
						//System.out.println(selectpre.getID());
					}
					catchflag = true;
					Activator.actpre = selectpres;
					Activator.actdgm = dgm;
					if(selectpres[0].getType().equals("ControlFlow/ObjectFlow")){
						if(ExtensionView.sliceviewerflag == 1 && selectnum == 1 && ChangedSliceColor.cmflag == false){
							IFlow se1 = (IFlow)(selectpres[0].getModel());
							csc.changeSliceColor();
							csc.changematchColor(se1);
						}
					}
				}
			} catch(Exception e){
				System.out.println(e);
			}
		}

	}

}