package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.pattern.DataboxGeneric;
import fr.proline.studio.rsmexplorer.gui.model.properties.IdentificationPropertiesTableModel;
import java.util.*;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.gui.GenericPanel;
import fr.proline.studio.rsmexplorer.gui.model.properties.AbstractPropertiesTableModel;
import fr.proline.studio.rsmexplorer.gui.model.properties.XICPropertiesTableModel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.utils.IconManager;
import org.openide.util.NbBundle;

/**
 * Display of the properties of a rset and rsm
 *
 * @author JM235353
 */


public class PropertiesAction extends AbstractRSMAction {

    public PropertiesAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(PropertiesAction.class, "CTL_PropertiesAction"), treeType);
    }

    @Override
    public void actionPerformed(final AbstractNode[] selectedNodes, int x, int y) {

        //String dialogName;
        String name = "";
        int nbSelectedNodes = selectedNodes.length;
        if (nbSelectedNodes == 1) {
            AbstractNode firstNode = selectedNodes[0];
            name = firstNode.getData().getName();
        }

        
        boolean identificationProperties = false;
        boolean isIdentificationTree = isIdentificationTree();
        
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            AbstractNode.NodeTypes type = node.getType();

            if ((type == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) || ((type == AbstractNode.NodeTypes.DATA_SET) && (isIdentificationTree)) || (type == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
                identificationProperties = true;
 
            }

        }

        // new Properties window
        AbstractPropertiesTableModel model = null;

        WindowBox windowBox = WindowBoxFactory.getGenericWindowBox(name, "Properties", IconManager.IconType.DOCUMENT_LIST, true);
        model = identificationProperties ? new IdentificationPropertiesTableModel() : new XICPropertiesTableModel();
        windowBox.setEntryData(-1L, model);
        DataBoxViewerTopComponent win2 = new DataBoxViewerTopComponent(windowBox);
        win2.open();
        win2.requestActive();

        //JPM.HACK ! Impossible to set the max number of lines differently in this case
        DataboxGeneric databoxGeneric = ((DataboxGeneric) windowBox.getEntryBox());
        GenericPanel genericPanel = (GenericPanel) databoxGeneric.getPanel();

        final AbstractPropertiesTableModel _model = model;
        final GenericPanel _genericPanel = genericPanel;

        final int loadingId = databoxGeneric.setLoading();
        
        // load data for properties
        final DataLoadedCallback dataLoadedCallback = new DataLoadedCallback(selectedNodes.length) {

            @Override
            public void run() {
                m_nbDataToLoad--;
                if (m_nbDataToLoad == 0) {

                    databoxGeneric.setLoaded(loadingId);
                    
                    ArrayList<DDataset> datasetList = new ArrayList<>();
                    for (AbstractNode node : selectedNodes) {
                        if (node instanceof DataSetNode) {
                            DDataset dataset = ((DataSetNode) node).getDataset();
                            datasetList.add(dataset);
                        }
                    }

                    if (_model != null) {
                        _model.setData(datasetList);
                        _genericPanel.setMaxLineNumber(_model.getRowCount());
                    }

                }
            }

        };

        int nbDataToLoad = selectedNodes.length;
        for (int i = 0; i < nbDataToLoad; i++) {
            selectedNodes[i].loadDataForProperties(dataLoadedCallback);
        }

    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;

        // properties action is enabled only if selected nodes
        // are of the same type and are of type PROJECT or DATA_SET
        AbstractNode.NodeTypes currentType = null;
        boolean identificationProperties = false;
        boolean quantitationProperties = false;
        for (int i = 0; i < nbSelectedNodes; i++) {
            AbstractNode node = selectedNodes[i];

            // node is being created, we can not show properties
            if (node.isChanging()) {
                setEnabled(false);
                return;
            }

            AbstractNode.NodeTypes type = node.getType();
            if ((currentType != null) && (currentType != type)) {
                setEnabled(false);
                return;
            }
            if (((type != AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) || (type != AbstractNode.NodeTypes.PROJECT_QUANTITATION)) && (type != AbstractNode.NodeTypes.DATA_SET) && (type != AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
                setEnabled(false);
                return;
            }

            // one can be the trash
            if (((DataSetNode) node).isTrash() || ((DataSetNode) node).isFolder()) {
                setEnabled(false);
                return;
            }

            if ((type == AbstractNode.NodeTypes.PROJECT_IDENTIFICATION) || (type == AbstractNode.NodeTypes.DATA_SET)) {
                DataSetNode datasetNode = (DataSetNode) node;
                identificationProperties = true;
                if (!datasetNode.hasResultSet() && !datasetNode.hasResultSummary()) {
                    setEnabled(false);
                    return;
                }
            }

            if ((type == AbstractNode.NodeTypes.PROJECT_QUANTITATION) || (type == AbstractNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS)) {
                DataSetNode datasetNode = (DataSetNode) node;
                quantitationProperties = true;
                if (!datasetNode.hasResultSet() && !datasetNode.hasResultSummary()) {
                    setEnabled(false);
                    return;
                }
            }

            currentType = type;
        }
        
        if (identificationProperties && quantitationProperties) {
            // can not display combined property for identification and quantitation
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }

    public abstract class DataLoadedCallback implements Runnable {

        protected int m_nbDataToLoad = 0;

        public DataLoadedCallback(int nb) {
            m_nbDataToLoad = nb;
        }

        @Override
        public abstract void run();

    }

}
