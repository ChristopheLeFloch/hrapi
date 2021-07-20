package com.integrationsi.hrapi.hrentity;

import java.util.Map;

public class HrUniqueOccur implements IHrEntity{

	private int nudoss;
	private String information;
	private Map<String, Object> values;
	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}
	
	
	public HrUniqueOccur(String information, int nudoss) {
		this.information = information;
		this.nudoss = nudoss;
	}
	
	
	@Override
	public int getNudoss() {
		return nudoss;
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
	public Map<String, Object> getHrEntityMap() {
		return values;
	}


}
