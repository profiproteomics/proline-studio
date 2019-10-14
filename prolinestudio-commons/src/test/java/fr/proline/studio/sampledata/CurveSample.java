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
package fr.proline.studio.sampledata;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Karine XUE
 */
public class CurveSample {

    static int[] SAMPLE_CURVE_Y = {5, 4, 3, 2, 3, 5, 9, 11, 10, 8, 7, 6, 3, 1, 2, 2, 2, 4, 5, 6, 7, 10, 11, 12, 10, 9, 8, 7, 6, 5};
    ArrayList<Float> _xValueSet;
    ArrayList<Float> _yValueSet;

    /**
     * creat a curve with peak whose X has distance between them
     *
     * @param valueYSet
     * @param xLength,
     * @param nbPoint
     */
    public CurveSample(int[] valueYSet, int xLength, int nbPoint) {

        int step = valueYSet.length - 1;
        int distance = Math.max(nbPoint / step, 1);
        float xSpace = (float) xLength / (nbPoint - 1);
        _xValueSet = new ArrayList();
        _yValueSet = new ArrayList();
        float x = 0;
        float y = valueYSet[0];
        float ySpace = 0;
        for (int i = 0; i < step - 1; i++) {
            ySpace = (float) (valueYSet[i + 1] - valueYSet[i]) / distance;
            for (int j = i * distance; j < (i + 1) * distance; j++) {

                _xValueSet.add(x);
                _yValueSet.add(y);
                x += xSpace;
                y += ySpace;
                if (_xValueSet.size() == nbPoint) {
                    return;
                }
            }

        }
        int rest = nbPoint - _xValueSet.size();
        for (int k = 0; k < rest; k++) {
            _xValueSet.add(x);
            _yValueSet.add(y);
            x += xSpace;
            y += ySpace;
        }
    }

    @Override
    public String toString() {
        String s = "size=" + _xValueSet.size() + " \n";
        for (int i = 0; i < _xValueSet.size(); i++) {
            s += "(" + _xValueSet.get(i) + "," + _yValueSet.get(i) + ")";
        }
        return s;
    }

    public CurveSample(int[] distributedX, int[] valueYSet, int xLength) {

    }

    public List<Object[]> getRowList() {
        List<Object[]> result = new ArrayList();
        Float[] row;
        for (int i = 0; i < _xValueSet.size(); i++) {
            row = new Float[3];

            row[0] = _xValueSet.get(i);
            row[1] = _yValueSet.get(i);
            row[2] = 0f;
            result.add(row);
        }
        return result;
    }

    public static void main(String[] args) {
        int[] test = {5, 4, 3, 2, 3, 5, 9, 11, 10, 8, 7, 6, 3, 1, 2, 2, 2, 4, 5, 6, 7, 10, 11, 12, 10, 9, 8, 7, 6, 5};
        CurveSample cs = new CurveSample(test, 12, 60);
        System.out.println(cs.toString());

    }
}
