package fr.proline.mzscope.ui;

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.ui.event.DisplayFeatureListener;
import fr.proline.mzscope.util.NumberFormatter;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableColumnModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * panel that contains the features/peaks table
 *
 * @author CB205360
 */
public class FeaturesPanel extends JPanel implements RowSorterListener, MouseListener {

    final private static Logger logger = LoggerFactory.getLogger(FeaturesPanel.class);

    private IRawFile rawFile;
    private List<Feature> features = new ArrayList<Feature>();
    private int modelSelectedIdxBeforeSort = -1;

    private JXTable featureTable;
    private FeaturesTableModel featureTableModel;
    private JScrollPane jScrollPane;
    
    //events
    private EventListenerList displayFeatureListenerList = new EventListenerList();

    public FeaturesPanel() {
        initComponents();
    }

    public FeaturesPanel(IRawFile rawFile) {
        this.rawFile = rawFile;
        initComponents();
        
        TableColumnModel columnModel = featureTable.getColumnModel();
        for (int k = 0; k < columnModel.getColumnCount(); k++) {
            switch (FeaturesTableModel.Columns.values()[k]) {
                case MZ_COL:
                    columnModel.getColumn(k).setCellRenderer(new DefaultTableRenderer(new NumberFormatter("#.0000"), JLabel.RIGHT));
                    break;
                case ET_COL:
                case DURATION_COL:
                    columnModel.getColumn(k).setCellRenderer(new DefaultTableRenderer(new NumberFormatter("#0.00"), JLabel.RIGHT));
                    break;
                case APEX_INT_COL:
                case AREA_COL:
                    columnModel.getColumn(k).setCellRenderer(new DefaultTableRenderer(new NumberFormatter("#,###,###"), JLabel.RIGHT));
                    break;
            }
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        featureTableModel = new FeaturesTableModel();
        jScrollPane = new JScrollPane();
        featureTable = new JXTable();

        featureTable.setModel(featureTableModel);
        featureTable.setColumnControlVisible(true);
        featureTable.setEditable(false);
        featureTable.setShowGrid(true);
        featureTable.setShowHorizontalLines(false);
        featureTable.addHighlighter(HighlighterFactory.createSimpleStriping());
        featureTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                featureTableMouseClicked(evt);
            }
        });
        featureTable.getRowSorter().addRowSorterListener(this);
        
        jScrollPane.setViewportView(featureTable);
        
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemViewRawFile = new JMenuItem("Display in the raw file");
        menuItemViewRawFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (featureTable.getSelectedRow() == -1) {
                    return;
                }
                Feature f = features.get(featureTable.convertRowIndexToModel(featureTable.getSelectedRow()));
                fireDisplayFeature(f, rawFile);
            }
        });
        JMenuItem menuItemViewSelectedRawFile = new JMenuItem("Display in the selected raw file");
        menuItemViewSelectedRawFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Feature f = features.get(featureTable.convertRowIndexToModel(featureTable.getSelectedRow()));
                fireDisplayFeatureInCurrentRawFile(f);
            }
        });
 
        popupMenu.add(menuItemViewRawFile);
        popupMenu.add(menuItemViewSelectedRawFile);
        featureTable.setComponentPopupMenu(popupMenu);
        
        this.add(jScrollPane, BorderLayout.CENTER);
    }

    public void setFeatures(List<Feature> features) {
        modelSelectedIdxBeforeSort = -1;
        featureTableModel.setFeatures(features);
        this.features = features;
    }

    private void featureTableMouseClicked(MouseEvent evt) {
        if ((features != null) && (!features.isEmpty()) && (rawFile != null)
                && (evt.getClickCount() == 2) && (featureTable.getSelectedRow() != -1)) {
            Feature f = features.get(featureTable.convertRowIndexToModel(featureTable.getSelectedRow()));
            fireDisplayFeature(f, rawFile);
        }
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
        if (featureTable.getSelectedRow() == -1) {
            return;
        }
        if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
            modelSelectedIdxBeforeSort = featureTable.convertRowIndexToModel(featureTable.getSelectedRow());
        } else if (modelSelectedIdxBeforeSort != -1) {
            int idx = featureTable.convertRowIndexToView(modelSelectedIdxBeforeSort);
            featureTable.scrollRectToVisible(new Rectangle(featureTable.getCellRect(idx, 0, true)));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // selects the row at which point the mouse is clicked
        Point point = e.getPoint();
        int currentRow = featureTable.rowAtPoint(point);
        featureTable.setRowSelectionInterval(currentRow, currentRow);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }
    
    /**
     * event register
     * @param listener 
     */
    public void addDisplayFeatureListener(DisplayFeatureListener listener) {
        displayFeatureListenerList.add(DisplayFeatureListener.class, listener);
    }

    public void removeDisplayFeatureListener(DisplayFeatureListener listener) {
        displayFeatureListenerList.remove(DisplayFeatureListener.class, listener);
    }
    
    private void fireDisplayFeature(Feature f, IRawFile rawFile) {
        Object[] listeners = displayFeatureListenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == DisplayFeatureListener.class) {
                ((DisplayFeatureListener) listeners[i + 1]).displayFeatureInRawFile(f, rawFile);
            }
        }
    }

    private void fireDisplayFeatureInCurrentRawFile(Feature f) {
        Object[] listeners = displayFeatureListenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == DisplayFeatureListener.class) {
                ((DisplayFeatureListener) listeners[i + 1]).displayFeatureInCurrentRawFile(f);
            }
        }
    }
}
