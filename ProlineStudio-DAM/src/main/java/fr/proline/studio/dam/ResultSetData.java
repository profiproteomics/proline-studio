/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam;


import fr.proline.core.om.model.msi.ResultSet;
import fr.proline.core.utils.generator.ResultSetFakeBuilder;

/**
 *
 * @author JM235353
 */
public class ResultSetData extends ContainerData {
    
    public ResultSet resultSet = null;
    
    public ResultSetData() {
        dataType = DataTypes.RESULT_SET;
        generateTestData();
    }
    
    public void generateTestData() {
        ResultSetFakeBuilder resultSetFakeBuilder = new ResultSetFakeBuilder(10, 2, 0, 8, 20);
        resultSet = resultSetFakeBuilder.toResultSet();
    }
}
