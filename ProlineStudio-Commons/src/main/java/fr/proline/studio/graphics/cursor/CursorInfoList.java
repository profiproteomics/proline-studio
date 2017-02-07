package fr.proline.studio.graphics.cursor;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class CursorInfoList {
    
    private ArrayList<CursorInfo> m_cursorInfoList = new ArrayList<>();
    
    public CursorInfoList() {
        
    }
    
    public void addCursorInfo(CursorInfo cursorInfo) {
        m_cursorInfoList.add(cursorInfo);
    }
    
    public ArrayList<CursorInfo> getCursorInfoList() {
        return m_cursorInfoList;
    }
    
}
