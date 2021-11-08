package com.integrationsi.hrapi.hrentity;

import java.util.Map;

public abstract class HrEntity implements IHrEntity {
	


	@Override
	public String getMainStructure() {
		return getInformation().substring(0,2);
	}

	@Override
	public String getMainInformation() {
		return getInformation().substring(2,4);
	}
	
	public abstract String getInformation() ;
	
	
	@Override
	abstract public Map<String, Object> getHrEntityMap();
	


}
