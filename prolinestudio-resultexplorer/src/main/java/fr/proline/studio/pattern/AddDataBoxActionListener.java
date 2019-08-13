package fr.proline.studio.pattern;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.dialog.DataBoxChooserDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.windows.WindowManager;

/**
 * Action to add a new Databox at the end of the queue
 * @author JM235353
 */
public class AddDataBoxActionListener implements ActionListener {

    private final SplittedPanelContainer m_splittedPanel;
    private final AbstractDataBox m_previousDatabox;

    public AddDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox previousDatabox) {
        m_splittedPanel = splittedPanel;
        m_previousDatabox = previousDatabox;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        DataBoxChooserDialog dialog = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), m_previousDatabox, false, null);
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            
            AbstractDataBox genericDatabox = dialog.getSelectedDataBox();
            try {
                AbstractDataBox newGenericDatabox = (AbstractDataBox) genericDatabox.getClass().newInstance(); // copy the databox
                
                //Some databox must be specifically configured ...  
                // FIXME VDS : To be more generic ?!
                if(DataboxGraphics.class.isInstance(newGenericDatabox)) {
                    ((DataboxGraphics)newGenericDatabox).setDefaultLocked(((DataboxGraphics)genericDatabox).isDefaultLocked());
                } else if (DataboxMultiGraphics.class.isInstance(newGenericDatabox) ){                    
                    newGenericDatabox = new DataboxMultiGraphics(false, false, ((DataboxMultiGraphics)genericDatabox).isDoubleYAxis());                    
                } else if (DataBoxPTMPeptides.class.equals(newGenericDatabox.getClass())){                    
                    newGenericDatabox = new DataBoxPTMPeptides(((DataBoxPTMPeptides)genericDatabox).isQuantiResult(), ((DataBoxPTMPeptides)genericDatabox).isAllPSMsDisplayed());
                }
                genericDatabox = newGenericDatabox;
            } catch (InstantiationException | IllegalAccessException e) {
                // should never happen
            }

            m_previousDatabox.addNextDataBox(genericDatabox);


            genericDatabox.createPanel();

            // add the new panel to the window (below, as a tabb, or as splitted
            if (dialog.addBelow()) {
                m_splittedPanel.registerAddedPanel((JPanel) genericDatabox.getPanel());
            } else if (dialog.addTabbed()) {
                m_splittedPanel.registerAddedPanelAsTab((JPanel) genericDatabox.getPanel());
            } else {
                // splitted
                m_splittedPanel.registerAddedPanelAsSplitted((JPanel) genericDatabox.getPanel());
            }

            // update display of added databox
            final AbstractDataBox _genericDatabox = genericDatabox;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    _genericDatabox.dataChanged();
                }
            });

        }

    }
}