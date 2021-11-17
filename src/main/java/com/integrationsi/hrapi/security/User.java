package com.integrationsi.hrapi.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

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
import com.hraccess.openhr.exception.UserConnectionException;
import com.hraccess.openhr.msg.HRResultUserError.Error;
import com.integrationsi.hrapi.application.ApplicationAccess;
import com.integrationsi.hrapi.commit.CommitStatus;
import com.integrationsi.hrapi.commit.HrUpdateCommitResult;
import com.integrationsi.hrapi.commit.ResourceBatchData;
import com.integrationsi.hrapi.commit.TechnicalCommitError;
import com.integrationsi.hrapi.commit.TechnicalError;
import com.integrationsi.hrapi.commit.ResourceBatchData.Method;
import com.integrationsi.hrapi.hrentity.HrEntity;
import com.integrationsi.hrapi.hrentity.HrMultipleOccur;
import com.integrationsi.hrapi.hrentity.HrToDeleteOccur;
import com.integrationsi.hrapi.hrentity.HrUniqueOccur;
import com.integrationsi.hrapi.hrentity.IHrEntity;
import com.integrationsi.hrapi.hrentity.IHrMultipleEntity;
import com.integrationsi.hrapi.util.SqlUtils;

/**
 * Modélise un utilisateur Hr Access. Par rapport à l'interface standard openHr,
 * celle-ci à une interface simplifiée.
 *
 * @author CLEFL
 *
 */
public class User {

	private IHRUser hrUser;
	private String code;
	private String label;
	private String matcle;
	private List<ApplicationAccess> applicationAccess;
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

	protected User(IHRUser hrUser) {
		this.hrUser = hrUser;
		this.code = hrUser.getUserId();
		this.label = hrUser.getLabel();

		roles = new ArrayList<Role>();
		this.hrRoles = hrUser.getRoles();
		this.hrRoles.forEach((HRRole) -> {
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

	public void setRole(Role role) throws NoRoleException {
		this.role = role;
		Optional<IHRRole> optionRole = this.hrRoles.stream().filter(
				hr -> hr.getTemplate().equals(role.getCode()) && hr.getParameter().equalsIgnoreCase(role.getValue()))
				.findFirst();
		if (optionRole.isPresent())
			this.hrRole = optionRole.get();
		else
			throw new NoRoleException();
	}

	public void setRole(String code, String value) throws NoRoleException {
		Optional<Role> roleOption = this.roles.stream()
				.filter(r -> code.equalsIgnoreCase(r.getCode()) && value.equalsIgnoreCase(r.getValue())).findFirst();
		if (roleOption.isPresent())
			this.role = roleOption.get();
		else
			throw new NoRoleException();
		Optional<IHRRole> hrRoleOption = this.hrRoles.stream()
				.filter(hr -> hr.getTemplate().equalsIgnoreCase(role.getCode())
						&& hr.getParameter().equalsIgnoreCase(role.getValue()))
				.findFirst();
		if (hrRoleOption.isPresent())
			this.hrRole = hrRoleOption.get();
		else
			throw new NoRoleException();
	}

	
	public void logout() throws UserConnectionException, IllegalStateException {
		this.hrUser.disconnect();
	}

	public HrUpdateCommitResult batchUpdate(String processus,  List<? extends ResourceBatchData> list, Integer nudoss) {
		HashMap<Integer, List<? extends ResourceBatchData>> dataMap = new HashMap<Integer, List<? extends ResourceBatchData>>();
		dataMap.put(nudoss, list);
		return this.batchUpdate(processus, dataMap);
	}
	
	
	private HrUpdateCommitResult batchUpdate(
					String processus, 
					Map<Integer, List<? extends ResourceBatchData>> dataMap) {
		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (dataMap == null) return result;

		// liste des cles a traiter
		Map<Integer, List<IHrEntity>> updateMap = new HashMap<Integer, List<IHrEntity>>();
		Map<Integer, List<IHrEntity>> deleteMap = new HashMap<Integer, List<IHrEntity>>();

		// liste des informations à traiter
		HashSet<String> informations = new HashSet<String>();
		// structure à traiter
		String structure = null;

		// construction de la liste des dossiers à traiter
		// et de la liste des informations à traiter
		for (Entry<Integer, List<? extends ResourceBatchData>> es: dataMap.entrySet()) {
			Integer nudoss = es.getKey();
			List<? extends ResourceBatchData> datas = es.getValue();
			
			for ( ResourceBatchData d: datas) {
				Map<Integer, List<IHrEntity>> map = null;
				if (d.getMethod() == ResourceBatchData.Method.PUT || d.getMethod() == ResourceBatchData.Method.POST)
					map = updateMap;
				if (d.getMethod() == ResourceBatchData.Method.DELETE)
					map = deleteMap;
				if (map == null)
					continue;
				IHrEntity e = d.getEntity();
	
				List<IHrEntity> occurs = map.get(nudoss);
				if (occurs == null) {
					occurs = new ArrayList<IHrEntity>();
					map.put(nudoss, occurs);
				}
				occurs.add(e);
	
					
				//TODO contrôler qu'il n'y a qu'une seule structure dans le flot
				structure = e.getMainStructure();				
				informations.add(e.getMainInformation());				
				
			};
		};

		Set<Integer> keys = new HashSet<Integer>();
		keys.addAll(updateMap.keySet());
		keys.addAll(deleteMap.keySet());


			

		HRDossierCollection collection;
		try {
			collection = this.initDossierCollection(processus, structure, new ArrayList<String>(informations));
		} catch (HRDossierCollectionException e) {
			// impossible d'initialiser la collection
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.INIT_COLLECTION, e.getMessage()));
			return result;
		}

		for (Integer nudoss: keys) {
		String select = "select nudoss from " + structure + "00 where nudoss = " + nudoss;
		HRDossierListIterator iterator;
		try {
			iterator = collection.loadDossiers(select);
		} catch (HRDossierCollectionException e) {
			e.printStackTrace();
			// impossible d'initialiser la collection
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.LOAD_COLLECTION, e.getMessage()));
			return result;
		}

		while (iterator.hasNext()) {

			// traitement d'un dossier hr
			HRDossier hrDossier = iterator.next();

			List<IHrEntity> deletedOccurs = deleteMap.get(hrDossier.getNudoss());

			if (deletedOccurs != null) {
				for (IHrEntity entity : deletedOccurs) {
					// Set information - Main Data
					HRDataSect dataSection = hrDossier.getDataSectionByName(entity.getMainInformation());

					boolean isMultiple = dataSection.isMultiple();

					HROccur hrOccur = null;
					if (isMultiple) {
						IHrMultipleEntity om = (IHrMultipleEntity) entity;
						// Récupération de l'occurrence
						if (om.getId() == null) { // création
							continue;
						} else { // modification
							hrOccur = dataSection.getOccurByNulign(om.getId());
							try {
								hrOccur.delete();
							} catch (HRDossierCollectionException e) {
								e.printStackTrace();
								result.setStatus(CommitStatus.KO);
								result.addTechnicalError(
										new TechnicalCommitError(TechnicalError.DELETE_ERROR, e.getMessage()));
								return result;
							}
						}
					} else {
						hrOccur = dataSection.getOccur();
						try {
							hrOccur.delete();
						} catch (HRDossierCollectionException e) {
							e.printStackTrace();
							result.setStatus(CommitStatus.KO);
							result.addTechnicalError(
									new TechnicalCommitError(TechnicalError.DELETE_ERROR, e.getMessage()));
							return result;
						}
					}
				}
			}

			List<IHrEntity> updatedOccurs = updateMap.get(hrDossier.getNudoss());

			// traitement des occurrences d'un dossier
			if (updatedOccurs != null) {
				for (IHrEntity o : updatedOccurs) {
					HRDataSect dataSection = hrDossier.getDataSectionByName(o.getMainInformation());
					boolean isMultiple = dataSection.isMultiple();

					// Recuperation de l'occurrence
					HROccur hrOccur = null;

					if (isMultiple) {
						IHrMultipleEntity om = (IHrMultipleEntity) o;
						if (om.getId() == null) { // creation
							HRKey k = new HRKey(om.getHrEntityKey());
							try {
								hrOccur = dataSection.createOccur(k);
							} catch (HRDossierCollectionException e) {
								// la creation de l'occurrence est en erreur
								e.printStackTrace();
								result.setStatus(CommitStatus.KO);
								result.addTechnicalError(
										new TechnicalCommitError(TechnicalError.CREATE_OCCUR, e.getMessage()));
								return result;
							}
						} else {
							hrOccur = dataSection.getOccurByNulign(om.getId());
							// l'occurrence n'existe plus
							if (hrOccur == null) {
								result.setStatus(CommitStatus.KO);
								result.addTechnicalError(new TechnicalCommitError(TechnicalError.UNKNOWN_OCCUR,
										hrDossier.getDossierId().toString()));
								return result;
							}
						}
					} else {
						hrOccur = dataSection.getOccur();
						if (hrOccur == null)
							try {
								hrOccur = dataSection.createOccur();
							} catch (HRDossierCollectionException e) {
								// la creation de l'occurrence est en erreur
								e.printStackTrace();
								result.setStatus(CommitStatus.KO);
								result.addTechnicalError(
										new TechnicalCommitError(TechnicalError.CREATE_OCCUR, e.getMessage()));
								return result;
							}
					}

					// Modification des valeurs
					for (Map.Entry<String, Object> entry : o.getHrEntityMap().entrySet()) {
						try {
							hrOccur.setValue(entry.getKey(), entry.getValue());
						} catch (HRDossierCollectionException e) {
							// erreur lors de la modification de la valeur
							e.printStackTrace();
							result.setStatus(CommitStatus.KO);
							result.addTechnicalError(
									new TechnicalCommitError(TechnicalError.BAD_DATA_FORMAT, e.getMessage()));
							return result;
						}
					}
				}
			}
		}
		}

		CommitResult r;
		try {
			r = collection.commitAllDossiers().getDossierCommitResult();
		} catch (HRDossierCollectionCommitException e) {
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.COMMIT_COLLECTION, e.getMessage()));
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


	public HrUpdateCommitResult update(String processus,  IHrEntity data, Integer nudoss) {		
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		 return this.batchUpdate(processus, list, nudoss);		
	}



	public HrUpdateCommitResult create(String processus,  IHrEntity data, Integer nudoss) {	
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		 return this.batchUpdate(processus, list, nudoss);		
	}
	
	public void delete(String processus, IHrEntity data, Integer nudoss) {
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.DELETE, data));
		this.batchUpdate(processus, list, nudoss);		
	}
	
	public HrUpdateCommitResult update(String processus,  IHrMultipleEntity data, Integer nudoss, Integer nulign) {		
		data.setId(nulign);
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		 return this.batchUpdate(processus, list, nudoss);		
	}

	public HrUpdateCommitResult create(String processus,  IHrMultipleEntity data, Integer nudoss) {	
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		 return this.batchUpdate(processus, list, nudoss);		
	}
	
	public void delete(String processus, String info, Integer nudoss, Integer nulign) {
		HrToDeleteOccur zf10 = new HrToDeleteOccur(info, nulign);
		ResourceBatchData<HrToDeleteOccur> data = new ResourceBatchData<HrToDeleteOccur>(Method.DELETE,zf10);
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(data);
		this.batchUpdate(processus, list, nudoss);		
	}

	private HRDossierCollection initDossierCollection(String processus, String structure, List<String> informations)
			throws HRDossierCollectionException {

		HRDossierCollectionParameters dossierCollectionParameters = new HRDossierCollectionParameters();
		dossierCollectionParameters.addDataSection(new HRDataSourceParameters.DataSection("00"));
		dossierCollectionParameters.setUpdateMode(UpdateMode.NORMAL);
		dossierCollectionParameters.setIgnoreSeriousWarnings(true);
		dossierCollectionParameters.setType(HRDossierCollectionParameters.TYPE_NORMAL);
		dossierCollectionParameters.setProcessName(processus);
		dossierCollectionParameters.setDataStructureName(structure);
		informations
				.forEach(i -> dossierCollectionParameters.addDataSection(new HRDataSourceParameters.DataSection(i)));

		// Instantiating a new dossier collection with given role, conversation and
		// configuration
		return new HRDossierCollection(dossierCollectionParameters, this.hrUser.getMainConversation(), hrRole,
				new HRDossierFactory(HRDossierFactory.TYPE_DOSSIER));

	}

	public List<ApplicationAccess> getApplicationAccess() {
		return applicationAccess;
	}

	public void setApplicationAccess(List<ApplicationAccess> applicationAccess) {
		this.applicationAccess = applicationAccess;
	}

	public boolean isConnected() {
		return this.hrUser.isConnected();
	}


}