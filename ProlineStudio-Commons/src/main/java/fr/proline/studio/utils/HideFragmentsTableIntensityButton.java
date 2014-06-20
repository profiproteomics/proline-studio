package fr.proline.studio.utils;

import fr.proline.studio.utils.IconManager;





import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import java.util.List;

import javax.swing.JButton;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.slf4j.LoggerFactory;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Button to export data of a table or image of a JPanel
 * @author AW
 */
public class HideFragmentsTableIntensityButton extends JButton implements ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    private JXTable m_table = null;
   private int m_startCol = 0;
    private int m_endCol = 0;
    private boolean m_isPressed = false;
    
            
    public HideFragmentsTableIntensityButton(JXTable table, int startCol, int endCol, boolean hideIntensity) {

        m_table = table; 
        m_startCol = startCol;
        m_endCol = endCol;
        setIcon(IconManager.getIcon(IconManager.IconType.ADD_REMOVE));
        setToolTipText("Show/hide matching fragments intensity...");
        // toggle hide/show intensity 
        
        addActionListener(this);
        showFragmentsIntensity(m_table, m_startCol, m_endCol, m_isPressed);
    	
    }
    
    public void setInterval (int startCol, int endCol) {
       
    	m_startCol = startCol;
    	m_endCol = endCol;
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {

        if(m_isPressed)
        {	
        	m_isPressed = false; // toggle flip/flop button value
        	
        }
        else {
        	
        	m_isPressed = true; // toggle flip/flop button value
        }
        this.setSelected(m_isPressed);
        
        showFragmentsIntensity(m_table, m_startCol, m_endCol, m_isPressed);
    	 	
     
    }
    
    public void showFragmentsIntensity(JXTable myTable, int colStart, int colEnd, boolean show) {
    	
    
    	String intensityStringIdentifier = "(I)";
    	
    	List<TableColumn> columns;
    	// get all columns including hidden ones
    	columns = myTable.getColumns(true);
    	
    	if(columns != null)  {
    		if(columns.size() > 0) {
    			//for (int i = colEnd -1 ; i>colStart; i--) { // could use only specific column range 
    			for (int i = columns.size() -1 ; i>0; i--) { // use all columns but check for an identifier in the title
    				TableColumn currentColumn = columns.get(i);
    				TableColumnExt tce= myTable.getColumnExt(currentColumn.getIdentifier());
						if(tce.getTitle().contains(intensityStringIdentifier)) {
							tce.setVisible(show);
						}
											
				}
    		}
    		
    	}
    			
	}

}
