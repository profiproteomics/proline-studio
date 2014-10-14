package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Dialog used to manage saved windows created by the user
 * @author JM235353
 */
public class ManageSaveWindowsDialog extends DefaultDialog {
    
    private static ManageSaveWindowsDialog m_singletonDialog = null;
    
    private JList<String> m_windowList;
    private JButton m_removeWndButton;
    
    public static ManageSaveWindowsDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ManageSaveWindowsDialog(parent);
        }

        m_singletonDialog.init();

        return m_singletonDialog;
    }
    
    private ManageSaveWindowsDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        
        setTitle("Manage User Windows");
       
        setHelpURL(null); //JPM.TODO
        
        setResizable(true);
        setMinimumSize(new Dimension(200, 240));

        JPanel p = createWindowsPanel();
        setInternalComponent(p);
    }
    
    private JPanel createWindowsPanel() {

        // Creation of Objects for File Selection Panel
        JPanel windowsPanel = new JPanel(new GridBagLayout());
        windowsPanel.setBorder(BorderFactory.createTitledBorder(" User Windows "));

        m_windowList = new JList<>(new DefaultListModel());
        m_windowList.setCellRenderer(new WindowListRenderer());
        JScrollPane windowListScrollPane = new JScrollPane(m_windowList) {

            private Dimension preferredSize = new Dimension(360, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };


        m_removeWndButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        m_removeWndButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        // Placement of Objects for File Selection Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        windowsPanel.add(windowListScrollPane, c);


        c.gridx++;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        windowsPanel.add(m_removeWndButton, c);



        c.gridy++;
        windowsPanel.add(Box.createVerticalStrut(30), c);


        // Actions on objects
        m_windowList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean sometingSelected = (m_windowList.getSelectedIndex() != -1);
                m_removeWndButton.setEnabled(sometingSelected);
            }
        });



        m_removeWndButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> selectedValues = m_windowList.getSelectedValuesList();
                Iterator<String> it = selectedValues.iterator();
                while (it.hasNext()) {
                    ((DefaultListModel) m_windowList.getModel()).removeElement(it.next());
                }
                m_removeWndButton.setEnabled(false);
            }
        });

        return windowsPanel;

    }
    
    private void init() {
        ArrayList<String> m_windowSavedList = WindowSavedManager.readSavedWindows();
        DefaultListModel listModel = ((DefaultListModel) m_windowList.getModel());
        listModel.clear();

        int nb = m_windowSavedList.size();
        for (int i=0;i<nb;i++) {
            listModel.addElement(m_windowSavedList.get(i));
        }
        
        m_removeWndButton.setEnabled(false);
    }

    @Override
    protected boolean okCalled() {
        DefaultListModel listModel = ((DefaultListModel) m_windowList.getModel());
        
        int size = listModel.getSize();
        ArrayList<String> savedWindowsList = new ArrayList<>(size);
        for (int i=0;i<size;i++) {
            savedWindowsList.add((String) listModel.get(i));
        }

        WindowSavedManager.setSavedWindows(savedWindowsList);
        
        return true;

    }
    
    private static class WindowListRenderer extends DefaultListCellRenderer {

        public WindowListRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, WindowSavedManager.getWindowName((String) value), index, isSelected, cellHasFocus);
        }
    }
}
