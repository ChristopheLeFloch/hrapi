package com.integrationsi.hrapi.hrentity;

import java.util.Map;




/**
 * Cette classe abstraite modélise une entité Hr manipulée en tant que table.
 * L'entité est principalement liée à une information Hr mais peut contenir d'autres informations secondaires.
 * L'information principale peut-être mise à jour mais pas les autres informations secondaires.
 * @author xohd685
 *
 */
public  interface HrEntity  {
	
	

	int getNudoss();		

	String getMainStructure();	
	String getMainInformation();
	
	public abstract Map<String, Object> getHrEntityMap();
	
	public abstract Object[] getHrEntityKey();


}
