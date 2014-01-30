package fr.proline.studio.rserver.command;

/**
 *
 * @author JM235353
 */
public class GenericCommand extends AbstractCommand {

    public String[] m_parameters = null;

    public GenericCommand() {
        super(CMD_GENERIC);
    }

    public GenericCommand(String function, String nodeName, String longDisplayName, String[] parameters, int resultType) {
        super(CMD_GENERIC, resultType, function, nodeName, longDisplayName);
        m_parameters = parameters;
    }

    @Override
    public String getCommandExpression(RVar outVariable, RVar inVariable) {

        // check if OUT_VARIABLE is a parameter
        boolean outVarIsAParameter = false;
        int nbParameters = m_parameters.length;
        for (int i = 0; i < nbParameters; i++) {
            String parameter = m_parameters[i];
            if (parameter.compareTo(OUT_VARIABLE) == 0) {
                outVarIsAParameter = true;
                break;
            }
        }


        StringBuilder sb = new StringBuilder();
        if (!outVarIsAParameter) {
            sb.append(outVariable);
            sb.append(" <- ");
        }
        sb.append(m_function);
        sb.append('(');

        for (int i = 0; i < nbParameters; i++) {

            String parameter = m_parameters[i];
            if ((parameter.compareTo(IN_VARIABLE) == 0) || (parameter.compareTo(OUT_VARIABLE) == 0)) {
                sb.append(inVariable);
            } else {
                sb.append(parameter);
            }


            if (i < nbParameters - 1) {
                sb.append(',');
            }
        }
        sb.append(')');

        return sb.toString();
    }

    @Override
    public void write(StringBuilder sb) {
        super.write(sb);

        int nbParameters = m_parameters.length;
        sb.append(nbParameters);
        sb.append(CMD_SEPARATOR);
        for (int i = 0; i < nbParameters; i++) {
            sb.append(m_parameters[i]);
            sb.append(CMD_SEPARATOR);
        }
    }

    @Override
    public int read(String script, int indexStart) {

        indexStart = super.read(script, indexStart);

        int indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
        String nbParametersS = script.substring(indexStart, indexOfSeparator);
        indexStart = indexOfSeparator + AbstractCommand.CMD_SEPARATOR.length();
        int nbParameters = Integer.valueOf(nbParametersS);
        m_parameters = new String[nbParameters];


        for (int i = 0; i < nbParameters; i++) {
            indexOfSeparator = script.indexOf(AbstractCommand.CMD_SEPARATOR, indexStart);
            m_parameters[i] = script.substring(indexStart, indexOfSeparator);
            indexStart = indexOfSeparator + AbstractCommand.CMD_SEPARATOR.length();
        }

        return indexStart;
    }
}
