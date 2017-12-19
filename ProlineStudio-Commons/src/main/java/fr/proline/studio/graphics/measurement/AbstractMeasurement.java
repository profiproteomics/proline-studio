package fr.proline.studio.graphics.measurement;

/**
 * Base Class for all measurements
 * @author JM235353
 */
public abstract class AbstractMeasurement {
    
    public enum MeasurementType {
        X_AXIS_POPUP, // Measurement through X Axis Popup
        Y_AXIS_POPUP, // Measurement through Y Axis Popup
        PLOT_POPUP    // Measurement through Plot Zone Popup
    }
    
    public abstract String getName();
    public abstract MeasurementType getMeasurementType();
    public abstract void applyMeasurement(int x, int y);
    public abstract boolean canApply();
}
