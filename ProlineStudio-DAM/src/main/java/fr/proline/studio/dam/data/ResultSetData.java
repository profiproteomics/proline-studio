/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.data.Data;
import fr.proline.core.utils.generator.ResultSetFakeBuilder;

/**
 *
 * @author JM235353
 */
public class ResultSetData extends Data {
    
    public ResultSet resultSet = null;
    
    public ResultSetData(ResultSet resultSet) {
        dataType = DataTypes.RESULT_SET;
        this.resultSet = resultSet;
    }
    
    public String getName() {
        if (resultSet != null) {
            return resultSet.getName();
        }
        return "";
    }
    
    
    // JPM.TODO : remove it later
    /*public void generateTestData() {
        ResultSetFakeBuilder resultSetFakeBuilder = new ResultSetFakeBuilder(10, 2, 0, 8, 20);
        resultSet = resultSetFakeBuilder.toResultSet();
    }*/
}
