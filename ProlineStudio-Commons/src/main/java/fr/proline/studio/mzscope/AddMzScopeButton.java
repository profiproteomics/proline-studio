package fr.proline.studio.mzscope;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.openide.windows.WindowManager;

/**
 * Button to access to mzScope
 * 
 * @author MB243701
 */
public abstract class AddMzScopeButton extends JButton implements ActionListener {

    private MzScopeInterface m_mzScopeInterface = null;
    private ProgressInterface m_progressInterface = null;

    public AddMzScopeButton(ProgressInterface progressInterface, MzScopeInterface mzScopeInterface) {
        m_progressInterface = progressInterface;
        m_mzScopeInterface = mzScopeInterface;
        init();
    }

    private void init() {
        setIcon(IconManager.getIcon(IconManager.IconType.WAVE));
        setToolTipText("Add mzdb file to MzScope...");

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

        actionPerformed(m_mzScopeInterface);

    }

    public abstract void actionPerformed(MzScopeInterface mzScopeInterface);

}
