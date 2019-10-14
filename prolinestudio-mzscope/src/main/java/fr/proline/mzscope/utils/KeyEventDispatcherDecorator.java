/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.utils;

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
