package com.integrationsi.hrapi.hrentity;

public abstract class HrMultipleEntity  extends HrEntity implements IHrMultipleEntity {
	
	protected Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String convertNull(String s) {
		if (s == null) return "";
		return s;
	}
	
	public Integer convertNull(Integer i) {
		if (i == null) return 0;
		return i;
	}

	public Boolean convertNull(Boolean b) {
		if (b == null) return false;
		return b;
	}
}
