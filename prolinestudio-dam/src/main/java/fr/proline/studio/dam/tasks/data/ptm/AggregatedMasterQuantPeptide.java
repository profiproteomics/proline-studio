package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatedMasterQuantPeptide extends DMasterQuantPeptide {
  protected final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ptm");

  private final List<DMasterQuantPeptide> m_mqPeptides;
  private final Integer m_selectionLevel;

  public AggregatedMasterQuantPeptide(List<DMasterQuantPeptide> mqPeptides, DMasterQuantitationChannel masterQC, DMasterQuantProteinSet masterQuantProteinSet) {
    m_mqPeptides = mqPeptides;

    Map<Long,Integer> selLevelByMQPepId = new HashMap<>();
    if (masterQuantProteinSet.getMasterQuantProtSetProperties() != null && masterQuantProteinSet.getMasterQuantProtSetProperties().getMqPeptideSelLevelById() != null)
      selLevelByMQPepId = masterQuantProteinSet.getMasterQuantProtSetProperties().getMqPeptideSelLevelById();

    m_selectionLevel = computeSelectionLevel(selLevelByMQPepId);

    for (DQuantitationChannel qc : masterQC.getQuantitationChannels()) {
      List<DQuantPeptide> qPeps = new ArrayList<>();
      Map<Long,Integer> localSelLevelByPepQuant = new HashMap<>();
      Map<Long, Integer> finalSelLevelByMQPepId = selLevelByMQPepId;
      mqPeptides.forEach(mqPep -> {
        DQuantPeptide qPep = mqPep.getQuantPeptideByQchIds().get(qc.getId());
        if(qPep!=null) {
          qPep.setPeptideInstanceId(mqPep.getPeptideInstanceId());
          if(localSelLevelByPepQuant.containsKey(qPep.getPeptideInstanceId()))
            m_logger.warn("!!!! Few Aggregate Quant Pep with SAME PeptideInstanceId !!!!");
          localSelLevelByPepQuant.put(qPep.getPeptideInstanceId(), qPep.getSelectionLevel());
          if ( !finalSelLevelByMQPepId.isEmpty()) {
            //update selection level !
            if (finalSelLevelByMQPepId.containsKey(mqPep.getId())) {
              Integer mapSelLevel = finalSelLevelByMQPepId.get(mqPep.getId());
              if (!mapSelLevel.equals(qPep.getSelectionLevel()))
                localSelLevelByPepQuant.put(qPep.getPeptideInstanceId(), mapSelLevel);
            }
          }
          qPeps.add(qPep);
        }
      });

      if (!qPeps.isEmpty())
        m_quantPeptideByQchIds.put(qc.getId(), new AggregatedQuantPeptide(qPeps,localSelLevelByPepQuant));
    }
  }

  private Integer computeSelectionLevel(Map<Long,Integer> selLevelByMQPepId ){
    Integer selLevel = -1;
    for(DMasterQuantPeptide mqPep : m_mqPeptides){
      Integer mqPepSelLevel = mqPep.getSelectionLevel();
      if(selLevelByMQPepId.containsKey(mqPep.getId())) {
        mqPepSelLevel = selLevelByMQPepId.get(mqPep.getId());
      }
      if (mqPepSelLevel > selLevel)
        selLevel = mqPepSelLevel;
    }

    if (selLevel==-1)
      selLevel = 2;
    return selLevel;
  }

  public int getGenericSelectionLevel(){
    return m_selectionLevel;
  }


  public List<DMasterQuantPeptide> getAggregatedMQPeptides(){
    return m_mqPeptides;
  }

}

class AggregatedQuantPeptide extends DQuantPeptide {

  private final List<DQuantPeptide> m_quantPeptides;
  private final Map<Long,Integer> m_localSelLevelByPepQuant;

  AggregatedQuantPeptide(List<DQuantPeptide> quantPeptides, Map<Long,Integer> localSelLevelByPepQuant) {
    m_quantPeptides = quantPeptides;
    m_localSelLevelByPepQuant= localSelLevelByPepQuant;
  }

  @Override
  public Float getRawAbundance() {
    if(m_quantPeptides != null) {

      return (float) m_quantPeptides.stream().filter(qpep -> m_localSelLevelByPepQuant.get(qpep.getPeptideInstanceId()) >= 2).mapToDouble(DQuantPeptide::getRawAbundance).filter(d -> !Double.isNaN(d)).sum();
    }
    return null;
  }

  @Override
  public Float getAbundance() {
    if(m_quantPeptides != null)
      return (float)m_quantPeptides.stream().filter(qpep -> m_localSelLevelByPepQuant.get(qpep.getPeptideInstanceId()) >=2).mapToDouble(DQuantPeptide::getAbundance).filter(d -> !Double.isNaN(d)).sum();
    return null;
  }

  @Override
  public Integer getSelectionLevel() {
    return super.getSelectionLevel();
  }

  @Override
  public Integer getPeptideMatchesCount() {
    if(m_quantPeptides != null)
      return m_quantPeptides.stream().mapToInt(DQuantPeptide::getPeptideMatchesCount).sum();
    return null;
  }

  @Override
  public Long getQuantChannelId() {
    return super.getQuantChannelId();
  }

  @Override
  public Float getElutionTime() {
    return super.getElutionTime();
  }

  @Override
  public Long getPeptideId() {
    return super.getPeptideId();
  }

  @Override
  public Long getPeptideInstanceId() {
    return super.getPeptideInstanceId();
  }

  @Override
  public Integer getIdentPeptideMatchCount() {
    if(m_quantPeptides != null)
      return m_quantPeptides.stream().mapToInt(DQuantPeptide::getIdentPeptideMatchCount).sum();
    return null;
  }
}
