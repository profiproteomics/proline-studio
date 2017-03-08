package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Dialog to select a Raw File in a list
 *
 * @author JM235353
 */
public class SelectRawFileDialog extends DefaultDialog {

    private JLabel m_rbFileFromDatabaseLabel = null;
    private JList m_jlist;

    private static SelectRawFileDialog m_singleton = null;

    private SelectRawFileDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Select a Raw File");

        setDocumentationSuffix("id.3cqmetx");

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

    public void init(HashMap<String, RawFile> rawFileMap, RunInfoData runInfoData) {

        RawFile linkedRawFile = runInfoData.getLinkedRawFile();
        if (linkedRawFile != null) {
            addRawFileInList(rawFileMap, linkedRawFile);
            return;
        }

        RawFile selectedRawFile = runInfoData.getSelectedRawFile();
        if (selectedRawFile != null) {
            addRawFileInList(rawFileMap, selectedRawFile);
            return;
        }

        File rawFileOnDisk = runInfoData.getRawFileOnDisk();
        if (rawFileOnDisk != null) {
            addRawFileInList(rawFileMap, null);
            return;
        }

        addRawFileInList(rawFileMap, null);

    }

    private boolean addRawFileInList(HashMap<String, RawFile> rawFileMap, RawFile rawFileToAdd) {

        DefaultListModel model = (DefaultListModel) m_jlist.getModel();
        model.clear();

        boolean rawFileInList = false;
        if (rawFileMap != null && !rawFileMap.isEmpty()) {

            for (Map.Entry<String, RawFile> entry : rawFileMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                model.addElement(value);
                if ((rawFileToAdd != null) && value.equals(rawFileToAdd)) {
                    rawFileInList = true;
                }
            }

        }

        if ((!rawFileInList) && (rawFileToAdd != null)) {
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
