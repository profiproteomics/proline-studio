/* 
 * Copyright (C) 2019
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

import fr.profi.mzdb.XicMethod;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author CB205360
 */
public class ExtractionRequest {

    public enum Type { NONE, RANGE, CENTERED }

    public static class Builder<T extends Builder<T>> {

        Object source = null;

        double mz = -1.0;
        double minMz = -1.0;
        double maxMz = -1.0;
        float mzTolPPM = 10.0f;
        Type mzRequestType = Type.NONE;

        float elutionTime = -1.0f;
        float elutionTimeLowerBound = -1.0f;
        float elutionTimeUpperBound = -1.0f;

        double fragmentMz = -1.0;
        double fragmentMinMz = -1.0;
        double fragmentMaxMz = -1.0;
        float fragmentMzTolPPM = 50.0f;
        Type fragmentRequestType = Type.NONE;

        double mobility = -1.0;
        double minMobility = -1.0;
        double maxMobility = -1.0;
        float mobilityTol = -1.0f;
        Type mobilityRequestType = Type.NONE;

        int msLevel = 1;

        XicMethod method = XicMethod.MAX;

        protected T self() {
            return (T) this;
        }

        public void setSource(Object source) {
            this.source = source;
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

        public T setMsLevel(int level) {
            this.msLevel = level;
            return self();
        }

        public T setMzTolPPM(float mzTolPPM) {
            mzRequestType = Type.CENTERED;
            this.mzTolPPM = mzTolPPM;
            if (mz > 0.0) {
                maxMz = (mz + mz * mzTolPPM / 1e6f);
                minMz = (mz - mz * mzTolPPM / 1e6f);
            }
            return self();
        }

        public T setMz(double mz) {
            mzRequestType = Type.CENTERED;
            this.mz = mz;
            maxMz = (mz + mz * mzTolPPM / 1e6f);
            minMz = (mz - mz * mzTolPPM / 1e6f);
            return self();
        }

        public T setMaxMz(double maxMz) {
            mzRequestType = Type.RANGE;
            this.maxMz = maxMz;
            if (minMz > 0.0) {
                mz = (maxMz + minMz)/2.0;
                mzTolPPM = (float) (1e6 * (maxMz - minMz) / (2*mz));
            }
            return self();
        }

        public T setMinMz(double minMz) {
            mzRequestType = Type.RANGE;
            this.minMz = minMz;
            if (maxMz > 0.0) {
                mz = (maxMz + minMz)/2.0;
                mzTolPPM = (float) (1e6 * (maxMz - minMz) / (2*mz));
            }
            return self();
        }

        public T setFragmentMz(double mz){
            fragmentRequestType = Type.CENTERED;
            fragmentMz = mz;
            fragmentMaxMz = (fragmentMz + fragmentMz * fragmentMzTolPPM / 1e6f);
            fragmentMinMz = (fragmentMz - fragmentMz * fragmentMzTolPPM / 1e6f);
            setMsLevel(2);
            return self();
        }

        public T setFragmentMaxMz(double maxMz) {
            fragmentRequestType = Type.RANGE;
            this.fragmentMaxMz = maxMz;
            if (fragmentMinMz > 0.0) {
                fragmentMz = (fragmentMaxMz + fragmentMinMz)/2.0;
                fragmentMzTolPPM = (float) (1e6 * (fragmentMaxMz - fragmentMinMz) / (2*fragmentMz));
            }
            setMsLevel(2);
            return self();
        }

        public T setFragmentMinMz(double minMz) {
            fragmentRequestType = Type.RANGE;
            this.fragmentMinMz = minMz;
            if (fragmentMaxMz > 0.0) {
                fragmentMz = (fragmentMaxMz + fragmentMinMz)/2.0;
                fragmentMzTolPPM = (float) (1e6 * (fragmentMaxMz - fragmentMinMz) / (2*fragmentMz));
            }
            setMsLevel(2);
            return self();
        }

        public T setFragmentMzTolPPM(float mzTolPPM) {
            fragmentRequestType = Type.CENTERED;
            fragmentMzTolPPM = mzTolPPM;
            if (fragmentMz > 0.0) {
                fragmentMaxMz = (fragmentMz + fragmentMz * fragmentMzTolPPM / 1e6f);
                fragmentMinMz = (fragmentMz - fragmentMz * fragmentMzTolPPM / 1e6f);
            }
            setMsLevel(2);
            return self();
        }

        public T setMethod(XicMethod method) {
            this.method = method;
            return self();
        }

        public void setMobility(double mobility) {
            mobilityRequestType = Type.CENTERED;
            this.mobility = mobility;
            if (mobilityTol > 0) {
                minMobility = mobility - mobilityTol;
                maxMobility = mobility + mobilityTol;
            }
        }

        public void setMinMobility(double minMobility) {
            mobilityRequestType = Type.RANGE;
            this.minMobility = minMobility;
            if (maxMobility > 0) {
                mobilityTol = (float)((maxMobility - minMobility)/2.0);
            }
        }

        public void setMaxMobility(double maxMobility) {
            mobilityRequestType = Type.RANGE;
            this.maxMobility = maxMobility;
            if (minMobility > 0) {
                mobilityTol = (float)((maxMobility - minMobility)/2.0);
            }
        }

        public void setMobilityTol(float mobilityTol) {
            mobilityRequestType = Type.CENTERED;
            this.mobilityTol = mobilityTol;
            if (mobility > 0) {
                minMobility = mobility - mobilityTol;
                maxMobility = mobility + mobilityTol;
            }
        }

        public ExtractionRequest build() {
            return new ExtractionRequest(this);
        }
    }

    private Object source;
    private final double minMz;
    private final double maxMz;
    private final double mz;
    private final float mzTolPPM;
    private final Type mzRequestType;

    // values in seconds !! 
    private final float elutionTimeLowerBound;
    private final float elutionTimeUpperBound;
    private final float elutionTime;

    private final double fragmentMz;
    private final double fragmentMinMz;
    private final double fragmentMaxMz;
    private final Type fragmentRequestType;
    private final float fragmentMzTolPPM;

    private final double mobility;
    private final double minMobility;
    private final double maxMobility;
    private final float mobilityTol;
    private final Type mobilityRequestType;

    private final int msLevel;

    private final XicMethod method;

    protected ExtractionRequest(Builder builder) {
        this.mz = builder.mz;
        this.maxMz = builder.maxMz;
        this.minMz = builder.minMz;
        this.mzTolPPM = builder.mzTolPPM;
        this.mzRequestType = builder.mzRequestType;
        this.elutionTimeLowerBound = builder.elutionTimeLowerBound;
        this.elutionTimeUpperBound = builder.elutionTimeUpperBound;
        this.elutionTime = builder.elutionTime;
        this.fragmentMz = builder.fragmentMz;
        this.fragmentMinMz = builder.fragmentMinMz;
        this.fragmentMaxMz = builder.fragmentMaxMz;
        this.fragmentMzTolPPM = builder.fragmentMzTolPPM;
        this.fragmentRequestType = builder.fragmentRequestType;
        this.mobility = builder.mobility;
        this.minMobility = builder.minMobility;
        this.maxMobility = builder.maxMobility;
        this.mobilityTol = builder.mobilityTol;
        this.mobilityRequestType = builder.mobilityRequestType;
        this.method = builder.method;
        this.msLevel = builder.msLevel;
        this.source = builder.source;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static Builder<?> builder(Object source) {
        Builder builder = new Builder();
        builder.setSource(source);
        return builder;
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

    public Type getMzRequestType() {
        return mzRequestType;
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

    public Type getFragmentRequestType() {
        return fragmentRequestType;
    }

    public int getMsLevel() {
        return msLevel;
    }

    public double getMobility() {
        return mobility;
    }

    public double getMinMobility() {
        return minMobility;
    }

    public double getMaxMobility() {
        return maxMobility;
    }

    public float getMobilityTol() {
        return mobilityTol;
    }

    public Type getMobilityRequestType() {
        return mobilityRequestType;
    }

    public float getMzTolPPM() {
        return mzTolPPM;
    }

    public float getFragmentMzTolPPM() {
        return fragmentMzTolPPM;
    }

    public XicMethod getMethod() {
        return method;
    }

    public boolean isMsnExtraction() {
        return msLevel > 1;
    }

    public Object getSource() {
        return source;
    }
}
