package com.integrationsi.hrapi.hrentity;

import java.util.Map;

public class HrOccur implements HrEntity{

	private int nudoss;
	private String information;
	private Map<String, Object> values;
	private Object[] keys;
	
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

	@Override
	public Object[] getHrEntityKey() {
		return keys;
	}

}
