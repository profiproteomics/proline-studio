package fr.proline.studio.types;

/**
 * Used to know if a quantitation table model corresponds to Xic or Spectral Count
 * 
 * @author JM235353
 */
public class XicMode  {
    
    private final boolean m_b;
    
    public XicMode(boolean b) {
        m_b = b;
    }
    
    public boolean isXicMode() {
        return m_b;
    }
}
