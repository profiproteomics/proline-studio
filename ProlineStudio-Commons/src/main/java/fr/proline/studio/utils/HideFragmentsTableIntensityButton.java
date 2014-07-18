package fr.proline.studio.utils;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Button to toggle display of intensity/masses from the fragmentation table
 *
 * @author AW
 */
public class HideFragmentsTableIntensityButton extends JButton implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JXTable m_table = null;
    private boolean m_isPressed = false;

    public HideFragmentsTableIntensityButton(JXTable table, boolean visible) {

        m_table = table;
        setIcon(IconManager.getIcon(IconManager.IconType.ADD_REMOVE));
        setToolTipText("Show/hide matching fragments intensity...");
        m_isPressed = visible;
        this.setSelected(m_isPressed);
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        m_isPressed = !m_isPressed;
        this.setSelected(m_isPressed);
        updateFragmentsIntensityVisibility();
    }

    public void updateFragmentsIntensityVisibility() {

        String intensityStringIdentifier = "(I)";

        List<TableColumn> columns;
        // get all columns including hidden ones
        columns = m_table.getColumns(true);

        if (columns != null) {
            if (columns.size() > 0) {
                for (int i = columns.size() - 1; i > 0; i--) { // use all columns but check for an identifier in the title
                    TableColumn currentColumn = columns.get(i);
                    TableColumnExt tce = m_table.getColumnExt(currentColumn.getIdentifier());
                    if (tce.getTitle().contains(intensityStringIdentifier)) {
                        tce.setVisible(m_isPressed);
                    }

                }
            }

        }

    }
}
