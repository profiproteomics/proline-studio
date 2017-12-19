package fr.proline.studio.graphics.measurement;

import fr.proline.studio.graphics.PlotBaseAbstract;

/**
 * Measure width between x1 and x2 interval
 * @author JM235353
 */
public class WidthMeasurement extends DeltaXMeasurement {
    
    public WidthMeasurement(PlotBaseAbstract plot) {
        super(plot);
        
        AlgorithmMeasurement widthAlgorithm = new AlgorithmMeasurement() {
            @Override
            public String getName() {
                return "Measurement Width";
            }

            @Override
            public String calculate(PlotBaseAbstract plot, double x1, double x2) {
                double width = x2 - x1;
                String res = plot.getBasePlotPanel().getXAxis().defaultFormat(width);
                return res;
            }

        };
        
        setAlgorithm(widthAlgorithm);
    }
}
