package fr.proline.studio.rsmexplorer.gui.calc;



import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import javax.swing.JPanel;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graph.DataGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;


/**
 *
 * @author JM235353
 */
public class DataMixerPanel extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;

    
    private GraphPanel m_graphPanel;

    
    public DataMixerPanel() {
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

        m_graphPanel = new GraphPanel();

        // create tree objects
        
        JPanel treePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.NORTHWEST;
        c2.fill = GridBagConstraints.BOTH;
        c2.insets = new java.awt.Insets(5, 5, 5, 5);
        
        

        
        
        JScrollPane treeScrollPane = new JScrollPane();
        final DataMixerTree dataMixerTree = new DataMixerTree();
        treeScrollPane.setViewportView(dataMixerTree);
        treeScrollPane.setMinimumSize(new Dimension(180,50));
        
        JButton refreshButton = new JButton("Refresh Data", IconManager.getIcon(IconManager.IconType.REFRESH));
        refreshButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dataMixerTree.updataDataNodes();
            }
        });
        
        c2.gridx = 0;
        c2.gridy = 0;
        treePanel.add(refreshButton, c2);
        c2.gridx++;
        c.weightx = 1;
        treePanel.add(Box.createHorizontalGlue(), c2);
        
        c2.gridx = 0;
        c2.gridy++;
        c2.gridwidth = 2;
        c2.weightx = 1;
        c2.weighty = 1;
        treePanel.add(treeScrollPane, c2);
        
        // create graph objects
        JScrollPane graphScrollPane = new JScrollPane();
        graphScrollPane.setBackground(Color.white);
        graphScrollPane.setViewportView(m_graphPanel);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, graphScrollPane);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        internalPanel.add(splitPane, c);


        return internalPanel;

    }

    
    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        //JPM.TODO

        return toolbar;
    }

    public void addTableInfoToGraph(TableInfo tableInfo) {
        DataGraphNode graphNode = new DataGraphNode(tableInfo, m_graphPanel);
        m_graphPanel.addGraphNode(graphNode);
    }
    
    public void addFunctionToGraph(AbstractFunction function) {
        FunctionGraphNode graphNode = new FunctionGraphNode(function, m_graphPanel);
        m_graphPanel.addGraphNode(graphNode);
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
    public void setLoading(int id) {}

    @Override
    public void setLoading(int id, boolean calculating) {}

    @Override
    public void setLoaded(int id) {}

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


    

    
    public class DataMixerTree extends DataTree {

        public DataMixerTree() {
            super(new RootDataMixerNode(), false, m_graphPanel);
        }

        @Override
        public void action(DataTree.DataNode node) {
            switch (node.getType()) {

                case VIEW_DATA: {
                    TableInfo tableInfo = ((ViewDataNode) node).getTableInfo();
                    addTableInfoToGraph(tableInfo);
                    break;
                }
                case FUNCTION: {
                    AbstractFunction function = ((FunctionNode) node).getFunction().cloneFunction(m_graphPanel);
                    addFunctionToGraph(function);
                    break;
                }

            }
        }

    }
    

    
}

