package fr.proline.studio.export;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.poi.hssf.util.HSSFColor;

/**
 *
 * @author JM235353
 */
public class ExportModelUtilities {

    public static String getExportRowCell(ExtendedTableModelInterface dataInterface, int row, int col) {
        Object o = dataInterface.getDataValueAt(row, col);
        if (o != null) {
            if ((o instanceof Double) && (((Double) o).isNaN())) {
                return "";
            }
            if ((o instanceof Float) && (((Float) o).isNaN())) {
                return "";
            }
            return o.toString();
        }
        return null;
    }

    public static ArrayList<ExportFontData> getExportFonts(DPeptideMatch peptideMatch) {
        Peptide peptide = peptideMatch.getPeptide();
        if (peptide.getTransientData() != null) {
            HashMap<Integer, DPeptidePTM> ptmMap = peptide.getTransientData().getDPeptidePtmMap();
            if (ptmMap != null) {

                ArrayList<ExportFontData> ExportFontDatas = new ArrayList<>();
                String sequence = peptide.getSequence();

                int nb = sequence.length();
                for (int i = 0; i < nb; i++) {
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

                            ExportFontData newSubStringFont = new ExportFontData(i, i + 1, HSSFColor.VIOLET.index);
                            ExportFontDatas.add(newSubStringFont);

                        } else if (nTerOrCterModification) {

                            ExportFontData newSubStringFont = new ExportFontData(i, i + 1, HSSFColor.GREEN.index);
                            ExportFontDatas.add(newSubStringFont);

                        } else if (aminoAcidModification) {

                            ExportFontData newSubStringFont = new ExportFontData(i, i + 1, HSSFColor.ORANGE.index);
                            ExportFontDatas.add(newSubStringFont);
                        }

                    }

                }
                return ExportFontDatas;
            }
        }
        return null;
    }

    public static ArrayList<ExportFontData> getExportFonts(DPeptideInstance peptideInstance) {
        if ((peptideInstance != null) && (peptideInstance.getBestPeptideMatch() != null)) {
            return ExportModelUtilities.getExportFonts(peptideInstance.getBestPeptideMatch());
        }
        return null;
    }

}
