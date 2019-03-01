/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author CB205360
 */
public class QCMetrics {
    
    public static String MAX_MS_LEVEL = "Max MS level";
    
    // WARNING : insert a null value at the head as a marker telling that 
    // the following values are values for each msLevel
    private Map<String, List<Object>> scalarMSLevelMetrics;
    private Map<String, List<DescriptiveStatistics>> populationMSLevelMetrics;
    private String name;
    transient private IRawFile rawFile;

    public QCMetrics(IRawFile rawFile) {
        this.rawFile = rawFile;
        this.name = rawFile.getName();
        scalarMSLevelMetrics = new LinkedHashMap<>();
        populationMSLevelMetrics = new LinkedHashMap<>();
    }

    // for deserialization purposes
    protected QCMetrics() {
        
    }

    public IRawFile getRawFile() {
        return rawFile;
    }

    public String getName() {
        return name;
    }

    public void setRawFile(IRawFile rawFile) {
        this.rawFile = rawFile;
    }
    
    public void addMetric(String name, Object value) {
        if (populationMSLevelMetrics.containsKey(name)) throw new IllegalArgumentException("Metric name already used to describe a population metric");
        if (!scalarMSLevelMetrics.containsKey(name)) {
            scalarMSLevelMetrics.put(name, new ArrayList<>());
        }
        scalarMSLevelMetrics.get(name).add(value);
    }
    
    public void addMetric(String name, Object[] values) {
        if (populationMSLevelMetrics.containsKey(name)) throw new IllegalArgumentException("Metric name already used to describe a population metric");
        if (!scalarMSLevelMetrics.containsKey(name)) {
            scalarMSLevelMetrics.put(name, new ArrayList<>());
        }
        // insert a null value at the head as a marker
        scalarMSLevelMetrics.get(name).add(null);
        scalarMSLevelMetrics.get(name).addAll(Arrays.asList(values));
    }
    
    public void addMetric(String name, DescriptiveStatistics value) {
        if (scalarMSLevelMetrics.containsKey(name)) throw new IllegalArgumentException("Metric name already used to describe a scalar metric");
        if (!populationMSLevelMetrics.containsKey(name)) {
            populationMSLevelMetrics.put(name, new ArrayList<>());
        }
        populationMSLevelMetrics.get(name).add(value);        
    }
    
    public void addMetric(String name, DescriptiveStatistics[] values) {
        if (scalarMSLevelMetrics.containsKey(name)) throw new IllegalArgumentException("Metric name already used to describe a scalar metric");
        if (!populationMSLevelMetrics.containsKey(name)) {
            populationMSLevelMetrics.put(name, new ArrayList<>());
        }
        // insert a null value at the head as a marker
        populationMSLevelMetrics.get(name).add(null);
        populationMSLevelMetrics.get(name).addAll(Arrays.asList(values));   
    }

    public DescriptiveStatistics[] getMetricStatistics(String name) {
        List<DescriptiveStatistics> stats = populationMSLevelMetrics.get(name);
        if (stats == null) 
            return null;
        return stats.toArray(new DescriptiveStatistics[0]);
    }
    
    public List<String> getPopulationMetricNames() {
        return new ArrayList<String>(populationMSLevelMetrics.keySet());
    }
    
    public Object[] getMetricValue(String name) {
        return scalarMSLevelMetrics.get(name).toArray(new Object[0]);
    }

    public Map<String, Object> asSummary() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        for (Map.Entry<String, List<Object>> e : scalarMSLevelMetrics.entrySet()) {
            List<Object> values = e.getValue();
            if (values.get(0) == null) {
                for (int msLevel = 1; msLevel < values.size(); msLevel++) {
                    if (values.get(msLevel) != null) {
                        String key = buildKey(e.getKey(), msLevel);
                        metrics.put(key, values.get(msLevel));
                    }
                }
            } else {
                metrics.put(e.getKey(), values.get(0));
            }
        }
        
        for (Map.Entry<String, List<DescriptiveStatistics>> e : populationMSLevelMetrics.entrySet()) {
            List<DescriptiveStatistics> values = e.getValue();
            if (values.get(0) == null) {
                for (int msLevel = 1; msLevel < values.size(); msLevel++) {
                    if (values.get(msLevel) != null) {
                        String key = buildKey(e.getKey(), msLevel);
                        DescriptiveStatistics stats = values.get(msLevel);
                        metrics.put(key+" Q1", stats.getPercentile(25.0));
                        metrics.put(key+" Q2", stats.getPercentile(50.0));
                        metrics.put(key+" Q3", stats.getPercentile(75.0));
                        metrics.put(key+" Q4", stats.getPercentile(100.0));
                        metrics.put(key+" mean", stats.getMean());
                        metrics.put(key+" stdev", stats.getStandardDeviation());
                        
                    }
                }
            } else {
                DescriptiveStatistics stats = values.get(0);
                metrics.put(e.getKey()+" Q1", stats.getPercentile(25.0));
                metrics.put(e.getKey()+" Q2", stats.getPercentile(50.0));
                metrics.put(e.getKey()+" Q3", stats.getPercentile(75.0));
                metrics.put(e.getKey()+" Q4", stats.getPercentile(100.0));
            }
        }
        return metrics;
    }

    private static String buildKey(String key, int msLevel) {
        StringBuilder builder = new StringBuilder();
        int idx = key.indexOf("MS");
        if (idx == -1) {
            return builder.append(key).append(msLevel).toString();
        } else {
            return builder.append(key.substring(0, idx+2)).append(msLevel).append(key.substring(idx+2)).toString();
        }
    }
   
}
