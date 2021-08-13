package com.integrationsi.hrapi.commit;


import com.integrationsi.hrapi.hrentity.IHrEntity;

public class ResourceBatchData <T extends IHrEntity> {

	public ResourceBatchData() {}
	
	public ResourceBatchData(Method method, T entity) {
		super();
		this.method = method;
		this.entity = entity;
	}

	private Method method;
	private T entity;
	
	
	public T getEntity() {
		return entity;
	}
	public void setEntity(T entity) {
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
