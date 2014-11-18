package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.comparedata.DiffDataModel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.JoinDataModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 *
 * @author JM235353
 */
public class SelectComparePanel extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    private CompareDataInterface m_result = null;
    private CompareDataInterface m_compareDataInterface1;
    private CompareDataInterface m_compareDataInterface2;
    
    private JScrollPane m_dataScrollPane;
    private JLabel m_label1;
    private JLabel m_label2;
    private JComboBox m_algorithmCombobox;
    
    private final static String[] ALGOS = { "Difference", "Join" };
    private final static int ALGO_DIFF = 0;
    private final static int ALGO_JOIN = 1;
    
    public SelectComparePanel() {
        setLayout(new BorderLayout());
        setBounds(0, 0, 500, 400);
        setBackground(Color.white);


        JPanel internalPanel = initComponents();
        add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        add(toolbar, BorderLayout.WEST);
    }
    
    
    private JPanel initComponents() {


        JPanel internalPanel = new JPanel();
        internalPanel.setBackground(Color.white);

        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);


        // create objects
        m_dataScrollPane = new JScrollPane();

        JPanel graphPanel = createGraphPanel();

        m_dataScrollPane.setViewportView(graphPanel);
        m_dataScrollPane.setBackground(Color.white);


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(m_dataScrollPane, c);


        return internalPanel;

    }
    
    private JPanel createGraphPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.white);
        
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        JLabel dataLabel1 = new JLabel("Data 1 : ");
        JLabel dataLabel2 = new JLabel("Data 2 : ");
        
        m_label1 = new JLabel();
        m_label2 = new JLabel();
        
        m_algorithmCombobox = new JComboBox(ALGOS);
        m_algorithmCombobox.setSelectedIndex(0);
        m_algorithmCombobox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                applyAlgorithm();
            }
            
        });
        
        c.gridx = 0;
        c.gridy = 0;
        p.add(dataLabel1, c);
        
        c.gridx++;
        p.add(m_label1, c);
        
        c.gridx = 0;
        c.gridy++;
        p.add(dataLabel2, c);
        
        c.gridx++;
        p.add(m_label2, c);
        
        c.gridx = 1;
        c.gridy++;
        p.add(m_algorithmCombobox, c);
        
        c.gridy++;
        c.gridx++;
        c.weightx = 1;
        c.weighty = 1;
        p.add(Box.createGlue(), c);
        
        return p;

    }
    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        //JPM.TODO

        return toolbar;
    }
    
    public void applyAlgorithm() {
        int algo = m_algorithmCombobox.getSelectedIndex();
        if (algo == ALGO_DIFF) {
            DiffDataModel diffModel = new DiffDataModel();
            diffModel.setData(m_compareDataInterface1, m_compareDataInterface2);

            m_result = diffModel;
        } else {  // ALGO_JOIN
            JoinDataModel joinDataModel = new JoinDataModel();
            joinDataModel.setData(m_compareDataInterface1, m_compareDataInterface2);
            
            m_result = joinDataModel;
        }
        
        m_dataBox.propagateDataChanged(CompareDataInterface.class);
    }
    
    public void setData(CompareDataInterface compareDataInterface1, CompareDataInterface compareDataInterface2) {
        if (compareDataInterface2 == null) {
            m_label1.setText(compareDataInterface1.getName());
            return;
        }

        m_compareDataInterface1 = compareDataInterface1;
        m_compareDataInterface2 = compareDataInterface2;

        m_label1.setText(compareDataInterface1.getName());
        m_label2.setText(compareDataInterface2.getName());

        applyAlgorithm();
    }
           
    public CompareDataInterface getResultDataInterface() {
        return m_result;
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

    @Override
    public void setLoading(int id) {}

    @Override
    public void setLoading(int id, boolean calculating) {}

    @Override
    public void setLoaded(int id) {}

    
}
