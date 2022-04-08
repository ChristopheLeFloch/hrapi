package com.integrationsi.hrapi.hrentity;

public class HrMultipleOccur extends HrUniqueOccur implements IHrMultipleEntity{


	private Integer id;

	private Object[] hrEntityKey;


	public HrMultipleOccur() {

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
		return hrEntityKey;
	}
	
	public void setHrEntityKey(Object[] hrEntityKey) {
		this.hrEntityKey = hrEntityKey;
	}

	@Override
	public HrMultipleOccur create() {
		super.create();
		return this;
	}

	@Override
	public HrMultipleOccur addValues(String key, Object value) {
		super.addValues(key, value);
		return this;
	}

}
