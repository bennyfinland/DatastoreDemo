package datastoring;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LocationSavingServlet
 *
 * Servlet saving an entry of location data sent by the client.
 * Data is received in JSON format which is then mapped into a 
 * ResponseContainer object using Gson library.
 */
public class LocationSavingServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(LocationSavingServlet.class.getName());
	
    @Override    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        String sessionName;
        Key sessionKey;
        Entity location;
        Gson gson = new Gson();

        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String json = "";
        
        if(br != null)
            json = br.readLine();        
        
    	LocationContainer container = gson.fromJson(json, LocationContainer.class);
    	
    	sessionName = container.sessionName;
    	sessionKey = KeyFactory.createKey("Session", sessionName);
    	location = new Entity("Location", sessionKey);
    	
    	location.setProperty("session", sessionName);
    	location.setProperty("state", container.state);
		location.setProperty("longitude", container.longitude);
		location.setProperty("latitude", container.latitude);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(location);
		
    	log.log(Level.INFO, "Saved a location to session: " + sessionName);
    	
    	response.setContentType("json");
    	response.setCharacterEncoding("UTF-8");
    	response.getWriter().write("{\"result\": \"ok\"}");
    }
}