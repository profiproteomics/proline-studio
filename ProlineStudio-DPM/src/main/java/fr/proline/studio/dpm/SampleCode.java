/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author CB205360
 */
public class SampleCode {

  
    private static void postRequest() {
       
        HttpTransport transport = new ApacheHttpTransport();
        HttpRequestFactory factory = transport.createRequestFactory();
        try {

//			JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), createUserRequest().getParameters());
//			HttpRequest request = factory.buildPostRequest(new GenericUrl("http://localhost:8080/admin/user_account/create"), content);

            JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), createProjectRequest().getParameters());
            HttpRequest request = factory.buildPostRequest(new GenericUrl("http://localhost:8080/admin/project/create"), content);


            System.out.println(content.getData().toString());
            HttpResponse response = request.execute();
            System.out.println(response.parseAsString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static JsonRpcRequest createProjectRequest() {

        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(12356);
        request.setMethod("create");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "myProject");
        params.put("description", "a new test project");
        params.put("owner_id", 10);
        request.setParameters(params);

        return request;
    }

    private static JsonRpcRequest createUserRequest() {

        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(12356);
        request.setMethod("create");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("login", "bruley");
        request.setParameters(params);

        return request;
    }
}
