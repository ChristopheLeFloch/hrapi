package com.integrationsi.hrapi.hrentity;

import java.util.Map;




/**
 * Cette classe abstraite modelise une entitee Hr manipulee en tant que table.
 * L'entite est liee a une information Hr principale mais peut contenir d'autres informations secondaires.
 * L'information principale peut-etre mise a  jour mais pas les autres informations secondaires.
 *
 */
public  interface IHrEntity  {
	
		


	String getMainStructure();	
	String getMainInformation();
	
	Map<String, Object> getHrEntityMap();



}
