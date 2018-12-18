/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data.ptm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.proline.core.orm.msi.PtmSpecificity.PtmLocation;
import fr.proline.core.orm.msi.dto.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author CB205360
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONPTMSite {
    //
    // Variables defined in Proline OM that will be read out from the database
    //@todo : try to make them private instead of public

    public Long proteinMatchId;
    public Long ptmDefinitionId;
    public Integer seqPosition;
    public Long bestPeptideMatchId;
    public Float localizationConfidence;
    // List of peptides ID matching this PTMSite by the position of this PTM in peptide Seq
    public Map<Integer, List<Long>> peptideIdsByPtmPosition;
    //List of all PeptidesInstances Ids (in leaf RSM)
    public Long[] peptideInstanceIds;
    //List of PepidesInstances which don't match this PTM but with same mass
    public Long[] isomericPeptideInstanceIds;


}
