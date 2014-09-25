package fr.proline.studio.rsmexplorer.gui.dialog.xic;



import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.FileParameter;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import org.slf4j.LoggerFactory;


/**
 * Dialog to select a Raw File in a list
 * @author JM235353
 */
public class SelectRawFileDialog extends DefaultDialog {

    
    
    private JRadioButton m_rbFileFromDatabaseRb = null;
    private JRadioButton m_rbFileFromServerRb = null;
    
    private JTextField m_textField = null;
    private JButton m_addFileButton = null;
    private JList m_jlist;
    
    private File m_selectedRawFile = null;
    private JFileChooser m_fileChooser = null;
    
    private static SelectRawFileDialog m_singleton = null;

    private SelectRawFileDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Select a Raw File");

        //setHelpURL(null); TODO

        
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
            m_rbFileFromDatabaseRb.setSelected(true);
            enableFileSelection(false);
            m_textField.setText("");
            m_selectedRawFile = null;
            return;
        }
        
        RawFile selectedRawFile = rawFileSource.getSelectedRawFile();
        if (selectedRawFile != null) {
            addRawFileInList(rawFileList, selectedRawFile);
            m_rbFileFromDatabaseRb.setSelected(true);
            enableFileSelection(false);
            m_textField.setText("");
            m_selectedRawFile = null;
            return;
        }
        
        File rawFileOnDisk = rawFileSource.getRawFileOnDisk();
        if (rawFileOnDisk != null) {
            addRawFileInList(rawFileList, null);
            m_rbFileFromServerRb.setSelected(true);
            enableFileSelection(true);
            m_textField.setText(rawFileOnDisk.getPath());
            m_selectedRawFile = rawFileOnDisk;
            return;
        }
        
        addRawFileInList(rawFileList, null);
        
        m_selectedRawFile = null;
        m_textField.setText("");
        if (rawFileList.isEmpty()) {
            m_rbFileFromServerRb.setSelected(true); 
            enableFileSelection(true);
        } else {
            m_rbFileFromDatabaseRb.setSelected(true);
            enableFileSelection(false);
        }
        

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

        
        m_rbFileFromServerRb = new JRadioButton("Raw File from Server");
        m_rbFileFromDatabaseRb = new JRadioButton("Registered Raw File In Database");
        ButtonGroup group = new ButtonGroup();
        group.add(m_rbFileFromServerRb);
        group.add(m_rbFileFromDatabaseRb);
        
        m_rbFileFromServerRb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                enableFileSelection(true);
            }
            
        });
        m_rbFileFromDatabaseRb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                enableFileSelection(false);
            }
            
        });
        
        JPanel filePanel = getFilePanel();
   
        c.gridx = 0;
        c.gridy = 0;
        internalPanel.add(m_rbFileFromServerRb, c);
        
        c.gridy++;
        c.weightx = 1.0;
        internalPanel.add(filePanel, c);
        
        c.gridy++;
        c.weightx = 0;
        internalPanel.add(m_rbFileFromDatabaseRb, c);
        
        
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
    private void enableFileSelection(boolean enable) {
        m_textField.setEnabled(enable);
        m_addFileButton.setEnabled(enable);
        m_jlist.setEnabled(!enable);
    }

    private JPanel getFilePanel() {

        JPanel panel = new JPanel(new FlowLayout());

        m_textField = new JTextField(30);
        m_textField.setEditable(false);

        m_addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        m_addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (m_fileChooser == null) {
                    m_fileChooser = createFileChooser();
                }

                int result = m_fileChooser.showOpenDialog(m_addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {

                    File file = m_fileChooser.getSelectedFile();
                    m_textField.setText(file.getPath());
                    m_selectedRawFile = file;
                }
            }
        });


        panel.add(m_textField);
        panel.add(m_addFileButton);


        return panel;


    }

    private JFileChooser createFileChooser() {
        
        ServerFile defaultDirectory = null;
        // -- get mzDB root path
        ArrayList<String> roots = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);
        if ((roots == null) || (roots.isEmpty())) {
            // check that the server has sent me at least one root path
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Server has returned no Root Path for mzDB Files. There is a problem with the server installation, please contact your administrator.");
        } else {

            String filePath = roots.get(0);
            if (filePath != null) {
                ServerFile f = new ServerFile(filePath, filePath, true, 0, 0);
                if (f.isDirectory()) {
                    defaultDirectory = f;
                }
            }
        }

        JFileChooser fileChooser = (defaultDirectory!= null) ? new JFileChooser(ServerFileSystemView.getServerFileSystemView()) : new JFileChooser(defaultDirectory, ServerFileSystemView.getServerFileSystemView());
        fileChooser.setMultiSelectionEnabled(false);
        return fileChooser;

    }
    
    @Override
    protected boolean okCalled() {

        if (m_rbFileFromDatabaseRb.isSelected()) {
            Object rawFileObject = m_jlist.getSelectedValue();
            if (rawFileObject == null) {
                setStatus(true, "You must select a Raw File");
                highlight(m_jlist);
                return false;
            }
        } else if (m_rbFileFromServerRb.isSelected()) {
            if (m_selectedRawFile == null) {
                setStatus(true, "You must select a Raw File from the Server");
                highlight(m_textField);
                return false;
            }
        }
        
        return true;

    }

    public RawFile getSelectedRawFile() {
        if (m_rbFileFromDatabaseRb.isSelected()) {
            return (RawFile) m_jlist.getSelectedValue();
        } else {
            return null;
        }
    }
    
    public File getSelectedDiskFile() {
        if (m_rbFileFromServerRb.isSelected()) {
            return m_selectedRawFile;
        } else {
            return null;
        }
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
