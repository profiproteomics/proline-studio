package fr.proline.studio.pattern;

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.rsmexplorer.gui.TaskDescriptionPanel;

/**
 * Management of one TaskInfo to display
 * @author JM235353
 */
public class DataBoxTaskDescription extends AbstractDataBox {

    public DataBoxTaskDescription() {

        // Name of this databox
        m_name = "Task";
        m_description = "Task Description";
        
        // Register Possible in parameters
        // One TaskInfo
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(TaskInfo.class, false);
        registerInParameter(inParameter);
    }

    @Override
    public void createPanel() {
        TaskDescriptionPanel p = new TaskDescriptionPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        TaskInfo taskInfo = (TaskInfo) m_previousDataBox.getData(false, TaskInfo.class);
        ((TaskDescriptionPanel)m_panel).setTaskInfo(taskInfo);
    }
    
        
    
}
