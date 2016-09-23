package fr.proline.studio.rsmexplorer.adjacencymatrix.ordering;

import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class dissimilarityValue implements DissimilarityMeasure {

    private double[][] m_simMatrix;

    public dissimilarityValue(double[][] simMatrix) {
        m_simMatrix = simMatrix;
    }

    @Override
    public double computeDissimilarity(Experiment arg0, int arg1, int arg2) {
        double disSimilarity = 1 - m_simMatrix[arg1][arg2];
        return disSimilarity;
    }

}
