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

/**
 * SessionDeletionServlet
 * 
 * Deletes all location entries that have the session name defined by the client request 
 */
public class SessionDeletionServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(SessionDeletionServlet.class.getName());
	
	@Override    
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String sessionName;
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));

		sessionName = br.readLine();    

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		log.log(Level.INFO, "Attempting to delete session " + sessionName);
		Key sessionKey = KeyFactory.createKey("Session", sessionName);
		Query locationQuery = new Query("Location", sessionKey);
		List<Entity> locations = datastore.prepare(locationQuery).asList(FetchOptions.Builder.withLimit(50));

		if (locations.isEmpty()) {
			log.log(Level.INFO, "Set is empty");
		} else {

			for (Entity location : locations) {     
				datastore.delete(location.getKey());
			}
			log.log(Level.INFO, "Delete complete");
		}        
		
		Key sessionListKey = KeyFactory.createKey("SessionList", "SessionListName");
		Query sessionQuery = new Query("SessionList", sessionListKey); 
		List<Entity> sessions = datastore.prepare(sessionQuery).asList(FetchOptions.Builder.withLimit(20)); 	  
		
		for (Entity session : sessions) {     
			if(session.getProperty("name").equals(sessionName)){
				datastore.delete(session.getKey());
				log.log(Level.INFO, "Session " + sessionName + " deleted from session list");
				break;
			}
		}
	}
}
