package com.integrationsi.hrapi.hrentity;

import java.util.HashMap;
import java.util.Map;

public class HrUniqueOccur implements IHrEntity{


	private String information;
	private Map<String, Object> values;
	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
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

	@Override
	public Map<String, Object> getHrEntityMap() {
		return values;
	}
	
	public HrUniqueOccur create() {
		this.values = new HashMap<String, Object>();
		return this;
	}

	
	public HrUniqueOccur addValues(String key, Object value) {
		this.values.put(key, value);
		return this;
	}


}
