/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.node;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author JM235353
 */
public class RSMDataSetNode extends RSMNode {

    private static ImageIcon vial = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/identification.png", false);
    private static ImageIcon gel = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/identificationFraction.png", false);
    private static ImageIcon rsmIcon = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/resultSummary.png", false);
    private static ImageIcon rsetIcon = ImageUtilities.loadImageIcon("fr/proline/studio/rsmexplorer/images/resultSet.png", false);

    
    public RSMDataSetNode(AbstractData data) {
        super(NodeTypes.DATA_SET, data);
    }

    @Override
    public ImageIcon getIcon() {

        DataSetTMP dataSet = ((DataSetData) getData()).getDataSet();
        
        if (dataSet.getResultSummaryId() != null) {
            return rsmIcon;
        } else if (dataSet.getResultSetId() != null) {
            return rsetIcon;
        }

        //JPM.TODO : vial icon vs gel icon
        
        return gel;

    }
    
    public boolean hasResultSummary() {
        DataSetTMP dataSet = ((DataSetData) getData()).getDataSet();
        return (dataSet.getResultSummaryId() != null);
    }
    

    public Integer getResultSummaryId() {
        return ((DataSetData) getData()).getDataSet().getResultSummaryId();
    }
    
    public ResultSummary getResultSummary() {
        return null; //JPM.TODO
    }
    
    public Integer getResultSetId() {
        return ((DataSetData) getData()).getDataSet().getResultSetId();
    }
    
    /*@Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canRename() {
        return true;
    }*/
    
    /*@Override
    public RSMNode cloneThis() {
        RSMIdentificationNode clonedNode = new RSMIdentificationNode((AbstractData) getUserObject());
        
        addClonedChildren(clonedNode);
        
        return clonedNode;
    }*/
    
}
