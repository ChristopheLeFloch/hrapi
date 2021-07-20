package com.integrationsi.hrapi.hrentity;

public class HrMultipleOccur extends HrUniqueOccur implements IHrMultipleEntity{



	private Object[] keys;
	public Object[] getKeys() {
		return keys;
	}

	public void setKeys(Object[] keys) {
		this.keys = keys;
	}

	@Override
	public Object[] getHrEntityKey() {
		return keys;
	}
	
	private int nulign;
	@Override
	public int getNulign() {
		return nulign;
	}
	
	
	public HrMultipleOccur(String information, int nudoss, Integer nulign) {
		super(information, nudoss);
		this.nulign = nulign;
	}
	

	



}
