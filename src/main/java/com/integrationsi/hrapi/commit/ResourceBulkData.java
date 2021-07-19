package com.integrationsi.hrapi.commit;

import com.integrationsi.hrapi.hrentity.HrEntity;

public class ResourceBulkData {

	private Method method;
	private HrEntity entity;
	
	
	public HrEntity getEntity() {
		return entity;
	}
	public void setEntity(HrEntity entity) {
		this.entity = entity;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	} 
	
	public enum Method {
		POST("POST"),
		DELETE("DELETE"),
		PUT("PUT"),
		CREATE("CREATE");
		
		String label;
		
		Method(String l) {
			this.label=l;
		}
		
		public String toString() {
			return this.label;
		}
	}
	
}
