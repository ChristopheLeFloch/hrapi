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
import com.integrationsi.hrapi.application.ApplicationAccess;
import com.integrationsi.hrapi.commit.CommitStatus;
import com.integrationsi.hrapi.commit.HrUpdateCommitResult;
import com.integrationsi.hrapi.commit.ResourceBatchData;
import com.integrationsi.hrapi.commit.TechnicalCommitError;
import com.integrationsi.hrapi.commit.TechnicalError;
import com.integrationsi.hrapi.hrentity.IHrEntity;
import com.integrationsi.hrapi.hrentity.IHrMultipleEntity;
import com.integrationsi.hrapi.util.SqlUtils;

/**
 * Mod�lise un utilisateur Hr Access. Par rapport � l'interface standard openHr,
 * celle-ci � une interface simplifi�e.
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

	public void setRole(Role role) {
		this.role = role;
		Optional<IHRRole> optionRole = this.hrRoles.stream()
				.filter(hr -> hr.getTemplate() == role.getCode() && hr.getParameter() == role.getValue()).findFirst();
		if (optionRole.isPresent())
			this.hrRole = optionRole.get();
	}

	/**
	 * Mise � jour en masse de donn�es Hr Access. La mise � jour est r�alis�e avec
	 * le processus pass� en param�tre. Les donn�es peuvent appartenir � des
	 * dossiers diff�rents et peuvent contenir des informations distinctes. Si une
	 * erreur survient, l'ensemble des mises � jour est en �chec.
	 * 
	 * @param processus
	 * @param bulkData
	 * @return
	 */
	public HrUpdateCommitResult batchUpdate(String processus, List<ResourceBatchData> batchkDatas) {

		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (batchkDatas.size() == 0)
			return result;

		// liste des cles a traiter
		Map<Integer, List<IHrEntity>> updateMap = new HashMap<Integer, List<IHrEntity>>();
		Map<Integer, List<IHrEntity>> deleteMap = new HashMap<Integer, List<IHrEntity>>();

		// liste des informations � traiter
		HashSet<String> informations = new HashSet<String>();
		// structure � traiter
		String structure = batchkDatas.get(0).getEntity().getMainStructure();

		// construction de la liste des dossiers � traiter
		// et de la liste des informations � traiter
		batchkDatas.forEach((d) -> {
			Map<Integer, List<IHrEntity>> map = null;
			if (d.getMethod() == ResourceBatchData.Method.PUT || d.getMethod() == ResourceBatchData.Method.POST
					|| d.getMethod() == ResourceBatchData.Method.CREATE)
				map = updateMap;
			if (d.getMethod() == ResourceBatchData.Method.PUT || d.getMethod() == ResourceBatchData.Method.POST
					|| d.getMethod() == ResourceBatchData.Method.CREATE)
				map = deleteMap;
			if (map == null)
				return;
			IHrEntity e = d.getEntity();

			List<IHrEntity> occurs = map.get(e.getNudoss());
			if (occurs == null) {
				occurs = new ArrayList<IHrEntity>();
				map.put(e.getNudoss(), occurs);
			}
			occurs.add(e);
			informations.add(e.getMainInformation());
		});

		List<Integer> keys = new ArrayList<Integer>(updateMap.keySet());
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

		String select = "select nudoss from ZY00 where nudoss in " + SqlUtils.getSqlNudossList(keys);
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

			List<IHrEntity> deletedOccurs = updateMap.get(hrDossier.getNudoss());

			if (deletedOccurs != null) {
				for (IHrEntity entity : deletedOccurs) {
					// Set information - Main Data
					HRDataSect dataSection = hrDossier.getDataSectionByName(entity.getMainInformation());

					boolean isMultiple = dataSection.isMultiple();

					HROccur hrOccur = null;
					if (isMultiple) {
						IHrMultipleEntity om = (IHrMultipleEntity) entity;
						// R�cup�ration de l'occurrence
						if (om.getNulign() == -1) { // cr�ation
							continue;
						} else { // modification
							hrOccur = dataSection.getOccurByNulign(om.getNulign());
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
						if (om.getNulign() == -1) { // creation
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
							hrOccur = dataSection.getOccurByNulign(om.getNulign());
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

	/**
	 * Mise � jour d'une information multiple Hr Access. La mise � jour est r�alis�e
	 * avec le processus pass� en param�tre.
	 * 
	 * @param processus
	 * @param data
	 * @return
	 */
	public HrUpdateCommitResult updateMultipleOccur(String processus, IHrMultipleEntity data) {

		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (data == null)
			return result;

		// liste des informations � traiter
		List<String> informations = new ArrayList<String>();
		informations.add("00");
		informations.add(data.getMainInformation());
		// structure � traiter
		String structure = data.getMainStructure();

		HRDossierCollection collection;
		try {
			collection = this.initDossierCollection(processus, structure, informations);
		} catch (HRDossierCollectionException e) {
			// impossible d'initialiser la collection
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.INIT_COLLECTION, e.getMessage()));
			return result;
		}

		HRDossier hrDossier;
		try {
			hrDossier = collection.loadDossier(data.getNudoss());
		} catch (HRDossierCollectionException e) {
			e.printStackTrace();
			// impossible d'initialiser la collection
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.LOAD_COLLECTION, e.getMessage()));
			return result;
		}

		// traitement des occurrences d'un dossier
		HRDataSect dataSection = hrDossier.getDataSectionByName(data.getMainInformation());

		// Recuperation de l'occurrence
		HROccur hrOccur = null;
		if (data.getNulign() == -1) { // creation
			HRKey k = new HRKey(data.getHrEntityKey());
			try {
				hrOccur = dataSection.createOccur(k);
			} catch (HRDossierCollectionException e) {
				// la creation de l'occurrence est en erreur
				e.printStackTrace();
				result.setStatus(CommitStatus.KO);
				result.addTechnicalError(new TechnicalCommitError(TechnicalError.CREATE_OCCUR, e.getMessage()));
				return result;
			}
		} else {
			hrOccur = dataSection.getOccurByNulign(data.getNulign());
			// l'occurrence n'existe plus
			if (hrOccur == null) {
				result.setStatus(CommitStatus.KO);
				result.addTechnicalError(
						new TechnicalCommitError(TechnicalError.UNKNOWN_OCCUR, hrDossier.getDossierId().toString()));
				return result;
			}
		}
		// Modification des valeurs
		for (Map.Entry<String, Object> entry : data.getHrEntityMap().entrySet()) {
			try {
				hrOccur.setValue(entry.getKey(), entry.getValue());
			} catch (HRDossierCollectionException e) {
				// erreur lors de la modification de la valeur
				e.printStackTrace();
				result.setStatus(CommitStatus.KO);
				result.addTechnicalError(new TechnicalCommitError(TechnicalError.BAD_DATA_FORMAT, e.getMessage()));
				return result;
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

	/**
	 * Mise � jour d'une information unique Hr Access. La mise � jour est r�alis�e
	 * avec le processus pass� en param�tre.
	 * 
	 * @param processus
	 * @param data
	 * @return
	 */
	public HrUpdateCommitResult updateUniueOccur(String processus, IHrEntity data) {

		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (data == null)
			return result;

		// liste des informations � traiter
		List<String> informations = new ArrayList<String>();
		informations.add("00");
		informations.add(data.getMainInformation());
		// structure � traiter
		String structure = data.getMainStructure();

		HRDossierCollection collection;
		try {
			collection = this.initDossierCollection(processus, structure, informations);
		} catch (HRDossierCollectionException e) {
			// impossible d'initialiser la collection
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.INIT_COLLECTION, e.getMessage()));
			return result;
		}

		HRDossier hrDossier;
		try {
			hrDossier = collection.loadDossier(data.getNudoss());
		} catch (HRDossierCollectionException e) {
			e.printStackTrace();
			// impossible d'initialiser la collection
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.LOAD_COLLECTION, e.getMessage()));
			return result;
		}

		// traitement des occurrences d'un dossier
		HRDataSect dataSection = hrDossier.getDataSectionByName(data.getMainInformation());

		// Recuperation de l'occurrence
		HROccur hrOccur = dataSection.getOccur();
		if (hrOccur == null) { // creation
			try {
				hrOccur = dataSection.createOccur();
			} catch (HRDossierCollectionException e) {
				// la creation de l'occurrence est en erreur
				e.printStackTrace();
				result.setStatus(CommitStatus.KO);
				result.addTechnicalError(new TechnicalCommitError(TechnicalError.CREATE_OCCUR, e.getMessage()));
				return result;
			}
		}
		// Modification des valeurs
		for (Map.Entry<String, Object> entry : data.getHrEntityMap().entrySet()) {
			try {
				hrOccur.setValue(entry.getKey(), entry.getValue());
			} catch (HRDossierCollectionException e) {
				// erreur lors de la modification de la valeur
				e.printStackTrace();
				result.setStatus(CommitStatus.KO);
				result.addTechnicalError(new TechnicalCommitError(TechnicalError.BAD_DATA_FORMAT, e.getMessage()));
				return result;
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