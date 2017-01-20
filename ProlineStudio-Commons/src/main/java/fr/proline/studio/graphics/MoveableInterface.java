package fr.proline.studio.graphics;

/**
 *
 * @author JM235353
 */
public interface MoveableInterface {
    
    public boolean inside(int x, int y);
    public void move(int deltaX, int deltaY);
    public boolean isMoveable();
    public void snapToData();
    public void setSelected(boolean s);
    
}
