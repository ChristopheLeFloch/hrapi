package com.integrationsi.hrapi.core.cache;

import java.util.HashMap;
import java.util.List;


/**
 * Un cache de donn�ees est une map qui associe une cl� � une valeur.
 * @author CLEFL
 *
 * @param <T>
 */
public class Cache <T extends Cacheable> {
	
	public  final HashMap<String,T> cache = new HashMap<String,T>();
	
	
	/**
	 * R�cup�rer une donn�e du cache
	 * @param key
	 * @return
	 */
	public T getData(String key) {
		return this.cache.get(key);
	}
	
	
	/**
	 * Ajouter une donn�e au cache
	 * @param o
	 */
	public void setData(T o) {
		 	cache.put(o.getKey(), o);
	}
	

	/**
	 * Ajouter une donn�e au cache
	 * @param o
	 */
	public void setDatas(List<T> oList) {
		oList.forEach( (o) -> {
		 	cache.put(o.getKey(), o);
		});
	}
	

}
