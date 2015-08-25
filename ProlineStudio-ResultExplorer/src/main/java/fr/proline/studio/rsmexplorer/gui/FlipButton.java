package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXTable;
import org.openide.windows.WindowManager;

/**
 * Button to export data of a table or image of a JPanel
 * @author AW
 */

public class FlipButton extends JButton implements ActionListener {

	    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String m_exportName;
   private JXTable m_table = null;
   //private JPanel m_panel = null;
   private RsetMSDiagPanel msdiagPanel = null;
   private String m_buttonStatus = "default";
  //  private ImageExporterInterface  m_imageExporter = null;
    
    private ProgressInterface m_progressInterface = null;
            
    public FlipButton(ProgressInterface progressInterface, String exportName, JXTable table) {

        setProgressInterface(progressInterface);
        
        m_exportName = exportName;
        m_table = table;
      
        
       	setIcon(IconManager.getIcon(IconManager.IconType.TABLE));
        
        setToolTipText("Export Data...");
        setFocusPainted(false);

        addActionListener(this);
    }
    
    public final void setProgressInterface(ProgressInterface progressInterface) {
        m_progressInterface = progressInterface;
    }
    
    
    public FlipButton(String exportName/*,  ImageExporterInterface exportablePicture*/) {


        m_exportName = exportName;
        msdiagPanel = null;
       // m_imageExporter = exportablePicture;

       
        setIcon(IconManager.getIcon(IconManager.IconType.TABLE));
       
        
        setToolTipText("Show predefined graphics/table or force table display...");

        addActionListener(this);
    }
    
    public FlipButton(String exportName, RsetMSDiagPanel msdiagPanel) {


        m_exportName = exportName;
        this.msdiagPanel = msdiagPanel;
       // m_imageExporter = null;

        if(m_buttonStatus.equals("default")) {
	        	setIcon(IconManager.getIcon(IconManager.IconType.TABLE));
	        	msdiagPanel.setDisplayType("default");
	        	m_buttonStatus="default";
	        	
	    }
	    else {
	        	setIcon(IconManager.getIcon(IconManager.IconType.CHART));
	        	msdiagPanel.setDisplayType("table");
	        	m_buttonStatus="table";
	        	
	    }
        System.out.println(m_buttonStatus);
        
        setToolTipText("Show predefined graphics/table or force table display...");
       // setToolTipText("Export Image...");
      
        addActionListener(this);
    }
 
    

    @Override
    public void actionPerformed(ActionEvent e) {

    	System.out.println("action performed: " + m_buttonStatus);
        if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Export is not available while data is loading. Please Wait.");
            dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return;
            }
        }
         // add here the action to be performed, for example switch between graph and table...
	       
        if(m_buttonStatus.equals("table")) {
        	setIcon(IconManager.getIcon(IconManager.IconType.TABLE));
        	msdiagPanel.setDisplayType("default");
        	m_buttonStatus="default";
        	
	    }
	    else {
        	setIcon(IconManager.getIcon(IconManager.IconType.CHART));
        	msdiagPanel.setDisplayType("table");
        	m_buttonStatus="table";
	        	
	    }
        int selectedTabIndex = msdiagPanel.m_tabbedPane.getSelectedIndex();
        msdiagPanel.m_tabbedPane.removeAll();
        msdiagPanel.displayData();
	    msdiagPanel.m_tabbedPane.setSelectedIndex(selectedTabIndex);
	    System.out.println(m_buttonStatus);
    
	    
//	        ExportDialog dialog;
//	        
//	        if (m_table != null) {
//	            dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_table, m_exportName);
//	        }
//
//	        else if (m_imageExporter == null){ // then png output only
//	            dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_panel, m_exportName);
//	        }
//	        else { 
//	            dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_panel, m_imageExporter, m_exportName);
//	        }
//	        
//	        dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
//	        dialog.setVisible(true);

	}
	
}
