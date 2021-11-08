package com.integrationsi.hrapi.hrentity;

public class HrMultipleOccur extends HrUniqueOccur implements IHrMultipleEntity{


	private Integer id;

	private Object[] keys;
	public Object[] getKeys() {
		return keys;
	}

	public void setKeys(Object[] keys) {
		this.keys = keys;
	}

	
	public HrMultipleOccur(String information,  Integer nulign) {
		super(information);
		this.id = nulign;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public Object[] getHrEntityKey() {
		// TODO Auto-generated method stub
		return null;
	}
	


}
