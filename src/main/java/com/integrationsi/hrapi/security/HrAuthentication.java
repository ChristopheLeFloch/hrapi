
package com.integrationsi.hrapi.security;

import com.hraccess.openhr.IHRRole;
import com.hraccess.openhr.IHRUser;
import com.hraccess.openhr.dossier.HRDossierCollection;
import com.hraccess.openhr.dossier.HRDossierCollectionParameters;
import com.hraccess.openhr.dossier.HRDossierFactory;
import com.hraccess.openhr.msg.HRMsgDressSqlStatement;
import com.hraccess.openhr.msg.HRResultDressSqlStatement;
import com.integrationsi.hrapi.session.SessionException;
import com.integrationsi.hrapi.session.SessionManager;



public class HrAuthentication  {

	
    private String vsid;
    
	private IHRUser hrUser;
    private User user;
    
   
    
	private boolean authenticated;

	
	

	/**
	 * Constructeur permettant de créer une authentification à partir du vsid si celui ci est valide.
	 * 
	 */
    public HrAuthentication(String vsid) throws SessionException {
    	hrUser = (SessionManager.getUser(vsid));
        this.setVsid(vsid);
        this.authenticated = true;
        this.setUser(new User(hrUser));
    }
    
    
	/**
	 * Constructeur permettant de créer une authentification à partir d'un login
	 * 
	 */
    public HrAuthentication(UserLogin login) throws SessionException {
    	hrUser = SessionManager.connectUser(login.getUser(), login.getPassword());
        this.vsid = hrUser.getVirtualSessionId();
        this.authenticated = true;
        this.setUser(new User(hrUser));   
    }

    public HrAuthentication() {
		this.authenticated = false;
	}


	public String getVsid() {
        return vsid;
    }

    public void setVsid(String vsid) {
        this.vsid = vsid;
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		this.user.getRoles().forEach((r) -> {
			
			
		});
	}


    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public void setAuthenticated(boolean arg0) {
        this.authenticated = arg0;

    }

	public Role getRole() {
		return this.user.getRole();
	}

	public void setRole(Role role) {
		this.user.setRole(role);
	}


	public static void disconnectUser(String vsid) throws SessionException {
		SessionManager.disconnectUser(vsid);
		
	}
	
 
    public  String getDressingCode(String cdstdo, String activity) {
		  HRMsgDressSqlStatement request = new HRMsgDressSqlStatement();
	        request.addStatement(cdstdo, "select za.NUDOSS from " + cdstdo + "00 za"); // Data extraction order
	        request.setRoleTemplate(this.user.getRole().getCode());
	        request.setRoleParameter(this.user.getRole().getValue());
	        request.setActivity(activity);

	        // Sending message via the session (synchronous task)
	        final StringBuilder sb = new StringBuilder();
	        HRResultDressSqlStatement result = (HRResultDressSqlStatement) this.hrUser.getSession().sendMessage(request);

	        result.getStatementsAsList().forEach(s -> sb.append(s));
	        return sb.toString();
	}
    
    public HRDossierCollection getDossierCollection(HRDossierCollectionParameters parameters) {
    	
    	
    	IHRRole hrRole = null;
    	  for (IHRRole role : this.hrUser.getRoles()) {
    		  if (role.getParameter() == this.user.getRole().getValue() 
    			&& role.getTemplate() == this.user.getRole().getCode()) {
    			  hrRole = role;
    		  }
    
    	  }

    		 
    	   	
		//Instantiating a new dossier collection with given role, conversation and configuration
		return new HRDossierCollection(parameters,	
										this.hrUser.getMainConversation(),	
										hrRole,
										new HRDossierFactory(HRDossierFactory.TYPE_DOSSIER));
    }



}