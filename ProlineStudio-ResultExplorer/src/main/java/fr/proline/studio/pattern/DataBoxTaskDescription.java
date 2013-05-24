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
        name = "Task Description";
        
        // Register Possible in parameters
        // One TaskInfo
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(TaskInfo.class, false);
        registerInParameter(inParameter);
    }

    @Override
    public void createPanel() {
        TaskDescriptionPanel p = new TaskDescriptionPanel();
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged(Class dataType) {
        TaskInfo taskInfo = (TaskInfo) previousDataBox.getData(false, TaskInfo.class);
        ((TaskDescriptionPanel)m_panel).setTaskInfo(taskInfo);
    }
    
        
    
}
