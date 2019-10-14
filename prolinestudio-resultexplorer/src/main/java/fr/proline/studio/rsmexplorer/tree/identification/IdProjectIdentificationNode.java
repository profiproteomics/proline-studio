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
package fr.proline.studio.rsmexplorer.tree.identification;

import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProjectTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.utils.IconManager;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Node.Property;


/**
 * Node for the opened Projects
 * @author JM235353
 */
public class IdProjectIdentificationNode extends AbstractNode {

    
    public IdProjectIdentificationNode(AbstractData data) {
        super(AbstractNode.NodeTypes.PROJECT_IDENTIFICATION, data);
    }


    public Project getProject() {
        return ((ProjectIdentificationData) getData()).getProject();
    }
    
    @Override
    public ImageIcon getIcon(boolean expanded) {
        return getIcon(IconManager.IconType.IDENTIFICATION);
    }

    @Override
    public String toString() {
        return "Identifications";
    }
    
    public void changeNameAndDescription(final String newName, final String newDescription) {
        
        final Project project = getProject();
        final String name = project.getName();
        String description = project.getDescription();
        
        if (((newName != null) && (newName.compareTo(name) != 0)) || ((newDescription != null) && (newDescription.compareTo(description) != 0))) {
            setIsChanging(true);
            project.setName(newName + "...");
            ((DefaultTreeModel) IdentificationTree.getCurrentTree().getModel()).nodeChanged(this);

            final IdProjectIdentificationNode projectNode = this;
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        setIsChanging(false);
                        project.setName(newName);
                        project.setDescription(newDescription);
                        ((DefaultTreeModel) IdentificationTree.getCurrentTree().getModel()).nodeChanged(projectNode);
                    }else{
                        setIsChanging(false);
                        project.setName(name);
                        ((DefaultTreeModel) IdentificationTree.getCurrentTree().getModel()).nodeChanged(projectNode);
                    }
                }
            };


            // ask asynchronous loading of data
            DatabaseProjectTask task = new DatabaseProjectTask(callback);
            task.initChangeSettingsOfProject(project, newName, newDescription, null);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }

    @Override
    public void loadDataForProperties(Runnable callback) {
        // nothing to do
        callback.run();
    }
    
    @Override
    public Sheet createSheet() {
        Project p = getProject();
        
        Sheet sheet = Sheet.createDefault();
        
        try {

            Sheet.Set propGroup = Sheet.createPropertiesSet();
            
         
            Property prop = new PropertySupport.Reflection<>(p, Long.class, "getId", null);
            prop.setName("id");
            propGroup.put(prop);
            
            prop = new PropertySupport.Reflection<>(p, String.class, "getName", null);
            prop.setName("name");
            propGroup.put(prop);
            
            prop = new PropertySupport.Reflection<>(p, String.class, "getDescription", null);
            prop.setName("description");
            propGroup.put(prop);
            
            sheet.put(propGroup);

        } catch (NoSuchMethodException e) {
            m_logger.error(getClass().getSimpleName() + " properties error ", e);
        }
        
        return sheet;
    }
    
    @Override
    public AbstractNode copyNode() {
        AbstractNode copy = new IdProjectIdentificationNode(getData());
        copyChildren(copy);
        return copy;
    }
    
}
