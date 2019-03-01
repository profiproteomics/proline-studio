package fr.proline.studio.graphics.colorpicker;

/**
 * Used to track modifications of the color selected
 * to keep synchronized the different parts of ColorPickerDialog
 * @author jm235353
 */
public interface ColorDataInterface {
    
    public void addListener(ColorDataInterface colorDataInterface);

    public void propagateColorChanged(int r, int g, int b);
    
    public int getRed();
    public int getGreen();
    public int getBlue();
}
