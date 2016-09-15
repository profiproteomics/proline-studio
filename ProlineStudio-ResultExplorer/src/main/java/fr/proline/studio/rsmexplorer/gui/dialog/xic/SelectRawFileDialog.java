package fr.proline.studio.rsmexplorer.gui.dialog.xic;



import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.*;
import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;


/**
 * Dialog to select a Raw File in a list
 * @author JM235353
 */
public class SelectRawFileDialog extends DefaultDialog {

    
    
    private JLabel m_rbFileFromDatabaseLabel = null;
    private JList m_jlist;

    
    private static SelectRawFileDialog m_singleton = null;

    private SelectRawFileDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Select a Raw File");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:xic");

        
        setInternalComponent(createInternalPanel());

        setButtonVisible(BUTTON_DEFAULT, false);
        setButtonVisible(BUTTON_HELP, false);

    }

    public static SelectRawFileDialog getSelectRawFileDialog(Window parent) {
        if (m_singleton == null) {
            m_singleton = new SelectRawFileDialog(parent);
        }
        return m_singleton;
    }
    
    public void init(ArrayList<RawFile> rawFileList, RunInfoData.RawFileSource rawFileSource) {

        RawFile linkedRawFile = rawFileSource.getLinkedRawFile();
        if (linkedRawFile != null) {
            addRawFileInList(rawFileList, linkedRawFile);
            return;
        }
        
        RawFile selectedRawFile = rawFileSource.getSelectedRawFile();
        if (selectedRawFile != null) {
            addRawFileInList(rawFileList, selectedRawFile);
            return;
        }
        
        File rawFileOnDisk = rawFileSource.getRawFileOnDisk();
        if (rawFileOnDisk != null) {
            addRawFileInList(rawFileList, null);
            return;
        }
        
        addRawFileInList(rawFileList, null);


        

    }
    
    private boolean addRawFileInList(ArrayList<RawFile> rawFileList, RawFile rawFileToAdd) {
        
        DefaultListModel model = (DefaultListModel) m_jlist.getModel();
        model.clear();
        
        boolean rawFileInList = false;
        if (rawFileList!=null && !rawFileList.isEmpty()) {
        
            Iterator<RawFile> it = rawFileList.iterator();
            while (it.hasNext()) {
                RawFile r = it.next();
                model.addElement(r);
                if ((rawFileToAdd!=null) && r.equals(rawFileToAdd)) {
                   rawFileInList = true; 
                }
            }
        }
        
        if ((!rawFileInList) && (rawFileToAdd!=null)) {
            model.insertElementAt(rawFileToAdd, 0);
            rawFileInList = true;
        }
        
        if (rawFileInList) {
            m_jlist.setSelectedValue(rawFileToAdd, true);
        }
        
        return rawFileInList;
    }
    
    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        m_rbFileFromDatabaseLabel = new JLabel("Registered Raw File In Database");

        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(m_rbFileFromDatabaseLabel, c);
        
        
        m_jlist = new JList();
        DefaultListModel listmodel = new DefaultListModel();
        m_jlist.setModel(listmodel);
        m_jlist.setCellRenderer(new RawFileListCellRenderer());
        m_jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane fileListScrollPane = new JScrollPane(m_jlist) {

            private Dimension preferredSize = new Dimension(360, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };

        c.gridy++;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(fileListScrollPane, c);

        return internalPanel;
    }




    
    @Override
    protected boolean okCalled() {

        Object rawFileObject = m_jlist.getSelectedValue();
        if (rawFileObject == null) {
            setStatus(true, "You must select a Raw File");
            highlight(m_jlist);
            return false;
        }


        return true;

    }

    public RawFile getSelectedRawFile() {
        return (RawFile) m_jlist.getSelectedValue();

    }

    
    @Override
    protected boolean cancelCalled() {
        return true;
    }

    private static class RawFileListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            l.setText(((RawFile) value).getRawFileName());
            return l;
        }
    }
    
}
