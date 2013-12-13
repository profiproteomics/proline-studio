/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.markerbar;

/**
 *
 * @author JM235353
 */
public class AnnotationMarker extends DefaultMarker {
    
    private String text = null;
    
    public AnnotationMarker(int row, String text) {
        super(row, ANNOTATION_MARKER);
        
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
