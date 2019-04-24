/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 16 avr. 2019
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic.aggregation;

import fr.proline.core.orm.uds.QuantitationChannel;
import fr.proline.core.orm.uds.dto.DDataset;
import java.util.HashMap;
import java.util.Map;

/**
 * for 1 specfic Quantitation Channel in Aggregation, we have a map of (Starting
 * Quntitation - Starting Quantitation channel);
 *
 * @author CB205360
 */
public class DQuantitationChannelMapping {

    /**
     * for one =Quantitation Channel in Aggregation Tree
     */
    private Integer parentQCNumber;
    /**
     * DDataSet = 1 Quantitation Map Quanti DataSet-Quanti channel
     */
    private Map<DDataset, QuantitationChannel> mappedQuantChannels;

    public DQuantitationChannelMapping(Integer parentQCNumber) {
        this.parentQCNumber = parentQCNumber;
        this.mappedQuantChannels = new HashMap<>();
    }

    public Integer getParentQCNumber() {
        return parentQCNumber;
    }

    public Map<DDataset, QuantitationChannel> getMappedQuantChannels() {
        return mappedQuantChannels;
    }

    public QuantitationChannel getQuantChannel(DDataset Quanti) {
        return mappedQuantChannels.get(Quanti);
    }

    public void put(DDataset ds, QuantitationChannel childQC) {
        mappedQuantChannels.put(ds, childQC);
    }

    public void put(DDataset ds, int qcNumber) {
        QuantitationChannel childQC = ds.getMasterQuantitationChannels().get(0).getQuantitationChannels().stream().filter(qc -> qc.getNumber() == qcNumber).findFirst().orElse(null);
        if (childQC != null) {
            mappedQuantChannels.put(ds, childQC);
        }
    }

    void remove(DDataset quanti) {
        mappedQuantChannels.remove(quanti);

    }

    public DQuantitationChannelMapping clone() {
        DQuantitationChannelMapping copy = new DQuantitationChannelMapping(this.parentQCNumber);
        for (DDataset quanti : mappedQuantChannels.keySet()) {
            copy.mappedQuantChannels.put(quanti, this.mappedQuantChannels.get(quanti));
        }
        return copy;

    }

    @Override
    public String toString() {
        String result = this.parentQCNumber + " ";
        for (DDataset quanti : this.mappedQuantChannels.keySet()) {
            QuantitationChannel channel = this.mappedQuantChannels.get(quanti);
            result += "{" + quanti.getName() + ",(id=" + channel.getId()+",name="+channel.getName() + ")}";
        }
        return result;
    }
}
