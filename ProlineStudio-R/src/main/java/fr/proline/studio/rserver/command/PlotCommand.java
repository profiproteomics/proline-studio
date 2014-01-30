package fr.proline.studio.rserver.command;

/**
 *
 * @author JM235353
 */
public class PlotCommand extends AbstractCommand {

    private String m_plotRExpression;
    private String m_fileNameOnServer;

    public PlotCommand() {
        super(CMD_PLOT);
    }

    public PlotCommand(String function, String nodeName, String longDisplayName, String plotRExpression) {
        super(CMD_PLOT, RVar.GRAPHIC, function, nodeName, longDisplayName);

        m_plotRExpression = plotRExpression;
       
    }

    @Override
    public String getCommandExpression(RVar outVariable, RVar inVariable) {

        String timestamp = String.valueOf(System.currentTimeMillis());
        m_fileNameOnServer = "image"+timestamp+".png";

        return m_plotRExpression.replaceFirst(IN_VARIABLE, inVariable.getVar()).replaceFirst(FILE_ON_SERVER, m_fileNameOnServer).replaceFirst(PREVIOUS_LONG_DISPLAY_NAME, inVariable.getFullDisplay());

    }

    public String getFileNameOnServer() {
        return m_fileNameOnServer;
    }

    @Override
    public void write(StringBuilder sb) {
        super.write(sb);

        sb.append(m_fileNameOnServer);
        sb.append(CMD_SEPARATOR);
        sb.append(m_plotRExpression);
        sb.append(CMD_SEPARATOR);
    }

    @Override
    public int read(String script, int indexStart) {

        indexStart = super.read(script, indexStart);

        int indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        m_fileNameOnServer = script.substring(indexStart, indexOfSeparator);
        indexStart = indexOfSeparator + AbstractCommand.CMD_SEPARATOR.length();

        indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        m_plotRExpression = script.substring(indexStart, indexOfSeparator);
        indexStart = indexOfSeparator + AbstractCommand.CMD_SEPARATOR.length();

        return indexStart;
    }
    
}
