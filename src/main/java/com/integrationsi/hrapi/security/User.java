package com.integrationsi.hrapi.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hraccess.openhr.IHRConversation;
import com.hraccess.openhr.IHRRole;
import com.hraccess.openhr.IHRUser;
import com.hraccess.openhr.UpdateMode;
import com.hraccess.openhr.beans.HRDataSourceParameters;
import com.hraccess.openhr.dossier.HRDossierCollection;
import com.hraccess.openhr.dossier.HRDossierCollectionException;
import com.hraccess.openhr.dossier.HRDossierCollectionParameters;
import com.hraccess.openhr.dossier.HRDossierFactory;
import com.hraccess.openhr.dossier.HRDossierListIterator;

import com.integrationsi.hrapi.util.SqlUtils;

import com.integrationsi.hrapi.commit.HrUpdateCommitResult;
import com.integrationsi.hrapi.hrentity.HrOccur;



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
    private IHRConversation conversation;
    private List<IHRRole> hrRoles;
    private IHRRole hrRole;    
    	


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
    	this.conversation = hrUser.getMainConversation();
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
		Optional<IHRRole> optionRole = this.hrRoles.stream().filter(hr ->  hr.getTemplate()==role.getCode() && hr.getParameter()==role.getValue()).findFirst();
		if (optionRole.isPresent()) this.hrRole=optionRole.get();
	}
	

	public HrUpdateCommitResult commitOccurs(String processus, List<HrOccur> data) throws HRDossierCollectionException {
		
		HrUpdateCommitResult result = new HrUpdateCommitResult();
		
		if (data.size() == 0) return result;
		
		// liste des cles a traiter
		Set<Integer> keys = new HashSet<Integer>();
		// liste des informations à traiter
		HashSet<String> informations = new HashSet<String>();
		// structure à traiter
		String structure = data.get(0).getMainStructure();
		
		// construction de la liste des dossiers à traiter
		// et de la liste des informations à traiter
		data.forEach((d) -> {
			keys.add(d.getNudoss());
			informations.add(d.getMainInformation());
		});

		HRDossierListIterator iterators = this.loadDossiers(processus, structure, new ArrayList(informations), new ArrayList(keys));

		return null;
	}

	
	public HRDossierListIterator loadDossiers(String processus, String structure, List<String> informations, List<Long> keys) throws HRDossierCollectionException {

		HRDossierCollectionParameters dossierCollectionParameters =new HRDossierCollectionParameters();
		dossierCollectionParameters.addDataSection(new HRDataSourceParameters.DataSection("00"));
		dossierCollectionParameters.setUpdateMode(UpdateMode.NORMAL);
		dossierCollectionParameters.setIgnoreSeriousWarnings(true);
		dossierCollectionParameters.setType(HRDossierCollectionParameters.TYPE_NORMAL);
		dossierCollectionParameters.setProcessName(processus);
		dossierCollectionParameters.setDataStructureName(structure);
		informations.forEach( i -> dossierCollectionParameters.addDataSection(new HRDataSourceParameters.DataSection(i)));
		   	
		//Instantiating a new dossier collection with given role, conversation and configuration
		HRDossierCollection hrDossierCollection = new HRDossierCollection(dossierCollectionParameters,	
										this.conversation,	
										hrRole,
										new HRDossierFactory(HRDossierFactory.TYPE_DOSSIER));
		
		String select = "select nudoss from ZY00 where nudoss in " + SqlUtils.getSqlNudossList(new ArrayList(keys));
		HRDossierListIterator hrDossierListIterator = hrDossierCollection.loadDossiers(select); 
		return hrDossierListIterator;
		
	}
		
		





}