
var timer; // Timer variable used to generate location data periodically
var queryResults; // Object containing location query results from server 
var sessionList; // Object containing session names received from server
var sessionName; // Name of the current session
var dark = false; // Boolean describing the state of ambient light
var transformables; // List of objects that change their look according to ambient light
var tabLinks = new Array(); // Array containing links to menu tabs
var contentDivs = new Array(); // Array of element references containing tab content
var latitude = 0; // Current latitude
var longitude = 0; // Current Longitude
var connection; // Variable describing connection state
var map; // Google map variable
var storageKey = 0; // Variable describing local storage key
var synchronizing = false; // Boolean determining if the application is synchronizing local data with server
google.maps.event.addDomListener(window, 'load', initialize);

//Initializes the Google map and registers sensor event listeners
function initialize(){
	
	var myOptions = {
			center:new google.maps.LatLng(65.0564222,25.4559615),
			panControl: false,
			zoom:15,
			mapTypeId:google.maps.MapTypeId.ROADMAP
	};
	map = new google.maps.Map(document.getElementById("mapCanvas"), myOptions);
		
	window.addEventListener("devicelight", lightListener, false);
	updateOnlineStatus("waiting...");
	window.addEventListener("online", updateOnlineStatus);
	window.addEventListener("offline", updateOnlineStatus);
	connection = navigator.onLine ? "Online" : "Offline";
	
	setLightSensitives();	
	initTabs();
	getSessions();
	
	document.getElementById("infoScreen").innerHTML = "Ready";
}

// Fired when connection state changes. Displays connection state.
function updateOnlineStatus() {
	
	connection = navigator.onLine ? "Online" : "Offline";
	if(connection === "Online")
		synchronizeData();
	
	document.getElementById("state").innerHTML = connection;	
}

// Displays current coordinates
function showCoordinates(){
	document.getElementById("lat").innerHTML = latitude.toFixed(4);
	document.getElementById("long").innerHTML = longitude.toFixed(4);
}

// Initializes the tabs used in the UI
function initTabs(){
	
	var tabListItems = document.getElementById('tabs').childNodes;
	for(var i = 0; i<tabListItems.length; i++){
		if(tabListItems[i].nodeName == "LI"){
			var tabLink = getFirstChildWithTagName(tabListItems[i], 'A');
			var id = getHash(tabLink.getAttribute('href'));
			tabLinks[id] = tabLink;
			contentDivs[id] = document.getElementById( id );
		}
	}
	var i = 0;

	for (var id in tabLinks){
		tabLinks[id].onclick = showTab;
		tabLinks[id].onfocus = function() {this.blur()};
		if(i == 0) 
			tabLinks[id].className = 'selected';
		i++;
	}
	var i = 0;

	for (var id in contentDivs){
		if (i != 0) contentDivs[id].className = 'tabContent hide';
		i++;
	}
}

// Sends the name of the new session to server and starts to generate location data periodically
function startRecording(){
	
    sessionName = $("#session").val();
	    
    if(sessionName == null || sessionName == "")
    	alert("Enter a name for the session");
    else{
    	$.ajax({
    		url: "newSession",
    		type: 'POST',
    		data: sessionName
    	}).done(function(resp){
    		timer = setInterval(function(){generatePositionData()}, 3000);
    	});
    }
}

// Stops the session recording process
function stopRecording(){
	clearInterval(timer);
	$("#infoScreen").html("Session stopped");
 	getSessions();
}

// Generates pseudo random location data. Uses the real connection status
function generatePositionData(){
	
	var message = new Object();
    message.sessionName = sessionName;
    message.longitude = longitude = (Math.random()/1000)+25.4559615;
    message.latitude = latitude = (Math.random()/1000)+65.0564222;
    message.state = connection;
    
    if(connection === "Online" && synchronizing === false)
    	sendPosition(message);
    else if(connection === "Offline")
    	saveLocally(message);
    	
    showCoordinates();
}

// Saves the location data locally
function saveLocally(msg){
	
	localStorage.setItem(storageKey.toString(), JSON.stringify(msg));
	$("#infoScreen").html("Saved location to local storage<br>" + new Date());
	storageKey++;
}

// Sends all locally stored entries to server and clears the local storage
function synchronizeData(){
	
	synchronizing = true;
	var msg;
	
	$("#infoScreen").html("Connection found, sending local data to server" +
			"<br>No new location data is saved while synchronizing");
	
	for (var i=0; i<localStorage.length; i++){
		msg = JSON.parse(localStorage.getItem(i.toString()));
		sendPosition(msg);
	}
	
	localStorage.clear();
	storageKey = 0;
	synchronizing = false;
}

// Sends the location message the server in JSON format
function sendPosition(msg){
	
    $.ajax({
        url: "savePosition",
        type: 'POST',
        data: JSON.stringify(msg)
    }).done(function(resp){
    	$("#infoScreen").html("Saved location to server " + "<br>" + new Date());
    });
}

// Queries all location entries with selected session name from server. 
// Results are received in JSON format and mapped into a variable.
// All location entries are shown on google map with a circle colored 
// according to entries connection state.
function queryLocations() {		 
	var selection = document.getElementById('querySelector');
	var msg = selection.options[selection.selectedIndex].value;
	
    $.ajax({
        url: "query",
        type: 'POST',
        dataType: 'json',
        data: msg    
    }).done(function(resp){
    	var cityCircle;
    	var circleColor;
    	var dataLimit = 8;
    	queryResults = resp;
    	
    	$("#infoScreen").html("");
    	for(var i=0; i<queryResults.length; i++){      
    		// Displays sample of data received in text format
    		if(i<dataLimit){
	        	$("#infoScreen").append(i+1 + ". Long: " + queryResults[i].longitude +
	        			", Lat: " + queryResults[i].latitude + 
	        			", State: " + queryResults[i].state + "<br>");	
	        	if(i == (dataLimit-1))
	        		$("#infoScreen").append("...");
    		}
        	
        	if(queryResults[i].state === "Online")
        		circleColor = 'green';
        	else
        		circleColor = 'red';
        		
        	// Draws the location spots on google map
        	var populationOptions = {
        		      strokeColor: circleColor,
        		      strokeOpacity: 0.8,
        		      strokeWeight: 2,
        		      fillColor: circleColor,
        		      fillOpacity: 0.35,
        		      map: map,
        		      center: new google.maps.LatLng(queryResults[i].latitude, 
        		    		  queryResults[i].longitude),
        		      radius: 3
        		    };
        		    // Add the circle for this city to the map.
        		    cityCircle = new google.maps.Circle(populationOptions);
    	}      	    		
    });
}

// Deletes all location entries having the determined session name
function deleteSession() {		 
	
	var selection = document.getElementById('querySelector');
	var msg = selection.options[selection.selectedIndex].value;
    
    $.ajax({
        url: "delete",
        type: 'POST',
        dataType: 'text',
        data: msg
    }).done(function(resp){
    	$("#infoScreen").html("Session deleted");
    	getSessions();
    });
}	

// Retrieves all the names of sessions stored on the server
function getSessions(){
	
	// Delay sessionlist retrieval to give the Datastore time to work
	setTimeout(function(){
	    $.ajax({
	        url: "getSessions",
	        type: 'POST',
	        dataType: 'json'
	    }).done(function(resp){
	    	sessionList = resp;
	    	
	    	if(sessionList[0] === "emptyList")
	        	$("#querySelector").html("");
	    	else{
	        	$("#querySelector").html("");
	        	for(var i=0; i<sessionList.length; i++){
	        		$("#querySelector").append("<option value=\"" + sessionList[i] + 
	        				"\">" + sessionList[i] + "</option>");
	        	}	
	    	}
	    });
	}, 1000);
}

// UI related functions

//Initializes the array containing elements that react to ambient light
function setLightSensitives(){
	transformables = new Array("ul", "li", "a.selected", "div.tabContent", "#pageContainer", "#mainHeader");
}

// Function triggered every time a change in ambient light is registered.
// Changes properties of elements that are defined to be light sensitive.
function lightListener(lightEvent){

	var currentLux = lightEvent.value; // Contains ambient light sensor data

	if (currentLux > 10) {
		if(dark){
			dark = false;
			toggleDarkness();
			document.body.style.background = "#E6E6E6";
		}
	}else{
		if(!dark){
			toggleDarkness();
			dark = true;
			document.body.style.background = "#070d0d";
		}
	}
}

// Uses jQuery to toggle "dark" class on and off for defined elements
function toggleDarkness(){
	
	for(var i=0; i<transformables.length; i++){
		$(transformables[i]).toggleClass("dark");
	}
}

// Highlights the selected tab in the ui and dims all others.
// Also shows the selected content div and hides all others.
function showTab() {
	var selectedId = getHash( this.getAttribute('href') );

	for ( var id in contentDivs ) {
		if ( id == selectedId ) {
			tabLinks[id].className = 'selected';
			contentDivs[id].className = 'tabContent';
		} else {
			tabLinks[id].className = '';
			contentDivs[id].className = 'tabContent hide';
		}
	}
	// Stop the browser following the link
	return false;
}

// Returns the first child of an element with defined tagname
function getFirstChildWithTagName( element, tagName ) {
	for ( var i = 0; i < element.childNodes.length; i++ ) {
		if ( element.childNodes[i].nodeName == tagName ) return element.childNodes[i];
	}
}

// Returns url string after the '#' character
function getHash( url ) {
	var hashPos = url.lastIndexOf('#');
	return url.substring(hashPos + 1);
}