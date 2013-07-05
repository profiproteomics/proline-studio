package fr.proline.studio.markerbar;

/**
 * Marker with a Text
 * @author JM235353
 */
public class AnnotationMarker extends DefaultMarker {
    
    private String m_text = null;
    
    public AnnotationMarker(int row, String text) {
        super(row, ANNOTATION_MARKER);
        
        m_text = text;
    }
    
    public String getText() {
        return m_text;
    }
    
    public void setText(String text) {
        this.m_text = text;
    }
}
