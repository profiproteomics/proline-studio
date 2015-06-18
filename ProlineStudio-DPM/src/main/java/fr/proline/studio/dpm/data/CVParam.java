package fr.proline.studio.dpm.data;

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
    
    
}
