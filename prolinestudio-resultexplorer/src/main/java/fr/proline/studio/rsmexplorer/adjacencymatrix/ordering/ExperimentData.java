package fr.proline.studio.rsmexplorer.adjacencymatrix.ordering;

import ch.usi.inf.sape.hac.experiment.Experiment;

public class ExperimentData implements Experiment {

    private int m_sizeOfArray;

    public ExperimentData(int sizeOfArray) {
        m_sizeOfArray = sizeOfArray;
    }

    @Override
    public int getNumberOfObservations() {
        return m_sizeOfArray;
    }

}
