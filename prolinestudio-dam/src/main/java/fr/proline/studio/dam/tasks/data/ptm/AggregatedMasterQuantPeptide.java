package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;

import java.util.List;
import java.util.stream.Collectors;

public class AggregatedMasterQuantPeptide extends DMasterQuantPeptide {

  private final List<DMasterQuantPeptide> m_mqPeptides;
  private final DMasterQuantitationChannel m_masterQC;

  public AggregatedMasterQuantPeptide(List<DMasterQuantPeptide> mqPeptides, DMasterQuantitationChannel masterQC) {
    m_mqPeptides = mqPeptides;
    m_masterQC = masterQC;
    for(DQuantitationChannel qc : m_masterQC.getQuantitationChannels()) {
      List<DQuantPeptide> qPeps = mqPeptides.stream().map(mqp -> mqp.getQuantPeptideByQchIds().get(qc.getId())).filter(qp -> qp != null).collect(Collectors.toList());
      if (qPeps != null && !qPeps.isEmpty())
        m_quantPeptideByQchIds.put(qc.getId(), new AggregatedQuantPeptide(qPeps));
    }
  }
}

class AggregatedQuantPeptide extends DQuantPeptide {

  private final List<DQuantPeptide> m_quantPeptides;

  AggregatedQuantPeptide(List<DQuantPeptide> quantPeptides) {
    m_quantPeptides = quantPeptides;
  }

  @Override
  public Float getRawAbundance() {
    if(m_quantPeptides != null)
      return (float)m_quantPeptides.stream().mapToDouble(qp -> qp.getRawAbundance()).filter(d -> !Double.isNaN(d)).sum();
    return null;
  }

  @Override
  public Float getAbundance() {
    if(m_quantPeptides != null)
      return (float)m_quantPeptides.stream().mapToDouble(qp -> qp.getAbundance()).filter(d -> !Double.isNaN(d)).sum();
    return null;
  }

  @Override
  public Integer getSelectionLevel() {
    return super.getSelectionLevel();
  }

  @Override
  public Integer getPeptideMatchesCount() {
    if(m_quantPeptides != null)
      return m_quantPeptides.stream().mapToInt(qp -> qp.getPeptideMatchesCount()).sum();
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
      return m_quantPeptides.stream().mapToInt(qp -> qp.getIdentPeptideMatchCount()).sum();
    return null;
  }
}
