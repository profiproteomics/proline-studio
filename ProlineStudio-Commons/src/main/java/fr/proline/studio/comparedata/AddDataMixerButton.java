package fr.proline.studio.comparedata;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.openide.windows.WindowManager;

/**
 * Button to send data to the data mixer
 * @author JM235353
 */
public abstract class AddDataMixerButton  extends JButton implements ActionListener {

    private ProgressInterface m_progressInterface = null;
    
    public AddDataMixerButton(ProgressInterface progressInterface) {

        m_progressInterface = progressInterface;

        setIcon(IconManager.getIcon(IconManager.IconType.ADD_DATA_MIXER));
        setToolTipText("Add Data to Data Mixer...");

        addActionListener(this);
    }
    
        @Override
    public void actionPerformed(ActionEvent e) {

        
        if ((m_progressInterface != null) && (!m_progressInterface.isLoaded())) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Data are not available, loading is not finished. Please Wait.");
            dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return;
            }
        }

        actionPerformed();

    }
    
    public abstract void actionPerformed();
    
}
