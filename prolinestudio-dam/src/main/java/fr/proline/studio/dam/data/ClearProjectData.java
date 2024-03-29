/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dam.data;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import java.sql.Timestamp;

/**
 * data from a project that could be removed
 * resultSet that is not linked to a dataset or resultSummary linked to a rs that is not linked to a dataset
 * To get orphans RS/RSM: 
 *  we list rs linekd to data_set +  we list rsm linked to a master_quant_channel and quant_channel and w elist all rs linked to these rsm
 * => we obtain a list of rs listed into the UDS database
 * The orphans RS are all MSI RS that are not in this list
 * The orphans RSM are all MSI RSM that are not linekd to the RS of this list.
 * @author MB243701
 */
public class ClearProjectData {
    public final static String TYPE_RS = "Search Result";
    public final static String TYPE_RSM = "Identification Summary";
    
    private long m_projectId;
    private ResultSummary m_rsm;
    private ResultSet m_rs;
    
    private boolean m_isSelected;
    private boolean m_isEditable;
    

    public ClearProjectData(long m_projectId, ResultSet m_rs) {
        this.m_projectId = m_projectId;
        this.m_rs = m_rs;
        this.m_rsm = null;
        this.m_isSelected = false;
        this.m_isEditable = true;
    }

    public ClearProjectData(long m_projectId, ResultSummary m_rsm) {
        this.m_projectId = m_projectId;
        this.m_rs = null;
        this.m_rsm = m_rsm;
        this.m_isSelected = true;
        this.m_isEditable = true;
    }
    
    
    public boolean isResultSet(){
        return m_rsm == null;
    }
    
    public boolean isResultSummary(){
        return m_rsm != null;
    }
    
    public ResultSet getResultSet(){
        if (isResultSet()){
            return m_rs;
        }else{
            return m_rsm.getResultSet();
        }
    }
    
    public ResultSummary getResultSummary(){
        return m_rsm;
    }
    
    public long getProjectId(){
        return m_projectId;
    }
    
    public Long getId(){
        if (isResultSummary()){
            return m_rsm.getId();
        }else{
            return m_rs.getId();
        }
    }
    
    public String getType(){
        if (isResultSummary()){
            String rsmType = "";
            switch (m_rsm.getResultSet().getType()){
                case SEARCH:
                    break;
                case DECOY_SEARCH:
                    rsmType = "DECOY";
                    break;
                case USER:
                    break;
                case DECOY_USER:
                    rsmType = "DECOY";
                    break;
                case QUANTITATION:
                    rsmType = "QUANTITATION";
                    break;
            }
            
            return TYPE_RSM+ " ("+rsmType+")";
        }
        return TYPE_RS + " ("+m_rs.getType()+")";
    }
    
    public String getRsName(){
        ResultSet rs = getResultSet();
        if (rs != null){
            return rs.getName();
        }
        return "";
    }
    
    public MsiSearch getMsiSearch(){
        ResultSet rs = getResultSet();
        if (rs != null){
            return rs.getMsiSearch();
        }
        return null;
    }
    
    public String getPeaklistPath(){
        MsiSearch msiS = getMsiSearch();
        if (msiS != null && msiS.getPeaklist() != null ){
            return msiS.getPeaklist().getPath();
        }
        return "";
    }
    
    public String getMsiSearchFileName(){
        MsiSearch msiS = getMsiSearch();
        if (msiS != null){
            return msiS.getResultFileName();
        }
        return "";
    }
    
    public String getMsiSearchDirectory(){
        MsiSearch msiS = getMsiSearch();
        if (msiS != null){
            return msiS.getResultFileDirectory();
        }
        return "";
    }
    
    public Timestamp getMsiSearchDate(){
        MsiSearch msiS = getMsiSearch();
        if (msiS != null){
            return msiS.getDate();
        }
        return null;
    }
    
    public boolean isSelected(){
        return m_isSelected;
    }
    
    public void setSelected(boolean isSelected){
        this.m_isSelected = isSelected;
    }

    public boolean isEditable() {
        return m_isEditable;
    }

    public void setIsEditable(boolean isEditable) {
        this.m_isEditable = isEditable;
    }
    
    
}
