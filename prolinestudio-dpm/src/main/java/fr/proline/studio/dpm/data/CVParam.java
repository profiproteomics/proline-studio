/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.dpm.data;

import java.util.Objects;

/**
 *
 * @author VD225637
 */
public class CVParam {

    private String m_cvLabel;
    private String m_name;
    private String m_accession;
    private String m_value;

    public CVParam(String cvLabel, String accession, String name, String value) {
        this.m_cvLabel = cvLabel;
        this.m_accession = accession;
        this.m_name = name;
        if(value == null)
            this.m_value = "";
        else 
            this.m_value = value;
    }

    public String getCvLabel() {
        return m_cvLabel;
    }

    public void setCvLabel(String cvLabel) {
        this.m_cvLabel = cvLabel;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        this.m_name = name;
    }

    public String getAccession() {
        return m_accession;
    }

    public void setAccession(String accession) {
        this.m_accession = accession;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
       if(value == null)
            this.m_value = "";
        else 
            this.m_value = value;
    }

    @Override
    public String toString() {
        return m_name + " [" + m_accession + ']';
    }
    
    public String toXMLString() {
        // <cvParam cvLabel="BTO" accession="BTO:0000089 " name="blood"/>
        
        String result ="<cvParam cvLabel=\""+m_cvLabel+"\" accession=\""+m_accession +"\" name=\""+m_name+"\"  value=\""+m_value+"\" />";
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
            
        if (obj instanceof CVParam) {
            return (this.m_cvLabel.equals( ((CVParam)obj).m_cvLabel ) && 
                    this.m_accession.equals( ((CVParam)obj).m_accession ) );
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.m_cvLabel);
        hash = 17 * hash + Objects.hashCode(this.m_name);
        hash = 17 * hash + Objects.hashCode(this.m_accession);
        return hash;
    }
   
    
}
