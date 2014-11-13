package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import fr.proline.studio.rsmexplorer.gui.MSDiagOutput_AW;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.export.ImageExporterInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.MSDiagTable_GenericTable.*;
import fr.proline.studio.rsmexplorer.gui.MSDiagTable_1stColumnIsString.*;
import fr.proline.studio.utils.IconManager;


/**
 * Panel used to display MSDiag content
 *
 * @author AW
 */
public class RsetMSDiagPanel extends HourglassPanel implements DataBoxPanelInterface, ImageExporterInterface  {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;
    private AbstractDataBox m_dataBox;
   
   
    private ResultSet m_previousrSet = null;
    private javax.swing.JPanel m_MSDiagPanel;
    public javax.swing.JTextArea m_textArea = new JTextArea("data text area initialized line 73");
    
    private MSDiagTable_GenericTable m_msdiagTable_massesPerCharge = null;
    private MSDiagTable_1stColumnIsString m_msdiagTable_matchesPerScanAndScore = null;
    private MSDiagTable_GenericTable m_msdiagTable_matchesPerChargeAndScore = null;
    private MSDiagTable_GenericTable m_msdiagTable_assignementRepartition = null;
    private MSDiagTable_1stColumnIsString m_msdiagTable_matchesPerResultSetAndScore = null;
    private MSDiagTable_GenericTable m_msdiagTable_matchesPerMinuteAndScore = null;
    private MSDiagTable_GenericTable m_msdiagTable_massesPerScore = null;
    
    
    
    @Override // declared in ProlineStudioCommons ImageExporterInterface
    public void generateSvgImage(String file) {
       // writeToSVG(file);
    }
    
    @Override // declared in ProlineStudioCommons ImageExporterInterface
    public void generatePngImage(String file) {
       // writeToPNG(file);
    }
    
    @Override
    public String getSupportedFormats() {
        return "png,svg";
    }
    
    /**
     * Creates new form RsetMSDiagPanel
     */
    public RsetMSDiagPanel(String message) {
        
       
        setLayout(new BorderLayout());
        
        
        JTabbedPane internalPanel = createInternalPanel();
        
        add(internalPanel, BorderLayout.CENTER);
             
        JPanel m_MSDiagPanel = new JPanel();
        m_MSDiagPanel.setLayout(new GridLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        ///
        //m_MSDiagPanel.setBounds(0, 0, this.getWidth(), this.getHeight());

        m_MSDiagPanel.add(internalPanel, new GridLayout(0,1));

        JToolBar toolbar = initToolbar();
        add(internalPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);
        


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        
        
        
    }
    public final JToolBar initToolbar() {
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        //m_picWrapper = new ExportPictureWrapper();
        //m_picWrapper.setFile(m_svgFile);

//        ExportButton exportImageButton = new ExportButton("MSDiag", (ImageExporterInterface) this);
//        toolbar.add(exportImageButton);
        
        
        return toolbar;
        
    }

    public final JTabbedPane createInternalPanel() {
        
    	JTabbedPane jtabbedPane = new JTabbedPane();
        
    	ImageIcon icon = IconManager.getIcon(IconManager.IconType.CHART);
    	
    	JPanel internalPanel = new JPanel();
        
    	
        internalPanel.setLayout(new GridLayout(0, 1));
           
        internalPanel.setBackground(Color.white);
        
        // m_msdiagTable_massesPerCharge
        JScrollPane scrollPane = new JScrollPane();
        m_msdiagTable_massesPerCharge = new MSDiagTable_GenericTable();
        m_msdiagTable_massesPerCharge.setModel(new MSdiagTable_GenericTableModel());
        scrollPane.setViewportView(m_msdiagTable_massesPerCharge);
        jtabbedPane.addTab("Masses Per Charge",icon, scrollPane); 
        
        //m_msdiagTable_matchesPerScanAndScore
        JScrollPane scrollPane2 = new JScrollPane();
        m_msdiagTable_matchesPerScanAndScore = new MSDiagTable_1stColumnIsString();
        m_msdiagTable_matchesPerScanAndScore.setModel(new MSDiagTable_1stColumnIsStringModel());
        scrollPane2.setViewportView(m_msdiagTable_matchesPerScanAndScore);
        jtabbedPane.addTab("Matches Per Scan And Score",icon, scrollPane2); 

        
        // m_msdiagTable_assignementRepartition
        JScrollPane scrollPane3 = new JScrollPane();
        m_msdiagTable_assignementRepartition = new MSDiagTable_GenericTable(); 
        m_msdiagTable_assignementRepartition.setModel(new MSdiagTable_GenericTableModel());
        scrollPane3.setViewportView(m_msdiagTable_assignementRepartition);
        jtabbedPane.addTab("Assignement Repartition",icon, scrollPane3); 
        
        // m_msdiagTable_matchesPerResultSetAndScore
        JScrollPane scrollPane4 = new JScrollPane();
        m_msdiagTable_matchesPerResultSetAndScore = new MSDiagTable_1stColumnIsString();
        m_msdiagTable_matchesPerResultSetAndScore.setModel(new MSDiagTable_1stColumnIsStringModel());
        scrollPane4.setViewportView(m_msdiagTable_matchesPerResultSetAndScore);
        jtabbedPane.addTab("Matches Per ResultSet And Score",icon, scrollPane4); 

      
        // m_msdiagTable_matchesPerChargeAndScore
        JScrollPane scrollPane5 = new JScrollPane();
        m_msdiagTable_matchesPerChargeAndScore = new MSDiagTable_GenericTable();
        m_msdiagTable_matchesPerChargeAndScore.setModel(new MSdiagTable_GenericTableModel());
        scrollPane5.setViewportView(m_msdiagTable_matchesPerChargeAndScore);
        jtabbedPane.addTab("Matches Per Charge And Score",icon, scrollPane5); 

      //m_msdiagTable_matchesPerMinuteAndScore
        JScrollPane scrollPane6 = new JScrollPane();
        m_msdiagTable_matchesPerMinuteAndScore = new MSDiagTable_GenericTable();
        m_msdiagTable_matchesPerMinuteAndScore.setModel(new MSdiagTable_GenericTableModel());
        scrollPane6.setViewportView(m_msdiagTable_matchesPerMinuteAndScore);
        jtabbedPane.addTab("Matches Per Minute And Score",icon, scrollPane6); 

        
      //m_msdiagTable_massesPerScore
        JScrollPane scrollPane7 = new JScrollPane();
        m_msdiagTable_massesPerScore = new MSDiagTable_GenericTable();
        m_msdiagTable_massesPerScore.setModel(new MSdiagTable_GenericTableModel());
        scrollPane7.setViewportView(m_msdiagTable_massesPerScore);
        jtabbedPane.addTab("Masses per score",icon, scrollPane7); 

        
        return jtabbedPane; //internalPanel;
    }
  
  
    public void setData(String jsonMessageHashMapJson) {
        
    	
        launchMSDiag(jsonMessageHashMapJson);
 
       
    }
    
      
    private void launchMSDiag(String messageHashMapJson) {
        
    	
    	// data is encoded in JSON string, subformed of other json strings!!!
        
        final String SERIES_NAME = "MSDiag data";
        if(messageHashMapJson != null) {
        
	        if (messageHashMapJson.length() == 0) {
	        	
	        	
	        } 
	        else 
	        {
	        	 
	        	if(messageHashMapJson.startsWith("{")) {
	         	
	         	
	        	Gson gson = new Gson();
	        	
	        	HashMap<String,String> msOutputHashMap = new HashMap<String,String>();
	        	msOutputHashMap = gson.fromJson(messageHashMapJson, msOutputHashMap.getClass());
	        	
	        	
	        	if(msOutputHashMap != null) {
	        		//--------------
 	        		        			        			
        			String msOutputString1 = msOutputHashMap.get("MassesPerCharge");
        			MSDiagOutput_AW msOutput1 = gson.fromJson(msOutputString1, MSDiagOutput_AW.class);  
			        ((MSdiagTable_GenericTableModel) m_msdiagTable_massesPerCharge.getModel()).setData(msOutput1);
			    //--------------
				    String msOutputString2 = msOutputHashMap.get("MatchesPerScanAndScore");
        			MSDiagOutput_AW msOutput2 = gson.fromJson(msOutputString2, MSDiagOutput_AW.class);  
			        ((MSDiagTable_1stColumnIsStringModel) m_msdiagTable_matchesPerScanAndScore.getModel()).setData(msOutput2);
				//--------------
			        String msOutputString3 = msOutputHashMap.get("AssignementRepartition");
        			MSDiagOutput_AW msOutput3 = gson.fromJson(msOutputString3, MSDiagOutput_AW.class);  
			        ((MSdiagTable_GenericTableModel) m_msdiagTable_assignementRepartition.getModel()).setData(msOutput3);
				//--------------
			        String msOutputString4 = msOutputHashMap.get("MatchesPerResultSetAndScore");
        			MSDiagOutput_AW msOutput4 = gson.fromJson(msOutputString4, MSDiagOutput_AW.class);  
			        ((MSDiagTable_1stColumnIsStringModel) m_msdiagTable_matchesPerResultSetAndScore.getModel()).setData(msOutput4);
			        
			     // MSDiagTable_MatchesPerScanAndScore
			        String msOutputString5 = msOutputHashMap.get("MatchesPerChargeAndScore");
        			MSDiagOutput_AW msOutput5 = gson.fromJson(msOutputString5, MSDiagOutput_AW.class);  
			        ((MSdiagTable_GenericTableModel) m_msdiagTable_matchesPerChargeAndScore.getModel()).setData(msOutput5);
			        
			     // m_msdiagTable_matchesPerMinuteAndScore
			        String msOutputString6 = msOutputHashMap.get("MatchesPerMinuteAndScore");
        			MSDiagOutput_AW msOutput6 = gson.fromJson(msOutputString6, MSDiagOutput_AW.class);  
			        ((MSdiagTable_GenericTableModel) m_msdiagTable_matchesPerMinuteAndScore.getModel()).setData(msOutput6);
			        
			     // m_msdiagTable_massesPerScore
			        String msOutputString7 = msOutputHashMap.get("MassesPerScore");
        			MSDiagOutput_AW msOutput7 = gson.fromJson(msOutputString7, MSDiagOutput_AW.class);  
			        ((MSdiagTable_GenericTableModel) m_msdiagTable_massesPerScore.getModel()).setData(msOutput7);
			        
				    this.repaint();
	        		
		        	}
	        	}
	        }
        }
        
    }
    
   
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }
    
    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
   
    
    public static byte[] floatsToBytes(float[] floats) {

        // Convert float to a byte buffer
        ByteBuffer byteBuf = ByteBuffer.allocate(4 * floats.length).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < floats.length; i++) {
            byteBuf.putFloat(floats[i]);
        }
        // Convert byte buffer into a byte array
        return byteBuf.array();
    }

	
    @Override
	public AbstractDataBox getDataBox() {
	       return m_dataBox;
	}
	   
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

	    
}


