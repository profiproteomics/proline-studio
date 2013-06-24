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

    SplittedPanelContainer m_splittedPanel;
    AbstractDataBox m_previousDatabox;

    public AddDataBoxActionListener(SplittedPanelContainer splittedPanel, AbstractDataBox previousDatabox) {
        m_splittedPanel = splittedPanel;
        m_previousDatabox = previousDatabox;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        DataBoxChooserDialog dialog = new DataBoxChooserDialog(WindowManager.getDefault().getMainWindow(), m_previousDatabox);
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            AbstractDataBox genericDatabox = dialog.getSelectedDataBox();
            try {
                genericDatabox = (AbstractDataBox) genericDatabox.getClass().newInstance(); // copy the databox

            } catch (InstantiationException | IllegalAccessException e) {
                // should never happen
            }

            m_previousDatabox.setNextDataBox(genericDatabox);


            RemoveDataBoxActionListener removeAction = new RemoveDataBoxActionListener(m_splittedPanel, m_previousDatabox);
            AddDataBoxActionListener addAction = new AddDataBoxActionListener(m_splittedPanel, genericDatabox);
            /*SplittedPanelContainer.UserDefinedButton removeBoxButton = new SplittedPanelContainer.UserDefinedButton(IconManager.getIcon(IconManager.IconType.MINUS11), removeAction);
            SplittedPanelContainer.UserDefinedButton addBoxButton = new SplittedPanelContainer.UserDefinedButton(IconManager.getIcon(IconManager.IconType.PLUS11), addAction);
            ArrayList<SplittedPanelContainer.UserDefinedButton> userButtonList = new ArrayList<>();
            userButtonList.add(addBoxButton);
            userButtonList.add(removeBoxButton);*/

            genericDatabox.createPanel();



            m_splittedPanel.registerAddedPanel((JPanel) genericDatabox.getPanel());

            // update display of added databox
            final AbstractDataBox _genericDatabox = genericDatabox;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    _genericDatabox.dataChanged(null);
                }
            });

        }

    }
}