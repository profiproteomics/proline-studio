package fr.proline.studio.rserver.data;

/**
 *
 * @author JM235353
 */
public class MsnSetData extends AbstractRData {

    public MsnSetData(String name) {
        m_dataType = DataTypes.MSN_SET;
        m_name = name;
        m_longDisplayName = name;
    }
}
