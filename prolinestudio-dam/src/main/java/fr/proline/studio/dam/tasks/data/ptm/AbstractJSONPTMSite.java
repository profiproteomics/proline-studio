/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
