/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam;

/**
 *
 * @author JM235353
 */
public abstract class ContainerData {
    
    public enum DataTypes {
        RESULT_SET,
        RESULT_SUMMARY,
        CONTEXT
    }
    
    protected String name;
    protected DataTypes dataType;
    
    public ContainerData() {

        name = String.valueOf(m_cnt++); //JPM.TODO
        
        
    }
    private static int m_cnt = 0;
    
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    

    
    public DataTypes getDataType() {
        return dataType;
    }

}
