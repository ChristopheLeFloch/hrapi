package com.integrationsi.hrapi.hrentity;

/**
 * Cette classe abstraite modelise une entite Hr manipulee en tant que table.
 * L'entite est liee a une information Hr principale mais peut contenir d'autres informations secondaires.
 * L'information principale peut-etre mise Ã  jour mais pas les autres informations secondaires.
 *
 */
public  interface IHrMultipleEntity extends IHrEntity {
	
	Integer getId();
	void setId(Integer id); 
	
	Object[] getHrEntityKey();
}
