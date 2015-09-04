package fr.proline.studio.rsmexplorer.adjacencymatrix.ordering;

import ch.usi.inf.sape.hac.experiment.Experiment;

public class ExperimentData implements Experiment {

    int sizeOfArray;

    public ExperimentData(int sizeOfArray) {
        this.sizeOfArray = sizeOfArray;
    }

    @Override
    public int getNumberOfObservations() {
        // TODO Auto-generated method stub
        return sizeOfArray;
    }

}
