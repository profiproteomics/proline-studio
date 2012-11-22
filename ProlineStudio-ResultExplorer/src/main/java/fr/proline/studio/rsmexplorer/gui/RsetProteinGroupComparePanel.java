package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class RsetProteinGroupComparePanel extends JPanel implements DataBoxPanelInterface {
    
    private AbstractDataBox dataBox;
    
    private ProteinSetComparePanel proteinSetComparePanel;
    
      /**
     * Creates new form RsetProteinGroupComparePanel
     */
    public RsetProteinGroupComparePanel() {

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        proteinSetComparePanel = new ProteinSetComparePanel();
        add(proteinSetComparePanel, c);

        c.gridy++;
        c.weighty = 0;
        add(new JPanel(), c); // TODO
        
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        this.dataBox = dataBox;
    }
    
    public void setData(ProteinMatch proteinMatch) {
        proteinSetComparePanel.setData(proteinMatch);
    }
    
}
