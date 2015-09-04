package fr.proline.studio.rsmexplorer.adjacencymatrix.ordering;

import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class dissimilarityValue implements DissimilarityMeasure {

    double[][] simMatrix;

    public dissimilarityValue(double[][] simMatrix) {
        this.simMatrix = simMatrix;
    }

    @Override
    public double computeDissimilarity(Experiment arg0, int arg1, int arg2) {
        double disSimilarity = 1 - simMatrix[arg1][arg2];
        return disSimilarity;

    }

}
