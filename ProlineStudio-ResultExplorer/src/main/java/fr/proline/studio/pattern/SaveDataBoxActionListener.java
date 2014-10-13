package fr.proline.studio.pattern;

import fr.proline.studio.gui.SplittedPanelContainer;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.openide.util.NbPreferences;


/**
 * Action called to save a window
 * @author JM235353
 */
public class SaveDataBoxActionListener  implements ActionListener {

    private SplittedPanelContainer m_splittedPanel;
    
    public SaveDataBoxActionListener(SplittedPanelContainer splittedPanel) {
        m_splittedPanel = splittedPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String dump = saveSplittedPanelContainer(m_splittedPanel);
        
        
        Preferences preferences = NbPreferences.root();
        preferences.put("TESTWND", dump);
        
    }
    
    public static String saveParentContainer(Container c) {
        while ((c != null) && !(c instanceof SplittedPanelContainer)) {
            c = c.getParent();
        }
        if (c == null) {
            return null;
        }
        return saveSplittedPanelContainer((SplittedPanelContainer) c);
    }
    public static String saveSplittedPanelContainer(SplittedPanelContainer splittedPanel) {
        ArrayList<JPanel> panelList = new ArrayList<>();
        ArrayList<SplittedPanelContainer.PanelLayout> layoutList = new ArrayList<>();
        splittedPanel.generateListOfPanels(panelList, layoutList);

        ArrayList<AbstractDataBox> boxList = new ArrayList<>();

        for (int i = 0; i < panelList.size(); i++) {
            JPanel p = panelList.get(i);
            if (p instanceof DataBoxPanelInterface) {
                DataBoxPanelInterface databoxPanelInterface = (DataBoxPanelInterface) p;
                AbstractDataBox b = databoxPanelInterface.getDataBox();
                boxList.add(b);
            }

        }
        
        String dump = WindowBoxFactory.writeBoxes(boxList, layoutList);
        
        return dump;
    }
}
