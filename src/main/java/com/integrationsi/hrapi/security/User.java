package com.integrationsi.hrapi.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

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
import com.integrationsi.hrapi.hrentity.HrToDeleteOccur;
import com.integrationsi.hrapi.hrentity.IHrEntity;
import com.integrationsi.hrapi.hrentity.IHrMultipleEntity;

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

	public HrUpdateCommitResult batchUpdate(String processus, List<? extends ResourceBatchData> list, Integer nudoss)
			throws NullRoleException, NoSessionException, InvalidUpdateException {
		HashMap<Integer, List<? extends ResourceBatchData>> dataMap = new HashMap<Integer, List<? extends ResourceBatchData>>();
		dataMap.put(nudoss, list);
		return this.batchUpdate(processus, dataMap);
	}

	public HrUpdateCommitResult batchUpdate(String processus, Map<Integer, List<? extends ResourceBatchData>> dataMap)
			throws NullRoleException, NoSessionException, InvalidUpdateException {

		if (this.hrRole == null)
			throw new NullRoleException();
		if (!this.hrUser.isConnected())
			throw new NullRoleException();
		try {
			this.hrUser.isValid();
		} catch (Exception e) {
			throw new NoSessionException();
		}

		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (dataMap == null)
			return result;

		// liste des cles a traiter
		Map<Integer, List<IHrEntity>> createMap = new HashMap<Integer, List<IHrEntity>>();
		Map<Integer, List<IHrEntity>> updateMap = new HashMap<Integer, List<IHrEntity>>();
		Map<Integer, List<IHrEntity>> deleteMap = new HashMap<Integer, List<IHrEntity>>();

		// liste des informations à traiter
		HashSet<String> informations = new HashSet<String>();
		// structure à traiter
		String structure = null;

		// construction de la liste des dossiers à traiter
		// et de la liste des informations à traiter
		for (Entry<Integer, List<? extends ResourceBatchData>> es : dataMap.entrySet()) {
			Integer nudoss = es.getKey();
			List<? extends ResourceBatchData> datas = es.getValue();

			for (ResourceBatchData d : datas) {
				Map<Integer, List<IHrEntity>> map = null;
				switch (d.getMethod()) {
				case PUT:
					map = updateMap;
					break;
				case POST:
					map = createMap;
					break;
				case DELETE:
					map = deleteMap;
					break;
				default:
					throw new InvalidUpdateException(TechnicalError.UNKNOWN, "Méthode invalide: " + d.getMethod());

				}

				IHrEntity e = d.getEntity();

				List<IHrEntity> occurs = map.get(nudoss);
				if (occurs == null) {
					occurs = new ArrayList<IHrEntity>();
					map.put(nudoss, occurs);
				}
				occurs.add(e);

				// TODO contrôler qu'il n'y a qu'une seule structure dans le flot
				structure = e.getMainStructure();
				informations.add(e.getMainInformation());

			}
			;
		}
		;

		Set<Integer> keys = new HashSet<Integer>();
		keys.addAll(createMap.keySet());
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

		for (Integer nudoss : keys) {

			HRDossier hrDossier;

			try {
				if (nudoss == -1)
					hrDossier = collection.createDossier();
				else
					hrDossier = collection.loadDossier(nudoss);
			} catch (HRDossierCollectionException e) {
				// impossible d'initialiser la collection
				e.printStackTrace();
				result.setStatus(CommitStatus.KO);
				result.addTechnicalError(new TechnicalCommitError(TechnicalError.LOAD_COLLECTION, e.getMessage()));
				return result;
			}

			if (hrDossier == null) {
				// impossible d'initialiser la collection
				result.setStatus(CommitStatus.KO);
				result.addTechnicalError(
						new TechnicalCommitError(TechnicalError.LOAD_COLLECTION, "le dossier n'existe pas"));
				return result;
			}

			// traitement des suppresssions
			List<IHrEntity> deletedOccurs = deleteMap.get(nudoss);
			if (deletedOccurs != null) {
				for (IHrEntity entity : deletedOccurs) {
					try {
						this.deleteEntityFromDossier(hrDossier, entity);
					} catch (InvalidUpdateException e) {
						e.printStackTrace();
						result.setStatus(CommitStatus.KO);
						result.addTechnicalError(new TechnicalCommitError(TechnicalError.CREATE_OCCUR, e.getMessage()));
						return result;
					}
				}
			}

			List<IHrEntity> updatedOccurs = deleteMap.get(nudoss);
			if (updatedOccurs != null) {
				for (IHrEntity entity : updatedOccurs) {
					try {
						this.updateEntityFromDossier(hrDossier, entity);
					} catch (InvalidUpdateException e) {
						e.printStackTrace();
						result.setStatus(CommitStatus.KO);
						result.addTechnicalError(new TechnicalCommitError(TechnicalError.UPDATE_OCCUR, e.getMessage()));
						return result;
					}
				}
			}

			List<IHrEntity> createdOccurs = createMap.get(nudoss);
			if (createdOccurs != null) {
				for (IHrEntity entity : createdOccurs) {
					try {
						this.createEntityFromDossier(hrDossier, entity);
					} catch (InvalidUpdateException e) {
						e.printStackTrace();
						result.setStatus(CommitStatus.KO);
						result.addTechnicalError(new TechnicalCommitError(TechnicalError.DELETE_ERROR, e.getMessage()));
						return result;
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

	private HROccur updateEntityFromDossier(HRDossier hrDossier, IHrEntity entity) throws InvalidUpdateException {
		HROccur hrOccur = this.getOccur(hrDossier, entity);
		if (hrOccur == null)
			throw new InvalidUpdateException(TechnicalError.UNKNOWN_OCCUR);

		// Modification des valeurs
		for (Map.Entry<String, Object> entry : entity.getHrEntityMap().entrySet()) {
			try {
				hrOccur.setValue(entry.getKey(), entry.getValue());
			} catch (HRDossierCollectionException e) {
				throw new InvalidUpdateException(TechnicalError.BAD_DATA_FORMAT, e.getMessage());
			} catch (Exception e) {
				throw new InvalidUpdateException(TechnicalError.BAD_DATA_FORMAT, e.getMessage());
			}
		}

		return hrOccur;
	}

	private HROccur getOccur(HRDossier hrDossier, IHrEntity entity) {

		HRDataSect dataSection = hrDossier.getDataSectionByName(entity.getMainInformation());
		boolean isMultiple = dataSection.isMultiple();

		// Recuperation de l'occurrence
		HROccur hrOccur = null;

		if (isMultiple) {
			IHrMultipleEntity om = (IHrMultipleEntity) entity;
			if (om.getId() == null) {
				// on recupere l'occurrence depuis la clé fonctionnelle
			} else {
				// on récupère l'occurrence depuis le nulign,
				hrOccur = dataSection.getOccurByNulign(om.getId());
			}
		}

		return hrOccur;

	}

	private HROccur createEntityFromDossier(HRDossier hrDossier, IHrEntity entity) throws InvalidUpdateException {

		HRDataSect dataSection = hrDossier.getDataSectionByName(entity.getMainInformation());
		boolean isMultiple = dataSection.isMultiple();
		HROccur hrOccur;
		try {
			if (isMultiple) {
				IHrMultipleEntity entityM = (IHrMultipleEntity) entity;
				HRKey k = new HRKey(entityM.getHrEntityKey());
				hrOccur = dataSection.createOccur(k);
			} else {
				// attention lors de la creation d'un dossier, une occurrence de 00 est crée automatiquement
				// la ligne ci-dessous renvoit donc une ligne
				hrOccur = dataSection.getOccur();
				if (hrOccur == null) hrOccur = dataSection.createOccur();
			}
		} catch (HRDossierCollectionException e) {
			throw new InvalidUpdateException(TechnicalError.BAD_DATA_FORMAT, e.getMessage());
		}
		// Modification des valeurs
		for (Map.Entry<String, Object> entry : entity.getHrEntityMap().entrySet()) {
			try {
				hrOccur.setValue(entry.getKey(), entry.getValue());
			} catch (HRDossierCollectionException e) {
				throw new InvalidUpdateException(TechnicalError.BAD_DATA_FORMAT, e.getMessage());
			} catch (Exception e) {
				throw new InvalidUpdateException(TechnicalError.BAD_DATA_FORMAT, e.getMessage());

			}
		}
		return hrOccur;

	}

	private void deleteEntityFromDossier(HRDossier hrDossier, IHrEntity entity) throws InvalidUpdateException {
		// récupération de l'information hr
		HRDataSect dataSection = hrDossier.getDataSectionByName(entity.getMainInformation());
		boolean isMultiple = dataSection.isMultiple();

		// ocurrence à supprimer, si c'est une multiple, on charge à partir de l'id
		// sinon su supprime l'unique occurrence existente
		HROccur hrOccur = null;
		try {
			hrOccur.delete();
		} catch (HRDossierCollectionException e) {
			throw new InvalidUpdateException(TechnicalError.DELETE_ERROR, e.getMessage());
		}

	}

	public HrUpdateCommitResult update(String processus, IHrEntity data, Integer nudoss)
			throws NullRoleException, NoSessionException, InvalidUpdateException {
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		return this.batchUpdate(processus, list, nudoss);
	}

	public HrUpdateCommitResult create(String processus, IHrEntity data, Integer nudoss)
			throws NullRoleException, NoSessionException, InvalidUpdateException {
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		return this.batchUpdate(processus, list, nudoss);
	}

	public HrUpdateCommitResult delete(String processus, IHrEntity data, Integer nudoss)
			throws NullRoleException, NoSessionException, InvalidUpdateException {
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.DELETE, data));
		return this.batchUpdate(processus, list, nudoss);
	}

	public HrUpdateCommitResult update(String processus, IHrMultipleEntity data, Integer nudoss, Integer nulign)
			throws NullRoleException, NoSessionException, InvalidUpdateException {
		data.setId(nulign);
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		return this.batchUpdate(processus, list, nudoss);
	}

	public HrUpdateCommitResult create(String processus, IHrMultipleEntity data, Integer nudoss)
			throws NullRoleException, NoSessionException, InvalidUpdateException {
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(new ResourceBatchData(Method.PUT, data));
		return this.batchUpdate(processus, list, nudoss);
	}

	public HrUpdateCommitResult delete(String processus, String info, Integer nudoss, Integer nulign)
			throws NullRoleException, NoSessionException, InvalidUpdateException {
		HrToDeleteOccur zf10 = new HrToDeleteOccur(info, nulign);
		ResourceBatchData<HrToDeleteOccur> data = new ResourceBatchData<HrToDeleteOccur>(Method.DELETE, zf10);
		List<ResourceBatchData> list = new ArrayList<ResourceBatchData>();
		list.add(data);
		return this.batchUpdate(processus, list, nudoss);
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