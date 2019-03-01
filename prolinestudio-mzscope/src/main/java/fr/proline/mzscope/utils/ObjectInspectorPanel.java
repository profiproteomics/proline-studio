package fr.proline.mzscope.utils;

/*
 * ObjectInspectorPanel.java
 *
 * Created on June 8, 2007, 10:48 PM
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Copyright (C) 2007 Cheok YanCheng <yccheok@yahoo.com>
 */

import com.l2fprod.common.swing.renderer.*;
import com.l2fprod.common.propertysheet.*;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import fr.profi.mzdb.model.Feature;
import java.awt.BorderLayout;

import java.beans.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yccheok
 */
public class ObjectInspectorPanel extends PropertySheetPanel {
    
    private static Logger logger = LoggerFactory.getLogger(ObjectInspectorPanel.class);    
    private Object bean;

    
    public ObjectInspectorPanel() {
        this.getTable().setEditorFactory(new PropertyEditorRegistryEx());
        PropertyRendererRegistry renderer = (PropertyRendererRegistry)this.getTable().getRendererFactory();

        //editor.registerEditor(Enum.class, new EnumComboBoxPropertyEditor());
    
        DefaultCellRenderer r = new DefaultCellRenderer();
        r.setShowOddAndEvenRows(false);    
        renderer.registerRenderer(Enum.class, r);

        this.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
        this.setToolBarVisible(false);
        this.setDescriptionVisible(false);    
        this.setToolBarVisible(true);
    }
    public ObjectInspectorPanel(Class beanClass) {
        
        BeanInfo beanInfo = null;

        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException exp) {
            logger.error("", exp);
        }
        
        this.getTable().setEditorFactory(new PropertyEditorRegistryEx());
        PropertyRendererRegistry renderer = (PropertyRendererRegistry)this.getTable().getRendererFactory();

        //editor.registerEditor(Enum.class, new EnumComboBoxPropertyEditor());
    
        DefaultCellRenderer r = new DefaultCellRenderer();
        r.setShowOddAndEvenRows(false);    
        renderer.registerRenderer(Enum.class, r);

        this.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
        this.setToolBarVisible(false);
        this.setDescriptionVisible(false);    
        this.setToolBarVisible(true);
        this.setBeanInfo(beanInfo);     
        
    }
    
    /** Creates a new instance of ObjectInspectorJPanel */
    public ObjectInspectorPanel(Object bean) {
        this.bean = bean;
        
        BeanInfo beanInfo = null;

        try {
            beanInfo = Introspector.getBeanInfo(bean.getClass());
        } catch (IntrospectionException exp) {
            logger.error("", exp);
        }
                
        this.getTable().setEditorFactory(new PropertyEditorRegistryEx());
        PropertyEditorRegistry editor = (PropertyEditorRegistry)this.getTable().getEditorFactory();
        PropertyRendererRegistry renderer = (PropertyRendererRegistry)this.getTable().getRendererFactory();

        //editor.registerEditor(Enum.class, new EnumComboBoxPropertyEditor());
    
        DefaultCellRenderer r = new DefaultCellRenderer();
        r.setShowOddAndEvenRows(false);    
        renderer.registerRenderer(Enum.class, r);

        this.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
        this.setToolBarVisible(false);
        this.setDescriptionVisible(false);    
        this.setToolBarVisible(true);
        this.setBeanInfo(beanInfo);        

        Property[] properties = this.getProperties();
        for (int i = 0, c = properties.length; i < c; i++) {
            properties[i].readFromObject(bean);            
        }
        
        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Property prop = (Property)evt.getSource();
                prop.writeToObject(ObjectInspectorPanel.this.bean);
            }
        };
        
        this.addPropertySheetChangeListener(listener);
    }
    

    private static class PropertyEditorRegistryEx extends PropertyEditorRegistry {
        // We will try to get the "nearest" super class.        
        public synchronized PropertyEditor getEditor(Class type) {
            PropertyEditor editor = super.getEditor(type);

            Class c = type;
        
            while(editor == null) {
                c = c.getSuperclass();

                if(c == null)
                    return editor;

                editor = super.getEditor(c);
            }

            return editor;
        }
    }  
    
    public void setBean(Object bean) {
        this.bean = bean;
        
        BeanInfo beanInfo = null;

        try {
            beanInfo = Introspector.getBeanInfo(bean.getClass());
        } catch (IntrospectionException exp) {
            logger.error("", exp);
        }
        
        this.setBeanInfo(beanInfo);        

        Property[] properties = this.getProperties();
        for (int i = 0, c = properties.length; i < c; i++) {
            properties[i].readFromObject(bean);            
        }
        
        //sheet.revalidate();
    }
    
    public Object getBean() {
        return bean;
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame("test");
        f.getContentPane().setLayout(new BorderLayout());
        JPanel i = new ObjectInspectorPanel(Feature.class);
        JScrollPane jScrollPane3 = new JScrollPane();
        jScrollPane3.setViewportView(i);
        
        f.getContentPane().add(jScrollPane3);
//        i.setBean("toto");
        f.setVisible(true);
        
    }
}