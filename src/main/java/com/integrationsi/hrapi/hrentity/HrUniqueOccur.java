package com.integrationsi.hrapi.hrentity;

import java.util.HashMap;
import java.util.Map;

public class HrUniqueOccur implements IHrEntity{


	private String information;
	

	private Map<String, Object> hrEntityMap;
	

	public HrUniqueOccur() {

	}
	
	public HrUniqueOccur(String information) {
		this.information = information;

	}
	

	@Override
	public String getMainStructure() {
		return information.substring(0,2);
	}

	@Override
	public String getMainInformation() {
		return information.substring(2,4);
	}


	public Map<String, Object> setHrEntityMap() {
		return hrEntityMap;
	}
	
	@Override
	public Map<String, Object> getHrEntityMap() {
		return hrEntityMap;
	}
	
	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public HrUniqueOccur create() {
		this.hrEntityMap = new HashMap<String, Object>();
		return this;
	}

	
	public HrUniqueOccur addValues(String key, Object value) {
		this.hrEntityMap.put(key, value);
		return this;
	}


}
