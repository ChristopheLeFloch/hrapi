package com.integrationsi.hrapi.hrentity;

/**
 * Cette classe abstraite modélise une entité Hr manipulée en tant que table.
 * L'entité est principalement liée à une information Hr mais peut contenir d'autres informations secondaires.
 * L'information principale peut-être mise à jour mais pas les autres informations secondaires.
 * @author xohd685
 *
 */
public  interface IHrMultipleEntity extends IHrEntity {
	
	int getNulign();

	
	Object[] getHrEntityKey();
}
