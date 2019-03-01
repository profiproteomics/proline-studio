/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.utils;

import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;

/**
 *
 * @author CB205360
 */
public interface IPopupMenuDelegate {
   
   public void initPopupMenu(JPopupMenu popupMenu);
   
   public void updatePopupMenu();
   
   public ActionListener getDefaultAction();
   
}
