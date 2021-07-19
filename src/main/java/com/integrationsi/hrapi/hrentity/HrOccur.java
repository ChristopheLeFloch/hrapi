package com.integrationsi.hrapi.hrentity;

import java.util.Map;

public class HrOccur implements HrEntity{

	private int nudoss;
	private String information;
	private Map<String, Object> values;
	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	private Object[] keys;
	public Object[] getKeys() {
		return keys;
	}

	public void setKeys(Object[] keys) {
		this.keys = keys;
	}

	private int nulign;
	
	
	public HrOccur(String information, int nudoss, Integer nulign) {
		this.information = information;
		this.nudoss = nudoss;
		this.nulign = nulign;
	}
	
	@Override
	public int getNulign() {
		return nulign;
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

	@Override
	public Object[] getHrEntityKey() {
		return keys;
	}

}
