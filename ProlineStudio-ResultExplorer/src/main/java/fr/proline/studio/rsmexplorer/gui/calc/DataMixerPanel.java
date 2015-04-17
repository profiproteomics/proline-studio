package fr.proline.studio.rsmexplorer.gui.calc;



import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import javax.swing.JPanel;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graph.DataGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;


/**
 *
 * @author JM235353
 */
public class DataMixerPanel extends JPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
    private JScrollPane m_dataScrollPane;
    
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


        // create tree objects
        JScrollPane treeScrollPane = new JScrollPane();
        DataMixerTree dataMixerTree = new DataMixerTree();
        treeScrollPane.setViewportView(dataMixerTree);
        
        
        // create graph objects
        JScrollPane graphScrollPane = new JScrollPane();
        graphScrollPane.setBackground(Color.white);
        m_graphPanel = new GraphPanel();
        graphScrollPane.setViewportView(m_graphPanel);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, graphScrollPane);

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
        DataGraphNode graphNode = new DataGraphNode(tableInfo);
        m_graphPanel.addGraphNode(graphNode);
    }
    
    public void addFunctionToGraph(AbstractFunction function) {
        FunctionGraphNode graphNode = new FunctionGraphNode(function);
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
            super(new RootDataMixerNode(), false);
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
                    AbstractFunction function = ((FunctionNode) node).getFunction();
                    addFunctionToGraph(function);
                    break;
                }

            }
        }

    }
    

    
}

