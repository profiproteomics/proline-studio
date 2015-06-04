package fr.proline.studio.pattern;

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.rsmexplorer.gui.TasksPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * Management of Task Logs
 * @author JM235353
 */
public class DataBoxTaskList extends AbstractDataBox {

    private static final int UPDATE_DELAY = 1000;
    
    private Timer m_updateTimer = null;
    
    public DataBoxTaskList() {
        super(DataboxType.DataBoxTaskList);
        
        // Name of this databox
        m_typeName = "Tasks Log";

        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(TaskInfo.class, false);
        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        TasksPanel p = new TasksPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    
    }

    @Override
    public void dataChanged() {
        // never called
    }
    
        @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(TaskInfo.class)) {
                return ((TasksPanel)m_panel).getSelectedTaskInfo();
            }

        }
        return super.getData(getArray, parameterType);
    }
    
    @Override
    public void setEntryData(Object data) {
        // never called
    }

    @Override
    public void windowClosed() {
         m_updateTimer.stop();
         super.windowClosed();
    }
    
    @Override
    public void windowOpened() {    
        if (m_updateTimer == null) {
            ActionListener taskPerformer = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    ((TasksPanel)m_panel).updateData();
                }
            };
            m_updateTimer = new Timer(UPDATE_DELAY, taskPerformer);

        }
        
        m_updateTimer.start();

    }


    
}
