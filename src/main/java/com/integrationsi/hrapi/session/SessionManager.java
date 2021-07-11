package com.integrationsi.hrapi.session;

import com.hraccess.openhr.IHRSession;
import com.hraccess.openhr.IHRUser;
import com.hraccess.openhr.exception.AuthenticationException;
import com.hraccess.openhr.exception.SessionBuildException;
import com.hraccess.openhr.exception.SessionConnectionException;
import com.hraccess.openhr.exception.UserConnectionException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class SessionManager {

    public SessionManager(PropertiesConfiguration propertiesConfiguration) throws AuthenticationException, SessionBuildException, SessionConnectionException, ConfigurationException, UserConnectionException, IllegalStateException {
    	Session.getInstance().initSession(propertiesConfiguration);
	}

	public static IHRSession getSession() {

        try {
			return Session.getInstance().getSession();
		} catch (AuthenticationException | SessionBuildException | SessionConnectionException | ConfigurationException
				| UserConnectionException | IllegalStateException e) {
			// TODO Auto-generated catch block
            e.printStackTrace();
            return null;
		}
    }

    public static IHRUser connectUser(String user, String password) throws SessionException {
        try {
            return Session.getInstance().getSession().connectUser(user, password);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(user);
            se.setCategory("Erreur d'authentification");
            throw se;

        } catch (UserConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(user);
            se.setCategory("Erreur d'authentification");
        
            throw se;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(); 
            SessionException se = new SessionException();
            se.setUser(user);
            se.setCategory("Erreur technique");
            throw se;
        } catch (SessionBuildException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(user);
            se.setCategory("Erreur technique");
            throw se;
        } catch (SessionConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(user);
            se.setCategory("Erreur technique");
            throw se;
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(user);
            se.setCategory("Erreur de fichier de configuration");
            throw se;
        }
            
    }

	public static IHRUser getUser(String vsid) throws SessionException {
		try {
		  return Session.getInstance().getSession().retrieveUser(vsid);
		 } catch (AuthenticationException e) {
	            e.printStackTrace();
	            SessionException se = new SessionException();
	            se.setUser("");
	            se.setCategory("Erreur d'authentification");
	            throw se;

	        } catch (UserConnectionException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            SessionException se = new SessionException();
	            se.setUser("");
	            se.setCategory("Erreur d'authentification");
	        
	            throw se;
	        } catch (IllegalStateException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace(); 
	            SessionException se = new SessionException();
	            se.setUser("");
	            se.setCategory("Erreur technique");
	            throw se;
	        } catch (SessionBuildException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            SessionException se = new SessionException();
	            se.setUser("");
	            se.setCategory("Erreur technique");
	            throw se;
	        } catch (SessionConnectionException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            SessionException se = new SessionException();
	            se.setUser("");
	            se.setCategory("Erreur technique");
	            throw se;
	        } catch (ConfigurationException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            SessionException se = new SessionException();
	            se.setUser("");
	            se.setCategory("Erreur de fichier de configuration");
	            throw se;
	        }
	}


    public static void disconnectUser(String vsid) throws SessionException {
        try {
            Session.getInstance().getSession().disconnectUser(vsid);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(vsid);
            se.setCategory("Erreur d'authentification");
            throw se;

        } catch (UserConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(vsid);
            se.setCategory("Erreur d'authentification");        
            throw se;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(); 
            SessionException se = new SessionException();
            se.setUser(vsid);
            se.setCategory("Erreur technique");
            throw se;
        } catch (SessionBuildException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(vsid);
            se.setCategory("Erreur technique");
            throw se;
        } catch (SessionConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(vsid);
            se.setCategory("Erreur technique");
            throw se;
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            SessionException se = new SessionException();
            se.setUser(vsid);
            se.setCategory("Erreur de fichier de configuration");
            throw se;
        }
            
    }


    
}