/*
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JM235353
 */
public class ValidationTask extends AbstractJMSTask {

  //PSM PreFilter

  public enum PSMFilter {

    RANK("PRETTY_RANK", "Pretty Rank"),
    SCORE("SCORE", "Score"),
    PEP_LENGTH("PEP_SEQ_LENGTH", "Length"),
    MASCOT_EVAL("MASCOT_EVALUE", "e-Value"),
    MASCOT_ADJUSTED_EVALUE("MASCOT_ADJUSTED_EVALUE", "Adjusted e-Value"),
    MASCOT_IT_SCORE("SCORE_IT_P-VALUE", "Identity p-Value"),
    MASCOT_HT_SCORE("SCORE_HT_P-VALUE", "Homology p-Value"),
    SINGLE_PSM_QUERY("SINGLE_PSM_PER_QUERY", "Single PSM per MS Query"),
    SINGLE_PSM_RANK("SINGLE_PSM_PER_RANK", "Single PSM per Rank"),
    SINGLE_SEQ_RANK("SINGLE_SEQ_PER_PRETTY_RANK", "Single Sequence per Pretty Rank"),
    ISOTOPE_OFFSET("ISOTOPE_OFFSET", "Isotope Offset");

    public final String key;
    public final String name;

    PSMFilter(String key, String name) {
      this.key = key;
      this.name = name;
    }
  }

  public enum ValidationParameters {

    FDR_METHOD("fdr_method", "FDR control method"),
    EXPECTED_FDR("expected_fdr", "FDR"),
    EXPECTED_FDR_PARAM("expected_fdr_parameter", "FDR Variable"),
    PROTEIN_EXPECTED_FDR("protein_expected_fdr", "Protein FDR"),
    PEPTIDE_EXPECTED_FDR("peptide_expected_fdr", "Peptide FDR"),
    TD_ANALYZER("td_analyzer", "Target/Decoy method");

    public final String key;
    public final String name;

    ValidationParameters(String key, String name) {
      this.key = key;
      this.name = name;
    }
  }

  private final DDataset m_dataset;
  private final String m_description;  //Not used on server side
  private final HashMap<String, String> m_argumentsMap;
  private final String m_scoringType;
  private final Integer[] m_resultSummaryId;
  private Map<Long, Long> m_rsmIdsPerRsIds = null;
  private final String m_version = "3.0";

  public ValidationTask(AbstractJMSCallback callback, DDataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId, String scoringType) {
    super(callback, new TaskInfo("JMS Validation of Search Result " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
    m_dataset = dataset;
    m_description = description;
    m_argumentsMap = argumentsMap;
    m_resultSummaryId = resultSummaryId;
    m_scoringType = scoringType;
  }

  public ValidationTask(AbstractJMSCallback callback, DDataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId, HashMap<Long, Long> rsmIdsPerRsIds, String scoringType) {
    super(callback, new TaskInfo("JMS Validation of Search Result " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
    m_dataset = dataset;
    m_description = description;
    m_argumentsMap = argumentsMap;
    m_rsmIdsPerRsIds = rsmIdsPerRsIds;
    m_resultSummaryId = resultSummaryId;
    m_scoringType = scoringType;
  }

  @Override
  public void taskRun() throws JMSException {
    final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
    jsonRequest.setNamedParams(createParams());

    final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

    /* ReplyTo = Temporary Destination Queue for Server -> Client response */
    message.setJMSReplyTo(m_replyQueue);
    message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ValidateResultSet");
    message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
    addSupplementaryInfo(message);

    setTaskInfoRequest(message.getText());

    // Step 8. Send the Message
    m_producer.send(message);
    m_loggerProline.info("ValidationTask Message [{}] sent", message.getJMSMessageID());
    m_taskInfo.setJmsMessageID(message.getJMSMessageID());
  }

  @Override
  public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

    final Object result = jsonResponse.getResult();

    if (result == null || !Map.class.isInstance(result)) {
      m_loggerProline.debug("Invalid or no result");
      throw new Exception("null or invalid result " + result);
    } else {
      m_loggerProline.debug("Result :\n" + result);
      Long rsmId = (Long) ((Map) result).get(m_dataset.getResultSetId().toString());
      m_resultSummaryId[0] = rsmId.intValue();
      if (m_rsmIdsPerRsIds != null) {
        ((Map<String, Long>) result).forEach((String key, Long value) -> {
          m_rsmIdsPerRsIds.put(Long.parseLong(key), value);
        });
      }
    }
    /*
     * TODO Use JSON-RPC Response
     */
    //traceJSONResponse(jsonString);
  }


  private HashMap<String, Object> createParams() {

    HashMap<String, Object> params = new HashMap<>();
    params.put("project_id", m_dataset.getProject().getId());
    params.put("result_set_id", m_dataset.getResultSetId());
    params.put("description", m_description); //JPM.TODO : string is ""

    // PSM Pre-PSMFilter
    ArrayList<Map<String, Object>> psmFilters = new ArrayList<>();

    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.RANK.key)) {
      Map<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.RANK.key);
      filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.RANK.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.SCORE.key)) {
      Map<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.SCORE.key);
      filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.SCORE.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.MASCOT_EVAL.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.MASCOT_EVAL.key);
      filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.MASCOT_EVAL.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.MASCOT_ADJUSTED_EVALUE.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.MASCOT_ADJUSTED_EVALUE.key);
      filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.MASCOT_ADJUSTED_EVALUE.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.PEP_LENGTH.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.PEP_LENGTH.key);
      filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.PEP_LENGTH.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.MASCOT_IT_SCORE.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.MASCOT_IT_SCORE.key);
      filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.MASCOT_IT_SCORE.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.MASCOT_HT_SCORE.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.MASCOT_HT_SCORE.key);
      filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.MASCOT_HT_SCORE.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.SINGLE_PSM_QUERY.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.SINGLE_PSM_QUERY.key);
      filterCfg.put("threshold", 1);
      filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.SINGLE_PSM_QUERY.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.SINGLE_PSM_RANK.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.SINGLE_PSM_RANK.key);
      filterCfg.put("threshold", 1);
      filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.SINGLE_PSM_RANK.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.SINGLE_SEQ_RANK.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.SINGLE_SEQ_RANK.key);
      filterCfg.put("threshold", 1);
//      filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.SINGLE_PSM_RANK.key)));
      psmFilters.add(filterCfg);
    }
    if (m_argumentsMap.containsKey("PSM_" + PSMFilter.ISOTOPE_OFFSET.key)) {
      HashMap<String, Object> filterCfg = new HashMap<>();
      filterCfg.put("parameter", PSMFilter.ISOTOPE_OFFSET.key);
      filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_" + PSMFilter.ISOTOPE_OFFSET.key)));
      psmFilters.add(filterCfg);
    }

    params.put("pep_match_filters", psmFilters);

    // PSM Validator
    if (m_argumentsMap.containsKey(ValidationParameters.EXPECTED_FDR.key)) {
      HashMap<String, Object> pepMatchValidator = new HashMap<>();
      pepMatchValidator.put("parameter", m_argumentsMap.get(ValidationParameters.EXPECTED_FDR_PARAM.key));
      pepMatchValidator.put("expected_fdr", m_argumentsMap.get(ValidationParameters.EXPECTED_FDR.key));
      params.put("pep_match_validator_config", pepMatchValidator);
    }

    HashMap<String, Object> fdrConfig = new HashMap<>();
    if (m_argumentsMap.containsKey(ValidationParameters.EXPECTED_FDR.key) ||
            m_argumentsMap.containsKey(ValidationParameters.PEPTIDE_EXPECTED_FDR.key) ||
            m_argumentsMap.containsKey(ValidationParameters.PROTEIN_EXPECTED_FDR.key)) {

      if ((m_argumentsMap.containsKey(ValidationParameters.FDR_METHOD.key))) {
        fdrConfig.put("method_name", (m_argumentsMap.get(ValidationParameters.FDR_METHOD.key)));

        if (m_argumentsMap.get(ValidationParameters.FDR_METHOD.key).equals("TARGET_DECOY")) {
          // force basic tdAnalyzer usage
          HashMap<String, Object> tdAnalyzerConfig = new HashMap<>();
          tdAnalyzerConfig.put("method_name", "BASIC");
          fdrConfig.put("td_analyzer_config", tdAnalyzerConfig);
        }

      }

//          if (m_argumentsMap.containsKey(ValidationParameters.TD_ANALYZER.key)) {
//              HashMap tdAnalyzerConfig = new HashMap();
//              tdAnalyzerConfig.put("method_name", m_argumentsMap.get(ValidationParameters.TD_ANALYZER.key));
//
//              if (m_argumentsMap.containsKey("db_ratio")) {
//                  HashMap tdAnalyzerParams = new HashMap();
//                  tdAnalyzerParams.put("ratio", m_argumentsMap.get("db_ratio"));            
//                  tdAnalyzerConfig.put("params", tdAnalyzerParams);
//              }
//              fdrConfig.put("td_analyzer_config", tdAnalyzerConfig);            
//          }

      params.put("fdr_analyzer_config", fdrConfig);
    }

    // Peptide validator
    if (m_argumentsMap.containsKey(ValidationParameters.PEPTIDE_EXPECTED_FDR.key)) {
      HashMap<String, Object> peptideValidatorConfig = new HashMap<>();
      peptideValidatorConfig.put("parameter", "BH");
      peptideValidatorConfig.put("expected_fdr", m_argumentsMap.get(ValidationParameters.PEPTIDE_EXPECTED_FDR.key));
      params.put("pep_validator_config", peptideValidatorConfig);
    }

    params.put("pep_set_score_type", m_scoringType);

    // Protein Pre-Filters
    ArrayList<Map<String, Object>> proteinFilters = new ArrayList<>();

    for (FilterProteinSetsTask.Filter filter : FilterProteinSetsTask.Filter.values()) {
      String filterKeyOfInMap = "PROT_" + filter.key;
      if (m_argumentsMap.containsKey(filterKeyOfInMap)) {
        HashMap<String, Object> filterCfg = new HashMap<>();
        filterCfg.put("parameter", filter.key);
        if (filter == FilterProteinSetsTask.Filter.SCORE) {
          filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get(filterKeyOfInMap)));
        } else {
          filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(filterKeyOfInMap)));
        }
        proteinFilters.add(filterCfg);
      }
    }

    params.put("prot_set_filters", proteinFilters);


    // protein parameters
    if (m_argumentsMap.containsKey(ValidationParameters.PROTEIN_EXPECTED_FDR.key)) {
      HashMap<String, Object> protSetValidator = new HashMap<>();
      protSetValidator.put("parameter", "SCORE");
      protSetValidator.put("expected_fdr", m_argumentsMap.get(ValidationParameters.PROTEIN_EXPECTED_FDR.key));
      protSetValidator.put("validation_method", "PROTEIN_SET_RULES");
      params.put("prot_set_validator_config", protSetValidator);
      // Verify that a td_analyzer_config have been supplied at PSM level, if not set it to BASIC
      if (!fdrConfig.containsKey("td_analyzer_config")) {
        HashMap<String, Object> tdAnalyzerConfig = new HashMap<>();
        tdAnalyzerConfig.put("method_name", "BASIC");
        fdrConfig.put("td_analyzer_config", tdAnalyzerConfig);
      }
    }

    if (m_argumentsMap.containsKey("propagate_prot_set_filters")) {
      params.put("propagate_prot_set_filters", Boolean.parseBoolean(m_argumentsMap.get("propagate_prot_set_filters")));
    }

    if (m_argumentsMap.containsKey("propagate_pep_match_filters")) {
      params.put("propagate_pep_match_filters", Boolean.parseBoolean(m_argumentsMap.get("propagate_pep_match_filters")));
    }

    return params;
  }

}
