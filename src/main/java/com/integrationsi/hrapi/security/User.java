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
import com.integrationsi.hrapi.commit.ResourceBulkData;
import com.integrationsi.hrapi.commit.TechnicalCommitError;
import com.integrationsi.hrapi.commit.TechnicalError;
import com.integrationsi.hrapi.hrentity.HrEntity;
import com.integrationsi.hrapi.hrentity.HrOccur;
import com.integrationsi.hrapi.util.SqlUtils;

/**
 * Modélise un utilisateur Hr Access. Par rapport à l'interface standard openHr,
 * celle-ci à une interface simplifiée.
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

	protected User(IHRUser hrUser) {
		this.code = hrUser.getUserId();
		this.label = hrUser.getLabel();
		roles = new ArrayList<Role>();
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
		Optional<IHRRole> optionRole = this.hrRoles.stream()
				.filter(hr -> hr.getTemplate() == role.getCode() && hr.getParameter() == role.getValue()).findFirst();
		if (optionRole.isPresent())
			this.hrRole = optionRole.get();
	}

	/**
	 * Mise à jour en masse de données Hr Access. La mise à jour est réalisée avec
	 * le processus passé en paramètre pour la liste de données fournies. Les
	 * données peuvent appartenir à des dossiers différents et peuvent contenir des
	 * informations distinctes. Si une erreur technique survient, l'ensemble des
	 * mises à jour est en échec. Si des erreurs fonctionnelles surviennent sur un
	 * dossier, seul ce dossier est en erreur.
	 * 
	 * @param processus
	 * @param bulkData
	 * @return
	 */
	public HrUpdateCommitResult bulkUpdate(String processus, List<ResourceBulkData> bulkDatas) {

		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (bulkDatas.size() == 0)
			return result;

		// liste des cles a traiter
		Map<Integer, List<HrEntity>> updateMap = new HashMap<Integer, List<HrEntity>>();
		Map<Integer, List<HrEntity>> deleteMap = new HashMap<Integer, List<HrEntity>>();

		// liste des informations à traiter
		HashSet<String> informations = new HashSet<String>();
		// structure à traiter
		String structure = bulkDatas.get(0).getEntity().getMainStructure();

		// construction de la liste des dossiers à traiter
		// et de la liste des informations à traiter
		bulkDatas.forEach((d) -> {
			Map<Integer, List<HrEntity>> map = null;
			if (d.getMethod() == ResourceBulkData.Method.PUT || d.getMethod() == ResourceBulkData.Method.POST
					|| d.getMethod() == ResourceBulkData.Method.CREATE)
				map = updateMap;
			if (d.getMethod() == ResourceBulkData.Method.PUT || d.getMethod() == ResourceBulkData.Method.POST
					|| d.getMethod() == ResourceBulkData.Method.CREATE)
				map = deleteMap;
			if (map == null)
				return;
			HrEntity e = d.getEntity();

			List<HrEntity> occurs = map.get(e.getNudoss());
			if (occurs == null) {
				occurs = new ArrayList<HrEntity>();
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

			List<HrEntity> deletedOccurs = updateMap.get(hrDossier.getNudoss());

			if (deletedOccurs != null) {
				for (HrEntity entity : deletedOccurs) {
					// Set information - Main Data
					HRDataSect dataSection = hrDossier.getDataSectionByName(entity.getMainInformation());

					// Récupération de l'occurrence
					HROccur hrOccur = null;
					if (entity.getNulign() == -1) { // création
						continue;
					} else { // modification
						hrOccur = dataSection.getOccurByNulign(entity.getNulign());
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

			List<HrEntity> updatedOccurs = updateMap.get(hrDossier.getNudoss());

			// traitement des occurrences d'un dossier
			if (updatedOccurs != null) {
				for (HrEntity o : updatedOccurs) {
					HRDataSect dataSection = hrDossier.getDataSectionByName(o.getMainInformation());

					// Recuperation de l'occurrence
					HROccur hrOccur = null;
					if (o.getNulign() == -1) { // creation
						HRKey k = new HRKey(o.getHrEntityKey());
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
						hrOccur = dataSection.getOccurByNulign(o.getNulign());
						// l'occurrence n'existe plus
						if (hrOccur == null) {
							result.setStatus(CommitStatus.KO);
							result.addTechnicalError(new TechnicalCommitError(TechnicalError.UNKNOWN_OCCUR,
									hrDossier.getDossierId().toString()));
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
	 * Mise à jour d'une donnée Hr Access. La mise à jour est réalisée avec le
	 * processus passé en paramètre.
	 * 
	 * @param processus
	 * @param data
	 * @return
	 */
	public HrUpdateCommitResult updateOccur(String processus, HrEntity data) {

		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (data == null)
			return result;

		// liste des informations à traiter
		HashSet<String> informations = new HashSet<String>();
		// structure à traiter
		String structure = data.getMainStructure();

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
	 * Mise à jour de données Hr Access. La mise à jour est réalisée avec le
	 * processus passé en paramètre pour la liste de données fournies. Les données
	 * doivent appartenir à une seule information et à un seul dossier.
	 * 
	 * @param processus
	 * @param data
	 * @return
	 */
	public HrUpdateCommitResult updateOccurs(String processus, List<? extends HrEntity> datas) {

		HrUpdateCommitResult result = new HrUpdateCommitResult();

		if (datas == null || datas.size() == 0)
			return result;

		// liste des informations à traiter
		HashSet<String> informations = new HashSet<String>();
		// structure à traiter
		String structure = datas.get(0).getMainStructure();

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

		HRDossier hrDossier;
		try {
			hrDossier = collection.loadDossier(datas.get(0).getNudoss());
		} catch (HRDossierCollectionException e) {
			e.printStackTrace();
			// impossible d'initialiser la collection
			e.printStackTrace();
			result.setStatus(CommitStatus.KO);
			result.addTechnicalError(new TechnicalCommitError(TechnicalError.LOAD_COLLECTION, e.getMessage()));
			return result;
		}

		// traitement des occurrences d'un dossier
		HRDataSect dataSection = hrDossier.getDataSectionByName(datas.get(0).getMainInformation());

		for (HrEntity data : datas) {

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
					result.addTechnicalError(new TechnicalCommitError(TechnicalError.UNKNOWN_OCCUR,
							hrDossier.getDossierId().toString()));
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
		return new HRDossierCollection(dossierCollectionParameters, this.conversation, hrRole,
				new HRDossierFactory(HRDossierFactory.TYPE_DOSSIER));

	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

}