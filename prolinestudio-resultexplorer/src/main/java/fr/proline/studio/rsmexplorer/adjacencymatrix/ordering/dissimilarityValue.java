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
