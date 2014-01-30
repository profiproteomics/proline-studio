package fr.proline.studio.rserver.command;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.rserver.RServerManager;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author JM235353
 */
public class CommandTask extends AbstractServiceTask {


        private RVar m_inVar;
        private RVar m_outVar;
        private AbstractCommand m_command;

        public CommandTask(AbstractServiceCallback callback, RVar outVar, RVar inVar, AbstractCommand command) {
            super(callback, true /** synchronous */, new TaskInfo(command.getNodeName(), false, TASK_LIST_INFO));

            m_inVar = inVar;
            m_outVar = outVar;
            m_command = command;
        }

        @Override
        public boolean askService() {

            RServerManager serverR = RServerManager.getRServerManager();

            try {

                
                String var = null;
                
                int resultType = m_command.getResultType();
                switch (resultType) {
                    case RVar.MSN_SET:
                        var = serverR.getNewVariableName("msnSet");
                        break;
                    case RVar.GRAPHIC:
                        var = serverR.getNewVariableName("image");
                        break;
                }

                m_outVar.setVar(var, resultType);
                String code = m_command.getCommandExpression(m_outVar, m_inVar);
                
                /*String codeTEST = "GetGraphicsAsPng('test_boxplot.png', boxPlotEDyP("+m_inVar.getVar()+", 'testBoxplot'), 500, 500)";
                
                serverR.eval(codeTEST);*/
                
                if (resultType == RVar.GRAPHIC) {

                    String fileOnServer = null;
                    File boxPlotTempFile = null;
                    try {
                        serverR.parseAndEval(code);
                        fileOnServer = ((PlotCommand) m_command).getFileNameOnServer();
                        
                        // download box plot png file
                        
                        boxPlotTempFile = File.createTempFile("boxPlot", ".png");
                        boxPlotTempFile.deleteOnExit();
                        serverR.downloadFile(fileOnServer, boxPlotTempFile.getAbsolutePath());

                        // Create the image
                        m_outVar.setAttachedData(ImageIO.read(boxPlotTempFile));
                        
                    } finally {
                        // remove files
                        try {
                            if (fileOnServer != null) {
                                serverR.deleteFile(fileOnServer);
                            }
                        } catch (Exception ex) {
                        }
                        
                        if (boxPlotTempFile!= null) {
                            boxPlotTempFile.delete();
                        }
                    }
                } else {
                    serverR.parseAndEval(code);
                }
                
            } catch (RServerManager.RServerException | java.io.IOException ex) {
                m_taskError = new TaskError(ex);
                return false;
            }


            return true;
        }

        @Override
        public AbstractServiceTask.ServiceState getServiceState() {
            // always returns STATE_DONE because it is a synchronous service
            return AbstractServiceTask.ServiceState.STATE_DONE;
        }
}
