package fr.proline.studio.rsmexplorer;

import fr.proline.studio.pattern.GroupParameter;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.table.TableInfo;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author VD225637
 */
public class MultiDataBoxViewerTopComponent extends DataBoxViewerTopComponent {

    private WindowBox[] m_windowBoxes = null;

    public MultiDataBoxViewerTopComponent(WindowBox[] windowBoxes, String name) {
        super(windowBoxes[0]);
        m_windowBoxes = windowBoxes;
        removeAll();
        setLayout(new GridLayout());
        add(createPanel());
        setName(name);
    }

    private JPanel createPanel() {
        JPanel internalPanel = new JPanel(new GridLayout());

          JTabbedPane tabbedPane = new JTabbedPane();
        for (WindowBox wBox : m_windowBoxes) {
            ImageIcon boxIcon = null;
            if(wBox.getIcon() != null)
                boxIcon = new ImageIcon(wBox.getIcon());
            tabbedPane.addTab(wBox.getName(), boxIcon, wBox.getPanel(), null);
        }
        
        internalPanel.add(tabbedPane);

        return internalPanel;
    }

    @Override
    public void retrieveTableModels(ArrayList<TableInfo> list) {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.retrieveTableModels(list);
        }
    }

    @Override
    protected void componentOpened() {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.windowOpened();
        }
    }

    @Override
    protected void componentClosed() {
        for (WindowBox wBox : m_windowBoxes) {
            wBox.windowClosed();
        }
    }

    @Override
    public HashSet<GroupParameter> getInParameters(){
        HashSet<GroupParameter> allWindowsParam = new HashSet();
         for (WindowBox wBox : m_windowBoxes) {
            allWindowsParam.addAll(wBox.getEntryBox().getInParameters());
        }
        return allWindowsParam;
    }
    
    @Override
    public ArrayList<GroupParameter> getOutParameters(){
        ArrayList<GroupParameter> allWindowsParam = new ArrayList();
         for (WindowBox wBox : m_windowBoxes) {
            allWindowsParam.addAll(wBox.getEntryBox().getOutParameters());
        }
        return allWindowsParam;        
    }

}
