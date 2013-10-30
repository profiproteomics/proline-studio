package fr.proline.studio.stats;

/**
 *
 * @author JM235353
 */
public abstract class ValuesForStatsAbstract {

    public abstract double getValue(int i);
    
    public abstract int size();
    
    public double sum() {
        
        double s = 0;
        int nb = size();
        for (int i=0;i<nb;i++) {
            s += getValue(i);
        }
        
        return s;
    }
    
    public double mean() {
        return sum()/size();
    }
    
    public double variance() {
        
        double v = 0;
        double mean = mean();
       int nb = size();
        for (int i=0;i<nb;i++) {
             double diff = getValue(i)-mean;
             v += diff*diff;
        }
        return v/nb;
    }
    
    public double standardDeviation() {
        return Math.sqrt(variance());
    }
}
