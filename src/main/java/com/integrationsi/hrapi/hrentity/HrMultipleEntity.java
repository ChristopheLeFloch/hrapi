package com.integrationsi.hrapi.hrentity;

public abstract class HrMultipleEntity  extends HrEntity implements IHrMultipleEntity {
	
	

	@Override
	public int getNudoss() {
		String id = getId();
		if (id == null) return -1;		
		return Integer.parseInt(id.split("_")[0]);
	}
	
	@Override
	public int getNulign() {
		String id = getId();
		if (id == null) return -1;		
		return Integer.parseInt(id.split("_")[1]);
	}




}
