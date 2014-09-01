package fr.proline.studio.rsmexplorer.gui.spectrum;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import fr.proline.studio.rsmexplorer.gui.spectrum.RsetPeptideFragmentationTable;
import fr.proline.studio.utils.IconManager;
import javax.swing.JButton;

/**
 * Button to toggle display of intensity/masses from the fragmentation table
 *
 * @author AW
 */
public class HideFragmentsTableIntensityButton extends JButton implements ActionListener {

    private static final long serialVersionUID = 1L;
    private RsetPeptideFragmentationTable m_table = null;
    private boolean m_isPressed = false;

    public HideFragmentsTableIntensityButton(RsetPeptideFragmentationTable table, boolean visible) {

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
        m_table.updateFragmentsIntensityVisibility(m_isPressed);
    }

}
