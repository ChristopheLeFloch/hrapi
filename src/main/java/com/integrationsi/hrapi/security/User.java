package com.integrationsi.hrapi.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hraccess.openhr.IHRConversation;
import com.hraccess.openhr.IHRRole;
import com.hraccess.openhr.IHRUser;
import com.hraccess.openhr.UpdateMode;
import com.hraccess.openhr.beans.HRDataSourceParameters;
import com.hraccess.openhr.dossier.HRDataSect;
import com.hraccess.openhr.dossier.HRDossier;
import com.hraccess.openhr.dossier.HRDossierCollection;
import com.hraccess.openhr.dossier.HRDossierCollection.CommitResult;
import com.hraccess.openhr.dossier.HRDossierCollectionCommitException;
import com.hraccess.openhr.dossier.HRDossierCollectionException;
import com.hraccess.openhr.dossier.HRDossierCollectionParameters;
import com.hraccess.openhr.dossier.HRDossierFactory;
import com.hraccess.openhr.dossier.HRDossierListIterator;
import com.hraccess.openhr.dossier.HRKey;
import com.hraccess.openhr.dossier.HROccur;
import com.hraccess.openhr.dossier.IHRKey;
import com.hraccess.openhr.msg.HRResultUserError.Error;
import com.integrationsi.hrapi.commit.CommitStatus;
import com.integrationsi.hrapi.commit.HrUpdateCommitResult;
import com.integrationsi.hrapi.commit.TechnicalCommitError;
import com.integrationsi.hrapi.commit.TechnicalError;
import com.integrationsi.hrapi.hrentity.HrOccur;
import com.integrationsi.hrapi.util.SqlUtils;



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
	

	public HrUpdateCommitResult commitOccurs(String processus, List<HrOccur> data)
			throws HRDossierCollectionException {
		
		HrUpdateCommitResult result = new HrUpdateCommitResult();
		
		if (data.size() == 0) return result;
		
		// liste des cles a traiter
		Map<Integer, List<HrOccur>> map = new HashMap<Integer, List<HrOccur>>();
		
		// liste des informations à traiter
		HashSet<String> informations = new HashSet<String>();
		// structure à traiter
		String structure = data.get(0).getMainStructure();
		
		// construction de la liste des dossiers à traiter
		// et de la liste des informations à traiter
		data.forEach((d) -> {
			List<HrOccur> occurs = map.get(d.getNudoss());
			if (occurs == null) {
				occurs = new ArrayList<HrOccur>();
				map.put(d.getNudoss(), occurs);
			}
			occurs.add(d);
		});

		ArrayList keys = new ArrayList(map.keySet());
		
		HRDossierCollection collection = this.initDossierCollection(processus, structure, new ArrayList(informations), keys);

		String select = "select nudoss from ZY00 where nudoss in " + SqlUtils.getSqlNudossList(keys);
		HRDossierListIterator iterator = collection.loadDossiers(select); 
		
		while (iterator.hasNext()) {
			HRDossier hrDossier = iterator.next();
			List<HrOccur> occurs = map.get(hrDossier.getNudoss());
			
			for (HrOccur o: occurs) {
				HRDataSect dataSection = hrDossier.getDataSectionByName(o.getMainInformation());

				// Récupération de l'occurrence
				HROccur hrOccur = null;
				if (o.getNulign() == -1 ) { // création
					HRKey k = new HRKey(o.getHrEntityKey());
					hrOccur = dataSection.createOccur(k);
				} else { //modification
					hrOccur = dataSection.getOccurByNulign(o.getNulign());
				}				
				// Modification des valeurs
				for (Map.Entry<String, Object> entry: o.getHrEntityMap().entrySet() ) {
					hrOccur.setValue(entry.getKey(), entry.getValue());
				}
			};
		}
		

		CommitResult r;
		try {
			r = collection.commitAllDossiers().getDossierCommitResult();
		} catch (HRDossierCollectionCommitException e) {
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.UNKNOWN, e.getMessage()));
			return result;
		}
		
		Error[] errors = r.getErrors();
		if (errors.length == 0) {
			result.setStatus(CommitStatus.OK);
			return result;
		}
		
		result.setStatus(CommitStatus.HR_ERRORS);
		for (com.hraccess.openhr.msg.HRResultUserError.Error error : r.getErrors()) {
            if (error.weight == 5) {    
            	IHRKey key = r.getErrorDossierId(error).getDossierKey();
            	HRDossier d = collection.getDossier(key);
            	result.addBusinessError(d, error);
            }
		}
		
		return result;
	}

	
	private HRDossierCollection initDossierCollection(String processus, String structure, List<String> informations, List<Long> keys) throws HRDossierCollectionException {

		HRDossierCollectionParameters dossierCollectionParameters =new HRDossierCollectionParameters();
		dossierCollectionParameters.addDataSection(new HRDataSourceParameters.DataSection("00"));
		dossierCollectionParameters.setUpdateMode(UpdateMode.NORMAL);
		dossierCollectionParameters.setIgnoreSeriousWarnings(true);
		dossierCollectionParameters.setType(HRDossierCollectionParameters.TYPE_NORMAL);
		dossierCollectionParameters.setProcessName(processus);
		dossierCollectionParameters.setDataStructureName(structure);
		informations.forEach( i -> dossierCollectionParameters.addDataSection(new HRDataSourceParameters.DataSection(i)));
		   	
		//Instantiating a new dossier collection with given role, conversation and configuration
		return new HRDossierCollection(dossierCollectionParameters,	
										this.conversation,	
										hrRole,
										new HRDossierFactory(HRDossierFactory.TYPE_DOSSIER));
		
	}
		
		





}