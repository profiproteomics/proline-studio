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
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task to import identifications from files
 *
 * @author VD225637
 */
public class ImportIdentificationTask extends AbstractJMSTask {

    private final String m_parserId;
    private final HashMap<String, String> m_parserArguments;
    private final String m_filePath;
    private final String m_decoyRegex;
    private final long m_instrumentId;
    private final long m_peaklistSoftwareId;
    private final long m_projectId;
    private final long m_fragmentRuleSetId;
    private final boolean m_saveSpectrumMatches;
    private final Long[] m_resultSetId;

    public ImportIdentificationTask(AbstractJMSCallback callback, String parserId, HashMap<String, String> parserArguments, String filePath, String decoyRegex, long instrumentId, long peaklistSoftwareId, boolean saveSpectrumMatches, long fragmRuleSetId, long projectId, Long[] resultSetId) {
        super(callback, new TaskInfo("Import Identification " + filePath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

        m_parserId = parserId;
        m_parserArguments = parserArguments;
        m_filePath = filePath;
        m_decoyRegex = decoyRegex;
        m_instrumentId = instrumentId;
        m_peaklistSoftwareId = peaklistSoftwareId;
        m_saveSpectrumMatches = saveSpectrumMatches;
        m_projectId = projectId;
        m_resultSetId = resultSetId;
        m_fragmentRuleSetId = fragmRuleSetId;
    }

    @Override
    public void taskRun() throws JMSException {

        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        //SERVICE - ImportValidateGenerateSpectrumMatches service TEST
//        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ImportValidateGenerateSM");
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ImportResultFiles");
        addSupplementaryInfo(message, m_projectId);

        setTaskInfoRequest(message.getText());
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("ImportIdentificationTask Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

  /*
   *
 * Input params : TO BE VERIFIED, since test was old !
 *  GLobal
 *   project_id : The id of the project used for data importation.
 *  Import Specific
 *   use_decoy_regexp: true if result file is formated with decoy strategy RegExp, false if it is formated with the id of the rule to be used.
 *   result_files : The list of the result files to be imported, as IResultFileDescriptor object (format, path, peaklist_id (optionnal)) + protMatchDecoyRuleId or + decoyStrategy
 *   instrument_config_id : id in datastore of the instrument config used for result file acquisition
 *   peaklist_software_id : id in datastore of the software use to generate peaklist
 *   importer_properties : Map of properties for importer, specific to result files format
 * Validate Specific
 *   pep_match_filters : List of PSM filters to use (parameter, threshold and post_validation)
 *   pep_match_validator_config : PSM validation configuration (as PepMatchValidatorConfig : parameter, threshold, expectedFdr)
 *   pep_set_score_type : PeptideSet Scoring to use, one of PepSetScoring (mascot:standard score, mascot:modified mudpit score)
 *   prot_set_filters : List of ProteinSet filters to use (parameter, threshold)
 *   prot_set_validator_config : ProteinSet validation configuration  (as ProtSetValidatorConfig : validation_method, parameter, thresholds, expectedFdr)
 *Generate Spectrum Match specific
 *   generate_spectrum_matches : If true, generate fragment matches of MS/MS spectra for validated PSM.
 *   force_insert : Specify if existing spectrum matches should be replaced
*/


    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_projectId);
//        params.put("use_decoy_regexp", true); //For ImportValidateGenerateSpectrumMatches test
        List<Map<String, Object> > args = new ArrayList<>();

        // add the file to parse
        Map<String, Object> resultfile = new HashMap<>();
        resultfile.put("path", m_filePath);  // files must be accessible from web-core by the same path
        resultfile.put("format", m_parserId);
        if (m_decoyRegex != null) {
            resultfile.put("decoy_strategy", m_decoyRegex);
        }
        args.add(resultfile);
        params.put("result_files", args);

        params.put("instrument_config_id", m_instrumentId);
        params.put("peaklist_software_id", m_peaklistSoftwareId);
        if(m_fragmentRuleSetId >0)
            params.put("fragmentation_rule_set_id",m_fragmentRuleSetId);
        params.put("save_spectrum_matches", m_saveSpectrumMatches);

        // parser arguments
        params.put("importer_properties", m_parserArguments);

//        //VDS TEST : ADD Validation properties !//For ImportValidateGenerateSpectrumMatches test
//        ArrayList pepFilters = new ArrayList();
//        HashMap filterCfg = new HashMap();
//        filterCfg.put("parameter", RANK_FILTER_KEY);
//        filterCfg.put("threshold", 2);
//        pepFilters.add(filterCfg);
//        params.put("pep_match_filters", pepFilters);
//        params.put("pep_set_score_type", "mascot:standard score");
//        ArrayList proteinFilters = new ArrayList();
//        params.put("prot_set_filters", proteinFilters);
//        params.put("generate_spectrum_matches", true);
//
//
        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if ((result == null) || (!ArrayList.class.isInstance(result))) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
            throw new Exception("Invalid result " + result);
        }

        HashMap returnedValuesMap = (HashMap) ((ArrayList) result).get(0);

        // retrieve resultSet id
        Long resultSetIdBD = (Long) returnedValuesMap.get("target_result_set_id");
        if (resultSetIdBD == null) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
            throw new Exception("Import result error : No returned ResultSet Id");
        }

        m_resultSetId[0] = resultSetIdBD;
    }

}
