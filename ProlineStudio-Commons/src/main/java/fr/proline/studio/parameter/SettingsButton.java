package fr.proline.studio.parameter;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class SettingsButton extends JButton implements ActionListener {

    private ProgressInterface m_progressInterface = null;
    private SettingsInterface m_settingsInterface = null;
            
    public SettingsButton(ProgressInterface progressInterface, SettingsInterface settingsInterface) {

        m_settingsInterface = settingsInterface;
        m_progressInterface = progressInterface;

        setIcon(IconManager.getIcon(IconManager.IconType.SETTINGS));
        setToolTipText("Settings...");
        setFocusPainted(false);

        addActionListener(this);
    }

        
    public final void setProgressInterface(ProgressInterface progressInterface) {
        m_progressInterface = progressInterface;
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {

        
        if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Settings dialog is not available while data is loading. Please Wait.");
            dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return;
            }
        }
        
        ArrayList<ParameterList> parameterListArray = m_settingsInterface.getParameters();
        if (parameterListArray == null) {
            return;
        }
        DefaultParameterDialog parameterDialog = new DefaultParameterDialog(WindowManager.getDefault().getMainWindow(), "Settings", parameterListArray);
        parameterDialog.setLocationRelativeTo(this);
        parameterDialog.setVisible(true);

        if (parameterDialog.getButtonClicked() == DefaultParameterDialog.BUTTON_OK) {
            m_settingsInterface.parametersChanged();
        }

    }
}