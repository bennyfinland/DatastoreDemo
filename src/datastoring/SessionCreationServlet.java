package datastoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * SessionCreationServlet
 * 
 * Saves a session name entry into the datastore. 
 */
public class SessionCreationServlet extends HttpServlet{

	private static final Logger log = Logger.getLogger(SessionCreationServlet.class.getName());

	@Override    
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String sessionName;
		Entity session;
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));

        sessionName = br.readLine();    	
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        Key sessionKey = KeyFactory.createKey("SessionList", "SessionListName");
        session = new Entity("SessionList", sessionKey);
        session.setProperty("name", sessionName);
        datastore.put(session);  
        log.log(Level.INFO, "Saved session " + sessionName);
        
    	response.setContentType("text");
    	response.setCharacterEncoding("UTF-8");
    	response.getWriter().write("OK");
	}
}

