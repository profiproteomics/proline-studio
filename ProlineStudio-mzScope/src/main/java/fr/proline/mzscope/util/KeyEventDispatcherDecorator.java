/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.proline.mzscope.util;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatch KeyEvents depending on the mouse position not on the component focus.Used by RawFilePlotPanel
 * 
 * @author CB205360
 */
public class KeyEventDispatcherDecorator {
  
   static {
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
      public boolean dispatchKeyEvent(KeyEvent e) {
        return processKeyEvent(e);
      }});
   }
   
   private static Logger logger = LoggerFactory.getLogger(KeyEventDispatcherDecorator.class);
   
   private static List<KeyEventDispatcher> listeners = new ArrayList<KeyEventDispatcher>();
   
   public static boolean processKeyEvent(KeyEvent evt) {
        for (KeyEventDispatcher listener : listeners) {
           if (JComponent.class.isAssignableFrom(listener.getClass())) {
              Point mouseCoords = ((JComponent)listener).getMousePosition();
              if (mouseCoords != null) {
                 listener.dispatchKeyEvent(evt);
              }
           }
        }
        return false;
   }
   
   public static boolean addKeyEventListener(KeyEventDispatcher listener) {
      if (! JComponent.class.isAssignableFrom(listener.getClass())) throw new UnsupportedOperationException("Only JComponent KeyEventDispatcher Listeners are supported");
      return listeners.add(listener);
   }

   public static boolean removeKeyEventListener(KeyEventDispatcher listener) {
      return listeners.remove(listener);
   }

}
