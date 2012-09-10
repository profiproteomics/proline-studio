/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam;

import fr.proline.core.algo.msi.inference.ParsimoniousProteinSetInferer;
import fr.proline.core.om.model.msi.*;
import fr.proline.core.utils.generator.ResultSetFakeBuilder;
import scala.Option;

/**
 *
 * @author JM235353
 */
public class ResultSummaryData extends ContainerData {
 
        public ResultSummary resultSummary = null;
    
        public ResultSummaryData() {
            dataType = DataTypes.RESULT_SUMMARY;
        }
    
    public void generateTestData() {
        ResultSetFakeBuilder resultSetFakeBuilder = new ResultSetFakeBuilder(10, 2, 0, 8, 20);
        ResultSet resultSet = resultSetFakeBuilder.toResultSet();
        ParsimoniousProteinSetInferer ppsi = new ParsimoniousProteinSetInferer();
        resultSummary = ppsi.computeResultSummary(resultSet);
    }
    
    /*
     JPM.TODO : remove later
           
        ResultSetFakeBuilder resultSetFakeBuilder = new ResultSetFakeBuilder(10, 2, 0, 8, 20);
        ResultSet resultSet = resultSetFakeBuilder.toResultSet();
        ParsimoniousProteinSetInferer ppsi = new ParsimoniousProteinSetInferer();
	ResultSummary rsm = ppsi.computeResultSummary(resultSet);
        
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
    
}
