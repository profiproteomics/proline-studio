package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxFrozenCopy;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.pattern.WindowSavedManager;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.types.XicMode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author JM235353
 */
public class DisplayViewInNewWindowAction extends AbstractAction {
    
    private AbstractDataBox m_sourceBox;
    private AbstractDataBox m_destinationBox;


    public DisplayViewInNewWindowAction(AbstractDataBox sourceBox, AbstractDataBox destinationBox) {
        super(destinationBox.getDescription());
        m_sourceBox = sourceBox;
        try {
            m_destinationBox = destinationBox.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // should never happen
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        DataBoxFrozenCopy dataBoxFrozenCopy = new DataBoxFrozenCopy(m_sourceBox);

        dataBoxFrozenCopy.addNextDataBox(m_destinationBox);

        char windowType = ' ';
        if (m_sourceBox.getData(false, XicMode.class) != null) {
            windowType = WindowSavedManager.SAVE_WINDOW_FOR_QUANTI;
        } else if (m_sourceBox.getData(false, ResultSummary.class) != null) {
            windowType = WindowSavedManager.SAVE_WINDOW_FOR_RSM;
        } else if (m_sourceBox.getData(false, ResultSet.class) != null) {
            windowType = WindowSavedManager.SAVE_WINDOW_FOR_RSET;
        }
        String dataName = m_sourceBox.getImportantOutParameterValue();
        if ((dataName !=null) && (dataName.length()>12)) {
            dataName = dataName.substring(0,10)+"...";
        }
        final WindowBox wbox = WindowBoxFactory.getUserDefinedWindowBox(dataName, dataName+": "+m_destinationBox.getDescription(), m_destinationBox, false, false, windowType);

        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();

        m_destinationBox.dataChanged();


    }

    
}
