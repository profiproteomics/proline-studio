package fr.proline.studio.export;

import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.WindowManager;

/**
 * Button to export data of a table
 * @author JM235353
 */
public class ExportButton extends JButton implements ActionListener {

    private String m_exportName;
    private JXTable m_table;
    
    private ProgressInterface m_progressInterface;
            
    public ExportButton(ProgressInterface progressInterface, String exportName, JXTable table) {

        m_progressInterface = progressInterface;
        
        m_exportName = exportName;
        m_table = table;
        
        setIcon(IconManager.getIcon(IconManager.IconType.EXPORT));
        setToolTipText("Export...");

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        
        if (!m_progressInterface.isLoaded()) {

            ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_progressInterface, "Data loading", "Export is not available while data is loading. Please Wait.");
            dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
            dialog.setVisible(true);

            if (!dialog.isWaitingFinished()) {
                return;
            }
        }

        ExportDialog dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_table, m_exportName);
        dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
        dialog.setVisible(true);

    }
}
