package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.QuantitationMethod;
import fr.proline.studio.WindowManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.editor.FloatTableCellEditor;
import fr.proline.studio.rsmexplorer.gui.model.AbstractCorrectionMatrixTaleModel;
import fr.proline.studio.rsmexplorer.gui.model.PurityCorrectionMatrixTableModel;
import fr.proline.studio.rsmexplorer.gui.model.ThermoCorrectionMatrixTableModel;
import fr.proline.studio.table.*;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.utils.IconManager;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Arrays;

public class EditIsobaricMatrixDialog extends DefaultDialog {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    private static final String[] KNOWN_TMT_METHODS = {
            AbstractCorrectionMatrixTaleModel.TMT_6PLEX_METHOD,
            AbstractCorrectionMatrixTaleModel.TMT_10PLEX_METHOD,
            AbstractCorrectionMatrixTaleModel.TMT_11PLEX_METHOD,
            AbstractCorrectionMatrixTaleModel.TMT_16PLEX_METHOD,
            AbstractCorrectionMatrixTaleModel.TMT_18PLEX_METHOD};
    private static final String KNOWN_HELP_MESSAGE ="Enter values as given by Thermo for this TMT Plex";
    private static final String UNKNOWN_HELP_MESSAGE ="Enter the matrix specifying purity correction to use";
    private QuantitationMethod m_quantMethod;
    boolean m_isGenericMatrix;
    JScrollPane m_tableScrollPane;
    PurityCorrectionMatrixTable m_purityCorrectionTable;
    public EditIsobaricMatrixDialog(Window parent, QuantitationMethod method) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setButtonVisible(BUTTON_DEFAULT, true);
        setButtonName(BUTTON_DEFAULT, "Clear");
        setButtonIcon(BUTTON_DEFAULT, IconManager.getIcon(IconManager.IconType.ERASER));
        this.m_quantMethod = method;
        m_isGenericMatrix = (Arrays.stream(KNOWN_TMT_METHODS).anyMatch(c->c.equals(m_quantMethod.getName()))) ? false : true;
        setTitle("Edit Purity Matrix for "+m_quantMethod.getName());
        setResizable(true);
        initDialog();
        m_purityCorrectionTable.packAll();
        pack();
    }

    private void initDialog(){
        setButtonVisible(BUTTON_HELP,false);
        if(m_isGenericMatrix){
            setHelpHeader("Purity Correction Matrix", UNKNOWN_HELP_MESSAGE);
        } else
            setHelpHeader("Thermo Correction Matrix", KNOWN_HELP_MESSAGE);

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy=0;
        c.gridx=0;
        c.insets=new Insets(5,10,5,5);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        m_tableScrollPane = new JScrollPane();
        m_purityCorrectionTable = createTable();

        m_tableScrollPane.setViewportView(m_purityCorrectionTable);
        m_tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_purityCorrectionTable.setFillsViewportHeight(true);
        m_purityCorrectionTable.setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);


        internalPanel.add(m_tableScrollPane, c );
        setInternalComponent(internalPanel);
        int windowWitdh =  (WindowManager.getDefault().getMainWindow() == null) ? m_preferredDialogWidth : WindowManager.getDefault().getMainWindow().getWidth();
        setPreferredSize(new Dimension(Math.min(m_preferredDialogWidth, windowWitdh),500));
    }

    private int m_preferredDialogWidth;
    PurityCorrectionMatrixTable createTable(){
        m_preferredDialogWidth =0;
        PurityCorrectionMatrixTable table = new PurityCorrectionMatrixTable();
        table.removeStriping();
        table.getTableHeader().setDefaultRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.CENTER));
        table.setCellSelectionEnabled(true);
        AbstractCorrectionMatrixTaleModel tableModel = (m_isGenericMatrix ?  new PurityCorrectionMatrixTableModel( m_quantMethod) : new ThermoCorrectionMatrixTableModel(m_quantMethod.getName()));

        table.setModel(tableModel);
        TableColumnModel columnModel = table.getColumnModel();

        // set preferred width of different columns
        int colCount= tableModel.getColumnCount();
        for(int i=0; i<colCount; i++){
            if(i == tableModel.getCenterColIndex()) {
                columnModel.getColumn(i).setPreferredWidth(40);
                columnModel.getColumn(i).setMaxWidth(40);
                m_preferredDialogWidth+=60;
            } else if( i ==0) {
                columnModel.getColumn(i).setPreferredWidth(80);
                columnModel.getColumn(i).setMaxWidth(100);
                m_preferredDialogWidth+=120;
            } else {
                int colWidth = tableModel.getCoeffColumWidth();
                columnModel.getColumn(i).setPreferredWidth(colWidth);
                columnModel.getColumn(i).setMaxWidth(colWidth+20);
                columnModel.getColumn(i).setCellEditor(new FloatTableCellEditor(2));
                m_preferredDialogWidth+=colWidth+30;
            }
        }

        return table;
    }


    @Override
    protected boolean okCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled(){
        //Clear Matrix
        ((AbstractCorrectionMatrixTaleModel) m_purityCorrectionTable.getModel()).clearMatrix();
        repaint();
        return false;
    }

    protected String getPurityMatrix(){
        return ((AbstractCorrectionMatrixTaleModel) m_purityCorrectionTable.getModel()).getPurityMatrixAsString();
    }

    private class PurityCorrectionMatrixTable extends DecoratedTable {

        public PurityCorrectionMatrixTable() {
            super();
            setSortable(false);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        public void addTableModelListener(TableModelListener l) {
            getModel().addTableModelListener(l);
        }

        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        @Override
        public void prepostPopupMenu() {

        }
    }


}
