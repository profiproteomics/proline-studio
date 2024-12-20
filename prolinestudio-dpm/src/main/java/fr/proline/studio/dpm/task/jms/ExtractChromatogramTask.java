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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.lcms.Peak;
import fr.proline.core.orm.lcms.dto.DFeature;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Get full XIC Chromatogram for a m/z with tolerance ppm Task JMS
 */
public class ExtractChromatogramTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/msq/ExtractChromatogram";
    private final String m_version = "1.0";



    private final ArrayList<DFeature> m_features;
    private final ArrayList<String> m_rawFileIdentifierList;
    private final ArrayList<Double> m_mzList;
    private final double m_ppm;


    public ExtractChromatogramTask(AbstractJMSCallback callback, ArrayList<DFeature> features, ArrayList<String> rawFileIdentifierList, ArrayList<Double> mzList, double ppm) {
        super(callback, new TaskInfo("Extract Chromatograms", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_features = features;
        m_rawFileIdentifierList = rawFileIdentifierList;
        m_mzList = mzList;
        m_ppm = ppm;
    }


    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        addSupplementaryInfo(message);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        //-- Global PARAMS

        params.put("raw_file_identifier", m_rawFileIdentifierList);
        params.put("mz", m_mzList);
        params.put("ppm", m_ppm);

        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {
        final Object result = jsonResponse.getResult();
        if (result == null || !String.class.isInstance(result)) {
            m_loggerProline.debug("Invalid result: No Chromatogram returned");
            throw new Exception("Invalid result " + result);
        } else {
            m_loggerProline.debug("Result :\n" + result);

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            Type peakArrayListType = new TypeToken<ArrayList<Peak[]>>() {
            }.getType();

            ArrayList<Peak[]> peakArray = gson.fromJson((String) result, peakArrayListType);

            for (int i = 0; i < peakArray.size(); i++) {
                m_features.get(i).addPeakArray(peakArray.get(i));
            }

        }

    }

}
