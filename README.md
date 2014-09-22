Data store demo
==========================


JS Code poll: DatastoreDemo/war/utils.js (Eclipse)


Operation environment: Andriod with Mozilla Firefox browser

=====================================================================================================================

The purpose of the application is to demonstrate the storing capabilities of an HTML5
application in conjuction with Google App Engine. The demo application generates
pseudo-random location data around VTT Oulu office including longitude, latitude and
the connection state of the browser. It uses the online/offline sensor information to
decide how the data will be stored. When offline an HTML5 Web Storage is used to
cache the data. Once an internet connection is found, the application will send the
cached data to the server side for persistent storing. If the application is online the
generated data will be sent straight to the server. Server side storage is implemented
with servlets that use Google App Engine Datastore API.


=====================================================================================================================

Also used Jquery and Gson(open source library for handling JSON)

=====================================================================================================================

This demo designed used for VTT technical center of Oulu, Finland 2013 
