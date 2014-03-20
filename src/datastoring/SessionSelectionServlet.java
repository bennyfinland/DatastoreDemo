package datastoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;

/**
 * SessionSelectionServlet
 *
 * Queries all location entries that have the session name defined by the client request. Results are
 * sent in JSON format back to the client.
 */
public class SessionSelectionServlet  extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(SessionSelectionServlet.class.getName());
	
    @Override    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
    	BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
    	Gson gson = new Gson();
    	LocationContainer container;
    	String responseString = "[";
    	
    	String sessionName = br.readLine();
    	log.log(Level.INFO, "Retrieving data for session: " + sessionName);
    	
    	if(sessionName != null && sessionName != ""){
    		
    	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();    
    	    Key sessionKey = KeyFactory.createKey("Session", sessionName);
    	    Query query = new Query("Location", sessionKey);    	
    	    List<Entity> locations = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1000)); 	    
    	    
    	    for (Entity location : locations) {   
    	    	container = new LocationContainer();
    	    	container.sessionName = sessionName;
    	    	container.state = location.getProperty("state").toString();
    	    	container.latitude = location.getProperty("latitude").toString();
    	    	container.longitude = location.getProperty("longitude").toString();
    	    	responseString += gson.toJson(container) + ",";
    	    }
    	    responseString = responseString.substring(0, responseString.length()-1);
    	    responseString += "]";
        }else
        	log.log(Level.INFO, "Couldn't retrieve data for session: " + sessionName);
    	        	
    	response.setContentType("application/json");
    	response.setCharacterEncoding("UTF-8");
    	response.getWriter().write(responseString);
    }
}
