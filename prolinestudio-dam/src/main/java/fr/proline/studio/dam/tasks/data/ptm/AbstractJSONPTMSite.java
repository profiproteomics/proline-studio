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
package fr.proline.studio.dam.tasks.data.ptm;

import java.util.List;
import java.util.Map;

/**
 *
 * @author VD225637
 */
public class AbstractJSONPTMSite {
    
    public Long proteinMatchId;
    public Long ptmDefinitionId;
    public Integer seqPosition;
    public Long bestPeptideMatchId;
    public Float localizationConfidence;
    // List of peptides ID matching this PTMSite by the position of this PTM in peptide Seq
    public Map<Integer, List<Long>> peptideIdsByPtmPosition;

    
}
