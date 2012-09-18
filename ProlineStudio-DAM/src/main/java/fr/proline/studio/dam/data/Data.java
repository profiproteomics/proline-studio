/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;

import java.util.List;

/**
 *
 * @author JM235353
 */
public abstract class Data {
    
    public enum DataTypes {
        MAIN,
        PROJECT,
        CONTEXT,
        RESULT_SET,
        RESULT_SUMMARY,
    }
    
    protected String name;
    protected DataTypes dataType;
    
    public Data() {

        //name = String.valueOf(m_cnt++); //JPM.TODO
        
        
    }
    //private static int m_cnt = 0;
    
    public void load(List<Data> list) {
        
    }
    
    public String getName() {
        return "";
    }
    public void setName(String name) {
    //    this.name = name;  //JPM.TODO
    }
    
    public DataTypes getDataType() {
        return dataType;
    }

}
