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
package fr.proline.mzscope.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author CB205360
 */
public class ExtractionRequest {

    public static class Builder<T extends Builder<T>> {

        double mz = 0.0;
        double minMz = 0.0;
        double maxMz = 0.0;

        float elutionTime = -1.0f;
        float elutionTimeLowerBound = -1.0f;
        float elutionTimeUpperBound = -1.0f;

        double fragmentMz = -1.0;
        double fragmentMinMz = -1.0;
        double fragmentMaxMz = -1.0;

        int msLevel = 1;
         
        @SuppressWarnings("unchecked")  // Smell 1
        protected T self() {
            return (T) this;            // Unchecked cast!
        }

        public T setMinMz(double minMz) {
            this.minMz = minMz;
            return self();
        }

        public T setMaxMz(double maxMz) {
            this.maxMz = maxMz;
            return self();
        }

        public T setFragmentMz(double parentMz) {
            this.fragmentMz = parentMz;
            return self();
        }

        public T setFragmentMinMz(double minMz) {
            this.fragmentMinMz = minMz;
            return self();
        }

        public T setFragmentMaxMz(double maxMz) {
            this.fragmentMaxMz = maxMz;
            return self();
        }

        public T setElutionTimeLowerBound(float startRT) {
            this.elutionTimeLowerBound = startRT;
            return self();
        }

        public T setElutionTimeUpperBound(float stopRT) {
            this.elutionTimeUpperBound = stopRT;
            return self();
        }

        public T setElutionTime(float rt) {
            this.elutionTime = rt;
            return self();
        }

        public T setMz(double mz) {
            this.mz = mz;
            return self();
        }

        public T setMsLevel(int level) {
            this.msLevel = level;
            return self();
        }
        public double getMinMz() {
            return minMz;
        }

        public double getMaxMz() {
            return maxMz;
        }

        public double getMz() {
            return mz;
        }

        public double getFragmentMz() {
            return fragmentMz;
        }

        public double getFragmentMinMz() {
            return fragmentMinMz;
        }

        public double getFragmentMaxMz() {
            return fragmentMaxMz;
        }

        public ExtractionRequest build() {
            return new ExtractionRequest(this);
        }
    }

    private final double minMz;
    private final double maxMz;
    private final double mz;
    
    // values in seconds !! 
    private final float elutionTimeLowerBound;
    private final float elutionTimeUpperBound;
    private final float elutionTime;

    private final double fragmentMz;
    private final double fragmentMinMz;
    private final double fragmentMaxMz;

    private final int msLevel;
    
    protected ExtractionRequest(Builder builder) {
        this.maxMz = builder.getMaxMz();
        this.minMz = builder.getMinMz();
        this.elutionTimeLowerBound = builder.elutionTimeLowerBound;
        this.elutionTimeUpperBound = builder.elutionTimeUpperBound;
        this.elutionTime = builder.elutionTime;
        this.mz = builder.getMz();
        this.fragmentMz = builder.getFragmentMz();
        this.fragmentMinMz = builder.getFragmentMinMz();
        this.fragmentMaxMz = builder.getFragmentMaxMz();
        this.msLevel = builder.msLevel;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @SuppressWarnings("rawtypes")       // Smell 2
    public static Builder<?> builder() {
        return new Builder();           // Raw type - no type argument!
    }

    public double getMinMz() {
        return minMz;
    }

    public double getMaxMz() {
        return maxMz;
    }

    public double getMz() {
        return mz;
    }

    public float getElutionTimeLowerBound() {
        return elutionTimeLowerBound;
    }

    public float getElutionTimeUpperBound() {
        return elutionTimeUpperBound;
    }

    public float getElutionTime() {
        return elutionTime;
    }

    public double getFragmentMz() {
        return fragmentMz;
    }

    public double getFragmentMinMz() {
        return fragmentMinMz;
    }

    public double getFragmentMaxMz() {
        return fragmentMaxMz;
    }

    public int getMsLevel() {
        return msLevel;
    }

    public boolean isMsnExtraction() {
        return msLevel > 1;
    }


}
