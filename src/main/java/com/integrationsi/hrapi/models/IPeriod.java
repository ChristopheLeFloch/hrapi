package com.integrationsi.hrapi.models;

import java.sql.Date;


/**
 * Interface des modèles de données ayant une période délimitée par une date de début et une date de fin.
 * @author xohd685
 *
 */
public interface IPeriod {
	
	public Date getStartDate() ;
	
	public Date getEndDate() ;

}
