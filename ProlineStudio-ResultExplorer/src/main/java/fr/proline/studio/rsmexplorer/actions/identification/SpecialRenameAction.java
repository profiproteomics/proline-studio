package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.ParameterDialog;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Action to Rename a dataset
 *
 * @author JM235353
 */
public class SpecialRenameAction extends AbstractRSMAction {

    private static final String GENERAL_APPLICATION_SETTINGS = "General Application Settings";
    private String m_parameterValue;

    // tree type: could be Identification or Quantitation
    AbstractTree.TreeType m_treeType = null;

    /**
     * Builds the RenameAction depending of the treeType
     *
     * @param treeType
     */
    public SpecialRenameAction(AbstractTree.TreeType treeType) {
        super(NbBundle.getMessage(SpecialRenameAction.class, "CTL_SpecialRenameAction"), treeType);
        this.m_treeType = treeType;
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

            ArrayList<DataSetNode> validNodes = new ArrayList<DataSetNode>();
            AbstractTree tree = IdentificationTree.getCurrentTree();

            String naming = showRenameDialog(x, y);

            for (int i = 0; i < selectedNodes.length; i++) {

                if (selectedNodes[i].getType() == AbstractNode.NodeTypes.DATA_SET) {

                    DataSetNode datasetNode = (DataSetNode) selectedNodes[i];

                    if (datasetNode.getChildCount() > 0) {

                        tree.loadAllAtOnce(datasetNode, true);
                        
                        Enumeration<AbstractNode> e = datasetNode.depthFirstEnumeration();

                        while (e.hasMoreElements()) {
                            AbstractNode currentElement = e.nextElement();
                            if (currentElement.getType() == AbstractNode.NodeTypes.DATA_SET && currentElement.isLeaf() && currentElement.getChildCount() == 0) {
                                validNodes.add((DataSetNode) currentElement);
                            }
                        }
                    } else {
                        if (datasetNode.isLeaf()) {
                            validNodes.add(datasetNode);
                        }
                    }
                }
            }

            for (int i = 0; i < validNodes.size(); i++) {

                DDataset dataset = validNodes.get(i).getDataset();
                DataSetData.fetchRsetAndRsmForOneDataset(dataset);

                if (dataset == null || dataset.getResultSet() == null || dataset.getResultSet().getMsiSearch() == null) {
                    continue;
                }

                String newName = "";

                newName = (dataset.getResultSet().getMsiSearch().getResultFileName() == null) ? "" : dataset.getResultSet().getMsiSearch().getResultFileName();
                if (newName.contains(".")) {
                    newName = newName.substring(0, newName.indexOf("."));
                }

                if (naming.equalsIgnoreCase(ImportManager.SEARCH_RESULT_NAME_SOURCE)) {
                    newName = dataset.getResultSet().getName();
                } else if (naming.equalsIgnoreCase(ImportManager.PEAKLIST_PATH_SOURCE)) {
                    newName = (dataset.getResultSet().getMsiSearch().getPeaklist().getPath() == null) ? "" : dataset.getResultSet().getMsiSearch().getPeaklist().getPath();
                    if (newName.contains(File.separator)) {
                        newName = newName.substring(newName.lastIndexOf(File.separator) + 1);
                    }
                }

                if (!newName.equalsIgnoreCase("")) {

                    validNodes.get(i).rename(newName, tree);

                    dataset.setName(newName);

                    tree.rename(validNodes.get(i), newName);

                }

            }

    }

    private String showRenameDialog(int x, int y) {

        ParameterList parameterList = new ParameterList(GENERAL_APPLICATION_SETTINGS);
        Object[] objectTable = {ImportManager.SEARCH_RESULT_NAME_SOURCE, ImportManager.PEAKLIST_PATH_SOURCE, ImportManager.MSI_SEARCH_FILE_NAME_SOURCE};
        ObjectParameter parameter = new ObjectParameter(ImportManager.DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY, "Default Search Result Name Source", objectTable, 2, null);
        parameterList.add(parameter);
        parameterList.loadParameters(NbPreferences.root(), true);

        ParameterDialog dialog = new ParameterDialog(WindowManager.getDefault().getMainWindow(), "Rename Project Using...", parameter);
        dialog.setLocation(x, y);
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            this.m_parameterValue = (String) dialog.getParameterValue();
        }

        if ((this.m_parameterValue != null) && (this.m_parameterValue.length() > 0)) {
            return this.m_parameterValue;
        }

        return null;
    }

    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (!DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            setEnabled(false);
            return;
        }

        AbstractNode node = selectedNodes[0];
        AbstractNode.NodeTypes nodeType = node.getType();
        if ((nodeType != AbstractNode.NodeTypes.DATA_SET) && (nodeType != AbstractNode.NodeTypes.PROJECT_IDENTIFICATION)) {
            setEnabled(false);
            return;
        }

        setEnabled(true);

    }

}
