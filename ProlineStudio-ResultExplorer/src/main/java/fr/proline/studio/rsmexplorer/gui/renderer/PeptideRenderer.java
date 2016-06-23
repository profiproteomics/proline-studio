package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.studio.export.ExportSubStringFont;
import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.utils.GlobalValues;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Renderer for a Peptide in a Table Cell
 * @author JM235353
 */
public class PeptideRenderer extends DefaultTableCellRenderer implements ExportTextInterface {

    private String m_basicTextForExport = "";
    private ArrayList<ExportSubStringFont> m_ExportSubStringFonts;
    private HSSFWorkbook wb;
    
    public void PeptideRenderer() {
        wb = new HSSFWorkbook();
        m_ExportSubStringFonts = new ArrayList<ExportSubStringFont>();
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        String displayString;
        
        if (value == null) {
            displayString = "";
        } else {
            DPeptideMatch pm = (DPeptideMatch) value;

            displayString = constructPeptideDisplay(pm);
        }
        
        return super.getTableCellRendererComponent(table, displayString, isSelected, hasFocus, row, column);
    }

    private String constructPeptideDisplay(DPeptideMatch peptideMatch) {
        

        Peptide peptide = peptideMatch.getPeptide();
        if (peptide.getTransientData() != null) {

            
            
            HashMap<Integer, DPeptidePTM> ptmMap = peptide.getTransientData().getDPeptidePtmMap();
            if (ptmMap != null) {
                m_displaySB.append("<HTML>");
            }



            String sequence = peptide.getSequence();

            if (ptmMap == null) {
                m_displaySB.append(sequence);
                m_exportSB.append(sequence);
            } else {


                int nb = sequence.length();
                for (int i = 0; i < nb; i++) {
                    
                    HSSFFont currentFont;

                    boolean nTerOrCterModification = false;
                    if (i == 0) {
                        DPeptidePTM nterPtm = ptmMap.get(0);
                        if (nterPtm != null) {
                            nTerOrCterModification = true;
                        }
                    } else if (i == nb - 1) {
                        DPeptidePTM cterPtm = ptmMap.get(-1);
                        if (cterPtm != null) {
                            nTerOrCterModification = true;
                        }
                    }

                    DPeptidePTM ptm = ptmMap.get(i + 1);
                    boolean aminoAcidModification = (ptm != null);

                    if (nTerOrCterModification || aminoAcidModification) {
                        
                        
                        if (nTerOrCterModification && aminoAcidModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_VIOLET).append("'>");
                            //currentFont.setColor(HSSFColor.VIOLET.index);
                        } else if (nTerOrCterModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_GREEN).append("'>");
                            //currentFont.setColor(HSSFColor.GREEN.index);
                        } else if (aminoAcidModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_ORANGE).append("'>");
                            //currentFont.setColor(HSSFColor.ORANGE.index);
                        }
                        m_displaySB.append(sequence.charAt(i));
                        m_displaySB.append("</span>");
                        
                        //ExportSubStringFont newSubStringFont = new ExportSubStringFont(i,i+1, currentFont);
                        //m_ExportSubStringFonts.add(newSubStringFont);
                        
                        
                    } else {
                        m_displaySB.append(sequence.charAt(i));
                    }
                    m_exportSB.append(sequence.charAt(i));

                }

            }


            if (ptmMap != null) {
                m_displaySB.append("</HTML>");
            }

            m_basicTextForExport = m_exportSB.toString();
            m_exportSB.setLength(0);
            
            String res = m_displaySB.toString();
            m_displaySB.setLength(0);
            return res;
        }

        if (peptide == null) {
            m_basicTextForExport = "";
        } else {
            m_basicTextForExport = peptide.getSequence();
        }
        
        return m_basicTextForExport;


    }
    private final StringBuilder m_displaySB = new StringBuilder();
    private final StringBuilder m_exportSB = new StringBuilder();

    @Override
    public String getExportText() {
        return m_basicTextForExport;
    }

    @Override
    public ArrayList<ExportSubStringFont> getSubStringFonts() {
        return this.m_ExportSubStringFonts;
    }
}
