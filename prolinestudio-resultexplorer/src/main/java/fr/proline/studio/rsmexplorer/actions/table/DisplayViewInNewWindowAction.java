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
package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxFrozenCopy;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 *
 * @author JM235353
 */
public class DisplayViewInNewWindowAction extends AbstractAction {
    
    private AbstractDataBox m_sourceBox;
    private AbstractDataBox m_destinationBox;


    public DisplayViewInNewWindowAction(AbstractDataBox sourceBox, AbstractDataBox destinationBox, String description) {
        super(description);
        
        Image img = destinationBox.getDefaultIcon();
        if (img != null) {
            putValue(Action.SMALL_ICON, new ImageIcon(img));
        }
        
        m_sourceBox = sourceBox;
        try {
            m_destinationBox = destinationBox.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // should never happen
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        DataBoxFrozenCopy dataBoxFrozenCopy = new DataBoxFrozenCopy(m_sourceBox);

        dataBoxFrozenCopy.addNextDataBox(m_destinationBox);


        String dataName = m_sourceBox.getImportantOutParameterValue();
        if ((dataName !=null) && (dataName.length()>12)) {
            dataName = dataName.substring(0,10)+"...";
        }

        final WindowBox wbox = WindowBoxFactory.getDetailWindowBox(dataName, dataName+": "+m_destinationBox.getDescription(), m_destinationBox);

        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();
        
        Class[] classes = m_sourceBox.getImportantInParameterClass();
        for (int i=0;i<classes.length;i++) {
            dataBoxFrozenCopy.addDataChanged(classes[i]);
        }
        dataBoxFrozenCopy.propagateDataChanged();



    }

    
}
