package fr.proline.studio.export;

import fr.proline.studio.utils.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.JXTable;
import org.openide.windows.WindowManager;

/**
 *
 * @author JM235353
 */
public class ExportButton extends JButton implements ActionListener {

    private String m_exportName;
    private JXTable m_table;
    
    public ExportButton(String exportName, JXTable table) {

        m_exportName = exportName;
        m_table = table;
        
        setIcon(IconManager.getIcon(IconManager.IconType.EXPORT));
        setToolTipText("Export...");

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!m_table.isSortable()) {
            // data is not ready
            JOptionPane.showMessageDialog(this, "Export is not possible while data is loading.", "Export Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ExportDialog dialog = ExportDialog.getDialog(WindowManager.getDefault().getMainWindow(), m_table, m_exportName);
        dialog.setLocation(getLocationOnScreen().x + getWidth() + 5, getLocationOnScreen().y + getHeight() + 5);
        dialog.setVisible(true);

    }
}
