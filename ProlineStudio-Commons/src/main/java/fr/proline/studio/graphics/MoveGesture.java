package fr.proline.studio.graphics;

/**
 *
 * @author JM235353
 */
public class MoveGesture {
    
    private MoveableInterface m_moveableObject = null;
    private int m_x;
    private int m_y;
    
    public MoveGesture() {
        
    }
    
    public void startMoving(int x, int y, MoveableInterface moveableObject) {
        m_moveableObject = moveableObject;
        m_x = x;
        m_y = y;
    }
    
    public boolean isMoving() {
        return (m_moveableObject != null);
    }
    
    public void move(int x, int y) {
        m_moveableObject.move(x-m_x, y-m_y);
        m_x = x;
        m_y = y;
    }
    
    public void stopMoving(int x, int y, boolean isCtrlOrShiftDown) {
        move(x, y);
        m_moveableObject.snapToData(isCtrlOrShiftDown);
        m_moveableObject = null;
    }
}
