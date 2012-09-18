/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.data;

import fr.proline.studio.dam.data.Data;
import fr.proline.core.algo.msi.inference.ParsimoniousProteinSetInferer;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.utils.generator.ResultSetFakeBuilder;
import scala.Option;
import scala.collection.immutable.Map;

/**
 *
 * @author JM235353
 */
public class ResultSummaryData extends Data {
 
    public ResultSummary resultSummary = null;

    public ResultSummaryData(ResultSummary resultSummary) {
        dataType = DataTypes.RESULT_SUMMARY;
        this.resultSummary = resultSummary;
    }

    @Override
    public String getName() {
        if (resultSummary != null) {
            return "Rsm"+resultSummary.getId();
        }
        return "";
    }
    
    public ResultSummary getResultSummary() {
        return resultSummary;
    }
    
    /*
     JPM.TODO : remove later
           

        
        // Retrieve Protein Groups ( <=> Protein Sets )
        ProteinSet[] proteinSets = rsm.proteinSets();
        
        // Retrieve First Protein Group
        ProteinSet proteinSet = proteinSets[0];
        
        // Retrieve Protein Matches
        Option<ProteinMatch[]> optionMatches = proteinSet.proteinMatches();
        ProteinMatch[] proteinMatches = null;
        if ((optionMatches!=null) && (optionMatches.isDefined())) {
            proteinMatches = optionMatches.get();
        }
        
        // Retrive the typical Protein Match
        Option<ProteinMatch> optionTypicalProtein = proteinSet.typicalProteinMatch();
        ProteinMatch typicalProtein = null;
        if ((optionTypicalProtein!=null) && (optionTypicalProtein.isDefined())) {
            typicalProtein = optionTypicalProtein.get();
        }
        
        // Retrieve the number of peptids in the typical Protein
        int peptidesInSameSetNumber = typicalProtein.peptideMatchesCount();
        
        // Retrieve first Protein Match
        ProteinMatch proteinMatch = proteinMatches[0];
        
        // Is in sameSet or subSet
        boolean sameSet = (peptidesInSameSetNumber == proteinMatch.peptideMatchesCount());
        
        // Retrieve Protein
        Option<Protein> optionProtein = proteinMatch.protein();
        Protein protein = null;
        if ((optionProtein!=null) && (optionProtein.isDefined())) {
            protein = optionProtein.get();
        }
        
        // Retrieve the Peptides of a Protein Match
        SequenceMatch[] sequenceMatches = proteinMatch.sequenceMatches();
        SequenceMatch sequenceMatch = sequenceMatches[0];
        Option<Peptide> optionPeptide = sequenceMatch.peptide();
        Peptide peptide = null;
        if ((optionPeptide!=null) && (optionPeptide.isDefined())) {
            peptide = optionPeptide.get();
        }
        
        
        
      
    
     */
    
        /*public void generateTestData() {
        ResultSetFakeBuilder resultSetFakeBuilder = new ResultSetFakeBuilder(10, 2, 0, 8, 20);
        ResultSet resultSet = resultSetFakeBuilder.toResultSet();
        ParsimoniousProteinSetInferer ppsi = new ParsimoniousProteinSetInferer();
        resultSummary = ppsi.computeResultSummary(resultSet);
        
        // Retrieve Protein Groups ( <=> Protein Sets )
        ProteinSet[] proteinSets = resultSummary.proteinSets();
        for (ProteinSet proteinSet : proteinSets) {
            Option<ProteinMatch[]> optionMatches = proteinSet.proteinMatches();
            ProteinMatch[] proteinMatches = null;
            if ((optionMatches!=null) && (optionMatches.isDefined())) {
                proteinMatches = optionMatches.get();
            }
            if (proteinMatches == null) {
                int[] ids = proteinSet.proteinMatchIds();
                Map<Object, ProteinMatch> map = resultSet.proteinMatchById();
                
                proteinMatches = new ProteinMatch[ids.length];
                for (int i=0; i<ids.length ; i++) {
                    ProteinMatch proteinMatch = null;
                    Option<ProteinMatch> proteinMatchOption = map.get(new Integer(ids[i]));
                    if ((proteinMatchOption!=null) && (proteinMatchOption.isDefined())) {
                        proteinMatch = proteinMatchOption.get();
                    }
                    proteinMatches[i] = proteinMatch;
                }
                
                proteinSet.proteinMatches_$eq(Option.apply(proteinMatches));
            }
        }
        
    }*/
    
    
}
