package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxFrozenCopy;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 *
 * @author JM235353
 */
public class DisplayViewInNewWindowAction extends AbstractAction {
    
    private AbstractDataBox m_sourceBox;
    private AbstractDataBox m_destinationBox;


    public DisplayViewInNewWindowAction(AbstractDataBox sourceBox, AbstractDataBox destinationBox) {
        super(destinationBox.getDescription());
        
        Image img = destinationBox.getDefaultIcon();
        if (img != null) {
            putValue(Action.SMALL_ICON, new ImageIcon(img));
        }
        
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


        String dataName = m_sourceBox.getImportantOutParameterValue();
        if ((dataName !=null) && (dataName.length()>12)) {
            dataName = dataName.substring(0,10)+"...";
        }

        final WindowBox wbox = WindowBoxFactory.getDetailWindowBox(dataName, dataName+": "+m_destinationBox.getDescription(), m_destinationBox);

        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();
        
        Class[] classes = m_sourceBox.getImportantInParameterClass();
        for (int i=0;i<classes.length;i++) {
            dataBoxFrozenCopy.propagateDataChanged(classes[i]);
        }



    }

    
}
