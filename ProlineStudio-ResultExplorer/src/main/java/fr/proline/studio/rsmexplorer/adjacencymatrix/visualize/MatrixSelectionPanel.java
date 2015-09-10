package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ImageExporterInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 *
 * @author JM235353
 */
public class MatrixSelectionPanel extends HourglassPanel implements DataBoxPanelInterface {
    
    private AbstractDataBox m_dataBox;
    
    private DrawVisualization m_drawVisualization = null; 

    
    private static HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap ;
    private ArrayList<Component> m_componentList;

    private Component m_currentComponent = null;
    private MatrixImageButton m_currentImageButton = null;
    
    private InternalPanel m_internalPanel = null;
    
    public MatrixSelectionPanel() {
        setLoading(0);
        setBackground(Color.white);
        
        setLayout(new BorderLayout());
        
        
        m_internalPanel = new InternalPanel();
         
        JToolBar toolbar = initToolbar();
        
       
        
        add(toolbar, BorderLayout.WEST);
        add(m_internalPanel, BorderLayout.CENTER);
        
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        ExportButton exportImageButton = new ExportButton("Adjacency Matrices", m_internalPanel);
        toolbar.add(exportImageButton);

        return toolbar;
    }
    
    public class InternalPanel extends JPanel {
        private InternalPanel() {
            setBackground(Color.white);
        }
        
        private void initPanel() {
                        
        // search filter order
        m_peptideToProteinMap = m_drawVisualization.get_peptideToProteinMap();
        m_componentList = filterComponents(m_drawVisualization.get_ComponentList());

        //Sort clist
        Collections.sort(m_componentList, new CustomComparator());

        setLayout(new BorderLayout());
        
        // create objects

        JPanel horizontalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        horizontalPanel.setBackground(Color.white);
        horizontalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        int nbComponents = m_componentList.size();
        for (int i=0;i<nbComponents;i++) {
            final Component component = m_componentList.get(i);
            final MatrixImageButton curImageButton =    new MatrixImageButton(i, component, m_drawVisualization);
            if (m_currentImageButton == null) {
                m_currentComponent = component;
                m_currentImageButton = curImageButton;
                curImageButton.setSelection(true);
            }
            
            curImageButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    if (m_currentImageButton != null) {
                        m_currentImageButton.setSelection(false);
                    }
                    curImageButton.setSelection(true);
                    m_currentImageButton = curImageButton;

                    setCurrentComponent(component);
                    m_dataBox.propagateDataChanged(Component.class);

                    repaint();

                }

            });
            
            horizontalPanel.add(curImageButton);
        }

        JScrollPane scroll = new JScrollPane(horizontalPanel);

        // add panels to the MatrixSelectionPanel
        add(scroll, BorderLayout.CENTER);

        m_dataBox.propagateDataChanged(Component.class);

        }
    }

    
    public DrawVisualization getDrawVisualization() {
        return m_drawVisualization;
    }
    
    public void setData(AdjacencyMatrixData matrixData) {
        
        m_drawVisualization = new DrawVisualization();
        
        m_drawVisualization.setData(matrixData);

        m_internalPanel.initPanel();
        
        setLoaded(0);
        
        revalidate(); 
        repaint();
    }
    
    private ArrayList<Component> filterComponents(ArrayList<Component> cList) {
        ArrayList<Component> subCList = new ArrayList<>();
        for (Component temp : cList) {
            if (temp.peptideSet.size() > 1 && temp.proteinSet.size() > 1) {
                if (!fullMatch(temp)) {
                    subCList.add(temp);
                }
            }
        }

        return subCList;
    }
    
    private boolean fullMatch(Component temp) {
        for (LightPeptideMatch peptTemp : temp.peptideSet) {
            ArrayList<LightProteinMatch> protList = m_peptideToProteinMap.get(peptTemp);
            for (LightProteinMatch protTemp : temp.proteinSet) {
                if (!protList.contains(protTemp)) {
                    return false;
                }
            }

        }
        return true;
    }
    
    public void setCurrentComponent(Component c) {
        m_currentComponent = c;
    }
    
    public Component getCurrentComponent() {
        return m_currentComponent;
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        //JPM.TODO
        return null;
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
       //JPM.TODO
        return null; 
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        //JPM.TODO
        return null;
    }

    


    public class CustomComparator implements Comparator<Component> {

        @Override
        public int compare(Component o1, Component o2) {

            if (o1 == null || o2 == null) {
                throw new NullPointerException();
            }

            return (o2.getPeptideSize() * o2.getProteinSize()) - (o1.getPeptideSize() * o1.getProteinSize());
        }
    }


    
}

