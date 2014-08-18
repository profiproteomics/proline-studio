package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.*;
import fr.profi.util.StringUtils;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import fr.proline.studio.dpm.task.util.JMSConstants;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.*;

/**
 *
 * * TEST JMS VERSION.... CRACRA ET A REPRENDRE ! 
 * 
 * @author VD225637
 */
public class ValidationJMSTask extends AbstractServiceTask implements MessageListener  {
    
        //PSM PreFilter
    public static String RANK_FILTER_KEY = "RANK";
    public static String RANK_FILTER_NAME = "Rank";
    public static String SCORE_FILTER_KEY = "SCORE";
    public static String SCORE_FILTER_NAME = "Score";
    public static String PEP_LENGTH_FILTER_KEY = "PEP_SEQ_LENGTH";
    public static String PEP_LENGTH_FILTER_NAME = "Length";
    public static String MASCOT_EVAL_FILTER_KEY = "MASCOT_EVALUE";
    public static String MASCOT_EVAL_FILTER_NAME = "e-Value";
    public static String MASCOT_IT_SCORE_FILTER_KEY = "SCORE_IT_P-VALUE";
    public static String MASCOT_IT_SCORE_FILTER_NAME = "Identity p-Value";
    public static String MASCOT_HT_SCORE_FILTER_KEY = "SCORE_HT_P-VALUE";
    public static String MASCOT_HT_SCORE_FILTER_NAME = "Homology p-Value";
    public static String SINGLE_PSM_QUERY_FILTER_KEY = "SINGLE_PSM_PER_QUERY";
    public static String SINGLE_PSM_QUERY_FILTER_NAME = "Single PSM per MS Query";               
    public static String SINGLE_PSM_RANK_FILTER_KEY = "SINGLE_PSM_PER_RANK";
    public static String SINGLE_PSM_RANK_FILTER_NAME = "Single PSM per Rank";               
    
    //Protein PreFilter
    public static String SPECIFIC_PEP_FILTER_KEY = "SPECIFIC_PEP";
    public static String SPECIFIC_PEP_FILTER_NAME = "Specific Peptides";               

    public static final String TASK_JMSLIST_INFO = "JMSServices";     
    private DDataset m_dataset = null;
    private String m_description;
    private HashMap<String, String> m_argumentsMap;
    private String m_scoringType = null;

    // JMS Specific params
    Connection connection = null;
    Session session = null;
    private ServiceState currentState = null;
    
    //Params for test !
        /* Count received messages */
    // Juste pour afficher le retour !
    public final AtomicInteger MESSAGE_COUNT_SEQUENCE = new AtomicInteger(0);

    public static final String DATE_FORMAT = "%td/%<tm/%<tY %<tH:%<tM:%<tS.%<tL";

    private static final int MESSAGE_BUFFER_SIZE = 2048;

    private static final String TAB = "    ";
    
    public ValidationJMSTask(AbstractServiceCallback callback, DDataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId, String scoringType){
        super(callback, false /*asynchronous*/, new TaskInfo("JMS Validation of Search Result "+dataset.getName(), true, TASK_JMSLIST_INFO));  
        m_dataset = dataset;
        m_description = description;
        m_argumentsMap = argumentsMap;       
        m_scoringType = scoringType;
    }
    
    @Override
    public boolean askService() {
        boolean serviceResult = false;
        
	try {
            currentState = ServiceState.STATE_WAITING;
            
            
	    // Get JMS Connection
	    connection = JMSConstants.getJMSConnection();

	    /* Thread specific : Session, Producer, Consumer ... */
	    // Step 5. Create a JMS Session (Session MUST be confined in current Thread)
	    // Not transacted, AUTO_ACKNOWLEDGE
	    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	    // Step 6. Create a JMS Message Producer (Producer MUST be confined in current Thread)
	    final MessageProducer producer = session.createProducer(JMSConstants.getServiceQueue());
	    final TemporaryQueue replyQueue = session.createTemporaryQueue();
	    final MessageConsumer responseConsumer = session.createConsumer(replyQueue);
            
	    responseConsumer.setMessageListener(this);

	    connection.start(); // Explicitely start connection to begin Consumer reception

	    // Step 7. Create a Text Message
            m_id = m_idIncrement++;
            final JSONRPC2Request jsonRequest = new JSONRPC2Request("tot", Integer.valueOf(m_id));
            jsonRequest.setNamedParams(createParams());
           
            final TextMessage message = session.createTextMessage(jsonRequest.toJSONString());

            /* ReplyTo = Temporary Destination Queue for Server -> Client response */
            message.setJMSReplyTo(replyQueue);
            message.setStringProperty(JMSConstants.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ValidateResultSet");
	
            // Step 8. Send the Message
            producer.send(message);
            serviceResult = true;
            m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
            
	} catch (Exception ex) {
	    m_loggerProline.error("Error sending JMS Message", ex);
            currentState = ServiceState.STATE_FAILED;
	} finally {

	    if (session != null) {
		try {
		    session.close();
		    m_loggerProline.info("JMS Session closed");
		} catch (Exception exClose) {
		    m_loggerProline.error("Error closing JMS Session", exClose);
		}
	    }
	}
        return serviceResult;
 }
    
    private HashMap<String, Object> createParams() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_dataset.getProject().getId());
        params.put("result_set_id", m_dataset.getResultSetId());
        params.put("description", m_description); //JPM.TODO : string is ""

        // Peptide Pre-Filters
        ArrayList pepFilters = new ArrayList();

        if (m_argumentsMap.containsKey(RANK_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", RANK_FILTER_KEY);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(RANK_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey(SCORE_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", SCORE_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(SCORE_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey(MASCOT_EVAL_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", MASCOT_EVAL_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(MASCOT_EVAL_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey(PEP_LENGTH_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PEP_LENGTH_FILTER_KEY);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(PEP_LENGTH_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey(MASCOT_IT_SCORE_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", MASCOT_IT_SCORE_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(MASCOT_IT_SCORE_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey(MASCOT_HT_SCORE_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", MASCOT_HT_SCORE_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(MASCOT_HT_SCORE_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey(SINGLE_PSM_QUERY_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", SINGLE_PSM_QUERY_FILTER_KEY);
            filterCfg.put("threshold", 1);
            filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get(SINGLE_PSM_QUERY_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey(SINGLE_PSM_RANK_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", SINGLE_PSM_RANK_FILTER_KEY);
            filterCfg.put("threshold", 1);
//                filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get(SINGLE_PSM_RANK_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        params.put("pep_match_filters", pepFilters);

        // Peptide Validator
        if (m_argumentsMap.containsKey("expected_fdr")) {
            HashMap pepMatchValidator = new HashMap();
            pepMatchValidator.put("parameter", m_argumentsMap.get("expected_fdr_parameter"));
            pepMatchValidator.put("expected_fdr", m_argumentsMap.get("expected_fdr"));
            params.put("pep_match_validator_config", pepMatchValidator);
        }



        if (m_argumentsMap.containsKey("use_td_competition")) {
            params.put("use_td_competition", Boolean.parseBoolean(m_argumentsMap.get("use_td_competition")));
        }

        params.put("pep_set_score_type", m_scoringType);

        // Protein Pre-Filters
        ArrayList proteinFilters = new ArrayList();

        if (m_argumentsMap.containsKey(SPECIFIC_PEP_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", SPECIFIC_PEP_FILTER_KEY);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(SPECIFIC_PEP_FILTER_KEY)));
            proteinFilters.add(filterCfg);
        }

        params.put("prot_set_filters", proteinFilters);


        // protein parameters
        if (m_argumentsMap.containsKey("protein_expected_fdr")) {
            HashMap protSetValidator = new HashMap();
            protSetValidator.put("parameter", "SCORE");
            protSetValidator.put("expected_fdr", m_argumentsMap.get("protein_expected_fdr"));
            protSetValidator.put("validation_method", "PROTEIN_SET_RULES");
            params.put("prot_set_validator_config", protSetValidator);
        }

        return params;
    }

    @Override
    public ServiceState getServiceState() {
        return currentState;
    }

    
    // JMS Specific Method
        @Override
    public void onMessage(final Message jmsMessage) {
	m_loggerProline.info("Receiving message n° " + MESSAGE_COUNT_SEQUENCE.incrementAndGet() + " : "
		+ formatMessage(jmsMessage));

	if (jmsMessage instanceof TextMessage) {
	    final TextMessage textMessage = (TextMessage) jmsMessage;

	    try {
		final String jsonString = textMessage.getText();

		/* TODO Use JSON-RPC Response */
		traceJSONResponse(jsonString);
                currentState = ServiceState.STATE_DONE;
                callback(true);
	    } catch (Exception ex) {
                currentState = ServiceState.STATE_FAILED;
		m_loggerProline.error("Error handling JMS Message", ex);
                callback(false);
	    }

	} else {
	    m_loggerProline.warn("Invalid JMS Message type");
            currentState = ServiceState.STATE_FAILED;
            callback(false);
	}
    }
        
     public static void traceJSONResponse(final String jsonString) throws JSONRPC2ParseException {
	final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);

	if (jsonMessage instanceof JSONRPC2Notification) {
	    final JSONRPC2Notification jsonNotification = (JSONRPC2Notification) jsonMessage;

	    System.out.println("JSON Notification method: " + jsonNotification.getMethod());

	    final Map<String, Object> namedParams = jsonNotification.getNamedParams();

	    if ((namedParams != null) && !namedParams.isEmpty()) {
		final StringBuilder buff = new StringBuilder("Params: ");

		boolean first = true;

		final Set<Map.Entry<String, Object>> entries = namedParams.entrySet();

		for (final Map.Entry<String, Object> entry : entries) {

		    if (first) {
			first = false;
		    } else {
			buff.append(" | ");
		    }

		    buff.append(entry.getKey());
		    buff.append(" : ").append(entry.getValue());
		}

		System.out.println(buff);
	    }

	} else if (jsonMessage instanceof JSONRPC2Response) {
	    final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;

	    System.out.println("JSON Response Id: " + jsonResponse.getID());

	    final JSONRPC2Error jsonError = jsonResponse.getError();

	    if (jsonError != null) {
		m_loggerProline.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
		m_loggerProline.error("JSON Throwable", jsonError);
	    }

	    final Object result = jsonResponse.getResult();

	    if (result == null) {
		System.out.println("No result");
	    } else {
		System.out.println("Result :\n" + result);
	    }

	}

    }
        

    /**
     * Formats some Header filds, Properties and Body of the given JMS Message to print usefull debug info.
     */
    public static String formatMessage(final Message message) {

	if (message == null) {
	    throw new IllegalArgumentException("Message is null");
	}

	final StringBuilder buff = new StringBuilder(MESSAGE_BUFFER_SIZE);

	try {
	    buff.append(message.getClass().getName()).append("  ").append(message.getJMSMessageID());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSCorrelationID ");
	    append(buff, message.getJMSCorrelationID());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSTimestamp ")
		    .append(String.format(DATE_FORMAT, message.getJMSTimestamp()));
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSDestination ");
	    append(buff, message.getJMSDestination());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    buff.append(TAB).append("JMSReplyTo ");
	    append(buff, message.getJMSReplyTo());
	    buff.append(StringUtils.LINE_SEPARATOR);

	    final Enumeration<String> nameEnum = message.getPropertyNames();

	    while (nameEnum.hasMoreElements()) {
		final String propertyName = nameEnum.nextElement();
		buff.append(TAB).append('[').append(propertyName).append("] : ");

		final String propertyValue = message.getStringProperty(propertyName);

		if (propertyValue == null) {
		    buff.append("NULL");
		} else {
		    buff.append('[').append(propertyValue).append(']');
		}

		buff.append(StringUtils.LINE_SEPARATOR);
	    }

	    if (message instanceof TextMessage) {
		buff.append(TAB).append(((TextMessage) message).getText());
	    }

	    buff.append(StringUtils.LINE_SEPARATOR);
	} catch (Exception ex) {
	    m_loggerProline.error("Error retrieving JMS Message header or content", ex);
	}

	return buff.toString();
    }
    
    private static void append(final StringBuilder sb, final Object obj) {
	assert (sb != null) : "append() sb is null";

	if (obj == null) {
	    sb.append("NULL");
	} else {
	    sb.append(obj);
	}

    }        
        
}
