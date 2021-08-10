package com.integrationsi.hrapi.hrentity;

public abstract class HrMultipleEntity  extends HrEntity implements IHrMultipleEntity {
	
	

	@Override
	public int getNudoss() {
		return Integer.parseInt(id.split("_")[0]);
	}
	
	@Override
	public int getNulign() {
		return Integer.parseInt(id.split("_")[1]);
	}




}
