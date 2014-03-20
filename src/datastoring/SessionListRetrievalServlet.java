package datastoring;

import java.io.IOException;
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
 * SessionListRetrievalServlet
 *
 * Queries all the names of the sessions currently stored in the datastore and returns them
 * to the client in JSON format.
 */
public class SessionListRetrievalServlet extends HttpServlet{
	
	private static final Logger log = Logger.getLogger(SessionListRetrievalServlet.class.getName());

	@Override    
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Gson gson = new Gson();
		String responseString = "[";	
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        log.log(Level.INFO, "Retrieving list of sessions");

	    Key sessionKey = KeyFactory.createKey("SessionList", "SessionListName");
        Query query = new Query("SessionList", sessionKey); 
        List<Entity> sessions = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(50)); 	  
        
	    for (Entity ses : sessions) {   
	    	responseString += gson.toJson(ses.getProperty("name").toString()) + ",";
	    }
	    responseString = responseString.substring(0, responseString.length()-1);
	    responseString += "]";
	    
	    if(sessions.isEmpty())
	    	responseString = "[\"emptyList\"]";

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(responseString);
	}
}