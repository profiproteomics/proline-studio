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
package fr.proline.studio.performancetest;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class to be used to check speed of code. It is thread safe and can be used in loops.
 * The display of time elapsed is done in log in debug mode.
 * If debug mode for log is not enable, the code does nothing.
 * You can execute multiple times your code, the time elapsed is automatically added.
 * 
 * 
 * Example of use:
 * 
 * public void aMethod() {
 *      PerformanceTest.startTime("aMethod");
 *      ...
 *      for (...) {
 *          PerformanceTest.startTime("STEP1");
 *          ...
 *          PerformanceTest.stopTime("STEP1");
 *          PerformanceTest.startTime("STEP2");
 *          ...
 *          PerformanceTest.stopTime("STEP2");
 *      }
 *      ...
 *      PerformanceTest.stopTime("aMethod");
 * 
 *      PerformanceTest.displayAll();
 * 
 * 
 * 
 * @author Jean-Philippe
 */
public class PerformanceTest {

    private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.Commons.PerformanceTest");

    private static HashMap<String, PerformanceTestData> m_map = new HashMap<>();

    /**
     * To put at the beginning of a code to measure the time elapsed
     * @param key 
     */
    public static void startTime(String key) {
        startTime(key, false);
    }
    public static void startTime(String key, boolean global) {

        if (!m_logger.isDebugEnabled()) {
            return;
        }

        startTimeSync(key, global);
    }

    /**
     * To put at the end of a code to measure the time elapsed.
     * The key must correspond to the key used for startTime()
     * 
     * @param key 
     */
    public static void stopTime(String key) {
        stopTime(key, false);
    }
    public static void stopTime(String key, boolean global) {

        if (!m_logger.isDebugEnabled()) {
            return;
        }

        stopTimeSync(key, global);
    }

    /**
     * Display time ellapsed in all code areas in all threads
     * The log is sorted according to time ellapsed.
     * 
     */
    public static void displayTimeAllThreads() {

        if (!m_logger.isDebugEnabled()) {
            return;
        }

        displayTimeAllThreadSync();
    }

    /**
     * Display time ellapsed in all code areas in current thread
     * The log is sorted according to time ellapsed.
     * 
     */
    public static void displayTimeCurrentThread() {

        if (!m_logger.isDebugEnabled()) {
            return;
        }

        displayTimeCurrentThreadSync();
    }

    /**
     * Clear time ellapsed for all thread
     */
    public static void clearTimeAllThreads() {

        if (!m_logger.isDebugEnabled()) {
            return;
        }

        clearTimeAllThreadSync();
    }

    /**
     * Clear time ellapsed for current thread
     */
    public static void clearTimeCurrentThread() {

        if (!m_logger.isDebugEnabled()) {
            return;
        }

        clearTimeCurrentThreadSync();
    }

    private static synchronized void startTimeSync(String key, boolean global) {

        getPerformanceTestData().startTime(key, global);
    }

    private static synchronized void stopTimeSync(String key, boolean global) {

        getPerformanceTestData().stopTime(key, global);
    }

    private static synchronized void displayTimeAllThreadSync() {

        PerformanceTestData mergedTestData = new PerformanceTestData(m_logger);
        for (String threadKey : m_map.keySet()) {
            PerformanceTestData testData = getPerformanceTestData(threadKey);
            mergedTestData.merge(testData);
        }

        mergedTestData.displayTime();
    }

    private static synchronized void displayTimeCurrentThreadSync() {
        getPerformanceTestData().displayTime();
    }

    private static synchronized void clearTimeAllThreadSync() {
        m_map.clear();
    }

    private static synchronized void clearTimeCurrentThreadSync() {
        String threadKey = Thread.currentThread().toString();
        m_map.remove(threadKey);
    }

    private static PerformanceTestData getPerformanceTestData() {
        String threadKey = Thread.currentThread().toString();
        return getPerformanceTestData(threadKey);
    }

    private static PerformanceTestData getPerformanceTestData(String threadKey) {

        PerformanceTestData testData = m_map.get(threadKey);
        if (testData == null) {
            testData = new PerformanceTestData(m_logger);
            m_map.put(threadKey, testData);
        }

        return testData;
    }

}
