package com.integrationsi.hrapi.security;

import java.util.ArrayList;
import java.util.List;

import com.hraccess.openhr.IHRUser;



/**
 * Modélise un utilisateur Hr Access. 
 * Par rapport à l'interface standard openHr, celle-ci à une interface simplifiée.
 *
 * @author CLEFL
 *
 */
public class User {

    private String code;
    private String label;
    private String matcle;



	public String getMatcle() {
		return matcle;
	}


	public void setMatcle(String matcle) {
		this.matcle = matcle;
	}


	private List<Role> roles; 
    private Role role;

 

    
    protected User (IHRUser hrUser) {
    	this.code=hrUser.getUserId();
    	this.label=hrUser.getLabel();
    	roles =new ArrayList<Role>();
    	hrUser.getRoles().forEach((HRRole) -> {
    		Role r = new Role(HRRole);
    		roles.add(r);   
  	    });

    }


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}



	public List<Role> getRoles() {
		return roles;
	}


	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}


	public Role getRole() {
		return role;
	}


	public void setRole(Role role) {
		this.role = role;
	}






}