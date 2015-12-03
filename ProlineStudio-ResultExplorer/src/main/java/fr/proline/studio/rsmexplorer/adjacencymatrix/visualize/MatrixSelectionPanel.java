package fr.proline.studio.rsmexplorer.adjacencymatrix.visualize;

import fr.proline.studio.dam.tasks.data.AdjacencyMatrixData;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.dam.tasks.data.LightPeptideMatch;
import fr.proline.studio.dam.tasks.data.LightProteinMatch;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterMapInterface;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.search.ApplySearchInterface;
import fr.proline.studio.search.SearchInterface;
import fr.proline.studio.search.SearchToggleButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;


/**
 *
 * @author JM235353
 */
public class MatrixSelectionPanel extends HourglassPanel implements DataBoxPanelInterface, SearchInterface, FilterMapInterface, ApplySearchInterface {
    
    private AbstractDataBox m_dataBox;
    
    private DrawVisualization m_drawVisualization = null; 

    
    private HashMap<LightPeptideMatch, ArrayList<LightProteinMatch>> m_peptideToProteinMap ;
    private ArrayList<Component> m_componentList;
    private ArrayList<MatrixImageButton> m_curImageButtonArray;

    private Component m_currentComponent = null;
    private MatrixImageButton m_currentImageButton = null;
    
    
    private InternalPanel m_internalPanel = null;
    private JScrollPane m_scrollPane = null;
    
    private SearchToggleButton m_searchToggleButton;
    
    private static final int PROTEIN = 0;
    private static final int PEPTIDE = 1;
    
    public MatrixSelectionPanel() {
        setLoading(0);
        setLayout(new BorderLayout());


        final JPanel matrixPanel = createMatrixPanel();

        final JLayeredPane layeredPane = new JLayeredPane();

        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                final java.awt.Component c = e.getComponent();

                matrixPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                layeredPane.revalidate();
                layeredPane.repaint();

            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
        add(layeredPane, BorderLayout.CENTER);

        layeredPane.add(matrixPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(m_searchToggleButton.getSearchPanel(), JLayeredPane.PALETTE_LAYER);
    }
    
    private JPanel createMatrixPanel() {
        JPanel matrixPanel = new JPanel(new BorderLayout());
         matrixPanel.setBackground(Color.white);
         
         m_internalPanel = new InternalPanel();
         
        JToolBar toolbar = initToolbar();

        matrixPanel.add(toolbar, BorderLayout.WEST);
        matrixPanel.add(m_internalPanel, BorderLayout.CENTER);
        
        return matrixPanel;
    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        m_searchToggleButton = new SearchToggleButton(this, null, this, this); //JPM.TODO
        
        ExportButton exportImageButton = new ExportButton("Adjacency Matrices", m_internalPanel);
        
        toolbar.add(m_searchToggleButton);
        toolbar.add(exportImageButton);

        return toolbar;
    }

    @Override
    public int search(Filter f, boolean newSearch) {
       
        if (m_searchIds == null) {
            m_searchIds = new ArrayList<>();
            newSearch = true;
        }
            
        if (newSearch) {
            m_searchIndex = 0;
            m_searchIds.clear();
            int nb = m_componentList.size();
            for (int i = 0; i < nb; i++) {

                
                int type = f.getModelColumn();
                boolean found = filter(f, i, type);
                if (found) {
                    m_searchIds.add(i);
                }
            }
        } else {
            m_searchIndex++;
            if (m_searchIndex>=m_searchIds.size()) {
                m_searchIndex = 0;
            }
        }
        
        if (m_searchIds.isEmpty()) {
            return -1;
        }
        return m_searchIds.get(m_searchIndex);

    }
    private ArrayList<Integer> m_searchIds = null;
    private int m_searchIndex = 0;
    
    private boolean filter(Filter filter, int index, int type) {
        if (type == PROTEIN) {
            Component c = m_componentList.get(index);
            ArrayList<LightProteinMatch> proteinSet = c.proteinSet;
            int nbProteins = proteinSet.size();
            for (int i=0;i<nbProteins;i++) {
                LightProteinMatch pm = proteinSet.get(i);
                if (((StringFilter) filter).filter((String)pm.getAccession(), null)) {
                    return true;
                }
            }
        } else if (type == PEPTIDE) {
            Component c = m_componentList.get(index);
            ArrayList<LightPeptideMatch> peptideSet = c.peptideSet;
            int nbPeptides = peptideSet.size();
            for (int i=0;i<nbPeptides;i++) {
                LightPeptideMatch pm = peptideSet.get(i);
                if (((StringFilter) filter).filter((String)pm.getSequence(), null)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public LinkedHashMap<Integer, Filter> getFilters() {
        
        LinkedHashMap<Integer, Filter> filtersMap = new LinkedHashMap<>(2);
        
        StringFilter proteinFilter = new StringFilter("Protein", null, PROTEIN);
        filtersMap.put(PROTEIN, proteinFilter);
        
        StringFilter peptideFilter = new StringFilter("Peptide", null, PEPTIDE);
        filtersMap.put(PEPTIDE, peptideFilter);
        
        return filtersMap;
    }

    @Override
    public void doSearch(Filter f, boolean firstSearch) {
        int modelRowIndex = search(f, firstSearch);
        if (modelRowIndex == -1) {
            return;
        }
        
        MatrixImageButton imageButton = m_curImageButtonArray.get(modelRowIndex);
        imageButton.doClick();
        
        Rectangle visibleRectangle = m_scrollPane.getBounds();
        double x1 = visibleRectangle.getX()+m_scrollPane.getHorizontalScrollBar().getValue();
        double x2 = x1+visibleRectangle.getWidth();
        
        Point imageButtonLocation = imageButton.getLocation();
        
        if ((imageButtonLocation.getX() < x1) || (imageButtonLocation.getX() + imageButton.getWidth() >x2)) {
            m_scrollPane.getViewport().setViewPosition(imageButton.getLocation());
        }
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
        m_curImageButtonArray  = new ArrayList<>(nbComponents);
        for (int i=0;i<nbComponents;i++) {
            final Component component = m_componentList.get(i);
            final MatrixImageButton curImageButton = new MatrixImageButton(i, component, m_drawVisualization);
            m_curImageButtonArray.add(curImageButton);
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

        m_scrollPane = new JScrollPane(horizontalPanel);

        // add panels to the MatrixSelectionPanel
        add(m_scrollPane, BorderLayout.CENTER);

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
    public void addSingleValue(Object v) {
        // not used for the moment JPM.TODO ?
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
       return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
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

