package fr.proline.studio.rserver.command;

/**
 *
 * @author JM235353
 */
public abstract class AbstractCommand {
       
    public static final String IN_VARIABLE = "#IN_VAR#";
    public static final String OUT_VARIABLE = "#OUT_VAR#";
    public static final String FILE_ON_SERVER = "#FILE_ON_SERVER#";
    public static final String PEVIOUS_NODE = "#PREVIOUS_NODE#";
    
    public static final int CMD_GENERIC = 0;
    public static final int CMD_PLOT = 1;
    

    
    
    
    protected int m_commandType;
    protected String m_function = null;
    protected String m_nodeName = null;
    protected String m_longDisplayName = null;
    protected int m_resultType;
    
    protected AbstractCommand(int commandType) {
        m_commandType = commandType;
    }
    
    protected AbstractCommand(int commandType, int resultType, String function, String nodeName, String longDisplayName) {
        m_commandType = commandType;
        m_resultType = resultType;
        m_function = function;
        m_nodeName = nodeName;
        m_longDisplayName = longDisplayName;
    }
        
    public String getFunction() {
        return m_function;
    }
    
    public String getNodeName() {
        return m_nodeName;
    }
    
    public String getLongDisplayName(String parentNodeName) {
        return m_longDisplayName.replaceFirst(PEVIOUS_NODE, parentNodeName);
    }
    
    public int getResultType() {
        return m_resultType;
    }
    
    public abstract String getCommandExpression(RVar outVariable, RVar inVariable);
    
    
    public final static String CMD_SEPARATOR = "#|#";
    public void write(StringBuilder sb) {
        sb.append(m_commandType);
        sb.append(CMD_SEPARATOR);
        sb.append(m_function);
        sb.append(CMD_SEPARATOR);
        sb.append(m_nodeName);
        sb.append(CMD_SEPARATOR);
        sb.append(m_longDisplayName);
        sb.append(CMD_SEPARATOR);
        sb.append(m_resultType);
        sb.append(CMD_SEPARATOR);
    }
    
    public int read(String script, int indexStart) {
        
        int indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        m_function = script.substring(indexStart, indexOfSeparator);
        indexStart = indexOfSeparator+AbstractCommand.CMD_SEPARATOR.length();
        
        indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        m_nodeName = script.substring(indexStart, indexOfSeparator);
        indexStart = indexOfSeparator+AbstractCommand.CMD_SEPARATOR.length();
        
        indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        m_longDisplayName = script.substring(indexStart, indexOfSeparator);
        indexStart = indexOfSeparator+AbstractCommand.CMD_SEPARATOR.length();
        
        indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        String resultTypeS = script.substring(indexStart, indexOfSeparator);
        m_resultType = Integer.valueOf(resultTypeS);
        indexStart = indexOfSeparator+AbstractCommand.CMD_SEPARATOR.length();
        
        
        return indexStart;
    }
    
    public static int read(RScenario scenario, String script, int indexStart) {
        
        int indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        String cmdTypeS = script.substring(indexStart, indexOfSeparator);
        int cmdType = Integer.valueOf(cmdTypeS);
        indexStart = indexOfSeparator+AbstractCommand.CMD_SEPARATOR.length();
        
        AbstractCommand cmd = null;
        switch (cmdType) {
            case CMD_GENERIC:
                cmd = new GenericCommand();

                break;
            case CMD_PLOT:
                cmd = new PlotCommand();
                break;

        }
        indexStart = cmd.read(script, indexStart);
        scenario.addCommand(cmd);
        
        
        
        return indexStart;
    }
    

    
}
