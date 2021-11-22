package com.integrationsi.hrapi.session;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.hraccess.openhr.HRSessionFactory;
import com.hraccess.openhr.IHRSession;
import com.hraccess.openhr.exception.AuthenticationException;
import com.hraccess.openhr.exception.SessionBuildException;
import com.hraccess.openhr.exception.SessionConnectionException;
import com.hraccess.openhr.exception.UserConnectionException;


public final class Session {




  private IHRSession session;


    // L'utilisation du mot clé volatile
    // permet d'éviter le cas où "Singleton.instance" est 	non nul,
     // mais pas encore "réellement" instancié.
  private static volatile Session instance = null;

    private Session() throws  SessionBuildException, 
                              SessionConnectionException, 
                               
                              AuthenticationException, 
                              UserConnectionException, 
                              IllegalStateException {
         super();
    }

      /**
   * Méthode permettant de renvoyer la session
   * 
   * @return Retourne l'instance du singleton.
   * @throws IllegalStateException
   * @throws UserConnectionException
   * @throws ConfigurationException
   * @throws SessionConnectionException
   * @throws SessionBuildException
   * @throws AuthenticationException
   */
  protected final static Session getInstance() throws  SessionBuildException,
      SessionConnectionException,  AuthenticationException, UserConnectionException,
      IllegalStateException {
        //Le "Double-Checked Singleton"/"Singleton doublement vérifié" permet 
         //d'éviter un appel coûteux à synchronized, 
         //une fois que l'instanciation est faite.
        if (Session.instance == null) {
           // Le mot-clé synchronized sur ce bloc empêche toute instanciation
           // multiple même par différents "threads".
           // Il est TRES important.
           synchronized(Session.class) {
             if (Session.instance == null) {
               Session.instance = new Session();
             }
           }
        }
        return Session.instance;
    }

    protected IHRSession getSession() {
      return this.session;

    }


    protected void initSession(PropertiesConfiguration propertiesConfiguration) throws SessionBuildException, SessionConnectionException {

	    // Creating from given OpenHR configuration file and connecting session to HR Access server
	    this.session = HRSessionFactory.getFactory().createSession(propertiesConfiguration);
    }


}