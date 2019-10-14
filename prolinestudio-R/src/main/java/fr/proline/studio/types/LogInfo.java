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
package fr.proline.studio.types;

/**
 *
 * Class used to add information to a Table Model in the Data Analyzer
 * This class is used to know if a columns has been logged.
 * 
 * @author JM235353
 */
public class LogInfo {
    public enum LogState {
        LOG2,
        LOG10,
        NO_LOG
    }
    
    private final LogState m_state;
    
    public LogInfo(LogState state) {
        m_state = state;
    }
    
    public boolean noLog() {
        return m_state == LogState.NO_LOG;
    } 
    
    public boolean isLog() {
        return m_state != LogState.NO_LOG;
    } 
    
    public boolean isLog2() {
        return m_state == LogState.LOG2;
    } 
    
    public boolean isLog10() {
        return m_state == LogState.LOG10;
    } 
    
}
