/* 
 * Copyright (C) 2019
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
package fr.proline.studio.export;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

class TabTitleEditListener extends MouseAdapter implements MouseListener {
    private final JTextField editor = new JTextField();
    private final JTabbedPane tabbedPane;
    private int editingIdx = -1;
    private int len = -1;
    private Dimension dim;
    private CheckboxTabPanel tabComponent;
    private HashMap<String,String> m_tabTitleIdHashMap;
    CustomExportDialog m_customEd;
	private String m_formerTitle;

    public TabTitleEditListener(final JTabbedPane tabbedPane, CustomExportDialog ced) {
        super();
        m_tabTitleIdHashMap = ced.m_tabTitleIdHashMap;
        m_customEd = ced;
        this.tabbedPane = tabbedPane;
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                renameTabTitle();
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    renameTabTitle();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                } else {
                    editor.setPreferredSize(editor.getText().length() > len ? null : dim);
                    tabbedPane.revalidate();
                }
            }
        });

        
        tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
        tabbedPane.getActionMap().put("start-editing", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                startEditing();
            }
        });
    }
   
    @Override public void mouseClicked(MouseEvent me) {
    	
        Rectangle rect = tabbedPane.getUI().getTabBounds(tabbedPane, tabbedPane.getSelectedIndex());
        if (rect != null && rect.contains(me.getPoint()) && me.getClickCount() == 2) {
            startEditing();
        } else {
            renameTabTitle();
        }
    }
    private void startEditing() {
        editingIdx = tabbedPane.getSelectedIndex();
        tabComponent = (CheckboxTabPanel) tabbedPane.getTabComponentAt(editingIdx);
        tabbedPane.setTabComponentAt(editingIdx, editor);
        editor.setVisible(true);
        m_formerTitle = tabComponent.getText();
        editor.setText(m_formerTitle);
        editor.selectAll();
        editor.requestFocusInWindow();
        len = editor.getText().length();
        dim = editor.getPreferredSize();
        editor.setMinimumSize(dim);
    }
    private void cancelEditing() {
        if (editingIdx >= 0) {
            tabbedPane.setTabComponentAt(editingIdx, tabComponent);
            editor.setVisible(false);
            editingIdx = -1;
            len = -1;
            tabComponent = null;
            editor.setPreferredSize(null);
            tabbedPane.requestFocusInWindow();
        }
    }
    
    private void renameTabTitle() {
        String title = editor.getText().trim();
        boolean canConfirmTitleChange = true;
        if (editingIdx >= 0 && !title.isEmpty()) {
        	for(int i=0;i<tabbedPane.getTabCount();i++) {
        		if(editingIdx != i && ((CheckboxTabPanel)tabbedPane.getTabComponentAt(i)).getText().equals(title)) {
        			cancelEditing();
        			canConfirmTitleChange = false;
        			// can't use twice the same title
        		}
        	}
        	if(canConfirmTitleChange) {
	            ((JTextField)tabbedPane.getTabComponentAt(editingIdx)).setText(title);
                    tabComponent.setText(title);
                    tabbedPane.setTabComponentAt(editingIdx, tabComponent);
	            String sheetId = m_tabTitleIdHashMap.get(m_formerTitle);
	            m_tabTitleIdHashMap.remove(m_formerTitle);
	            m_tabTitleIdHashMap.put(title, sheetId);
	           // if(m_customEd.m_exportConfig.sheets[editingIdx].title.equals(m_formerTitle)) {
	           // 	m_customEd.m_exportConfig.sheets[editingIdx].title=title; // replace title with new one
	           // } else
	           // {
	           // 	">>ERROR: found title is not at the expected place...");
	           // }
        	} else {
        		// do not change the title
        	}
        }
         cancelEditing();
    }
}
    

