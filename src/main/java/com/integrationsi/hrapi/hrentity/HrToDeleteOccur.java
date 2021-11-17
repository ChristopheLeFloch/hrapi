package com.integrationsi.hrapi.hrentity;

public class HrToDeleteOccur extends HrUniqueOccur implements IHrMultipleEntity{


	private Integer id;


	public HrToDeleteOccur(String information,  Integer nulign) {
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
		return null;
	}
	


}
