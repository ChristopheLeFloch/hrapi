package com.integrationsi.hrapi.hrentity;

import java.util.Map;

public abstract class HrEntity implements IHrEntity {
	
	protected String information;
	protected String id;
	

	@Override
	public int getNudoss() {
		return Integer.parseInt(id);
	}

	@Override
	public String getMainStructure() {
		return information.substring(2,2);
	}

	@Override
	public String getMainInformation() {
		return information.substring(0,2);
	}

	@Override
	abstract public Map<String, Object> getHrEntityMap();
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;	
	}

}
