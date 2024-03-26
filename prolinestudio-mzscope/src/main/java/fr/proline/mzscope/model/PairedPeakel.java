/*
 * Copyright (C) 2024
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

/**
 * Data of two paired peakels.
 * If a peakel has not been paired, we can have peakel1 or peakel2 set to null
 */
public class PairedPeakel implements Comparable<PairedPeakel> {

    public enum PairedPeakelStatus {
        NO_PAIRING("No Pairing"),
        PAIRING(""),
        MULTI_PAIRING("Multi Pairing"),
        PAIRING_APEX_PROBLEM("Apex Problem");

        private final String statusString;

        private PairedPeakelStatus(String s) {
            statusString = s;
        }

        public String toString() {
            return this.statusString;
        }
    }


    private IPeakel m_peakel1 = null;
    private IPeakel m_peakel2 = null;

    private PairedPeakelStatus m_status = PairedPeakelStatus.NO_PAIRING;

    public PairedPeakel() {

    }

    public void set(IPeakel peakel1, IPeakel peakel2, PairedPeakelStatus status) {
        m_peakel1 = peakel1;
        m_peakel2 = peakel2;
        m_status = status;
    }

    public IPeakel getPeakel1() {
        return m_peakel1;
    }

    public IPeakel getPeakel2() {
        return m_peakel2;
    }

    public double getMz() {
        if (m_peakel1 != null) {
            m_peakel1.getMz();
        }
        return m_peakel2.getMz();
    }

    public float getElutionTime() {
        if (m_peakel1 != null) {
            m_peakel1.getElutionTime();
        }
        return m_peakel2.getElutionTime();
    }

    public float getApexIntensity() {
        if (m_peakel1 != null) {
            m_peakel1.getApexIntensity();
        }
        return m_peakel2.getApexIntensity();
    }

    public PairedPeakelStatus getStatus() {
        return m_status;
    }

    @Override
    public int compareTo(PairedPeakel pp) {

        if (Math.abs(getMz() - pp.getMz()) > 0) {
            return getMz()<pp.getMz() ?  -1 : 1;
        }

        if (Math.abs(getElutionTime() - pp.getElutionTime()) > 0) {
            return getElutionTime()<pp.getElutionTime() ?  -1 : 1;
        }

        return (getApexIntensity()<pp.getApexIntensity()) ? -1 : 1;


    }

}
