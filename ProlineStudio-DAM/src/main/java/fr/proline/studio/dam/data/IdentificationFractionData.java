package fr.proline.studio.dam.data;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.IdentificationFraction;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadResultSetTask;
import java.util.List;

/**
 * Correspond to an Identification Fraction in UDS DB
 *
 * @author JM235353
 */
public class IdentificationFractionData extends AbstractData {

    private IdentificationFraction identificationFraction;

    public IdentificationFractionData(IdentificationFraction identificationFraction) {
        dataType = AbstractData.DataTypes.IDENTIFICATION_FRACTION;

        this.identificationFraction = identificationFraction;

    }

    @Override
    public String getName() {
        if (identificationFraction == null) {
            return "";
        } else {
            return identificationFraction.getRawFile().getRawFileName();
        }
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadResultSetTask(callback, identificationFraction, list));

    }
}
