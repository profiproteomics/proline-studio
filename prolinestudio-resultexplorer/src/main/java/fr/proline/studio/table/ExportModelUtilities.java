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
package fr.proline.studio.table;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.studio.dam.tasks.data.ProjectInfo;
import fr.proline.studio.export.ExportFontData;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.poi.hssf.util.HSSFColor;

/**
 *
 * @author JM235353
 */
public class ExportModelUtilities extends fr.proline.studio.export.ExportModelUtilities {

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



    public static ArrayList<ExportFontData> getExportFonts(String projectIds, HashMap<String, ProjectInfo.Status> projectStatusMap) {
        if (projectIds != null && projectStatusMap != null) {
            ArrayList<ExportFontData> exportFontDatas = new ArrayList<>();
            
            String[] ids = projectIds.split(",");
            int start = 0, stop = 0;
            short colorIndex;
            for (String id : ids) {
                ProjectInfo.Status status = projectStatusMap.get(id);
                stop = start + id.length();
                switch (status) {
                    case ARCHIVED:
                        colorIndex = HSSFColor.GREY_50_PERCENT.index;
                        break;
                    case INACTIVE:
                        colorIndex = HSSFColor.BLUE.index;
                        break;
                    default:
                        colorIndex = HSSFColor.BLACK.index;
                        break;
                }
                exportFontDatas.add(new ExportFontData(start, stop, colorIndex));
                start = stop + 1; //skip ","
            }
            return exportFontDatas;
        }
        return null;
    }
}