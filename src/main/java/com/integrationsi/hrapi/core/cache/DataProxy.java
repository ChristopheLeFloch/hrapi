package com.integrationsi.hrapi.core.cache;

import java.util.ArrayList;
import java.util.List;



public abstract class DataProxy<T extends Key> {
	
	boolean useCache = false;
	
	final Cache<T> cache = new Cache<T>();
	
	
	public DataProxy() {
	
	
	}
	
	
	
	/**
	 * Récupérer une donnée 
	 * @param key
	 * @return
	 */
	public T getData(String o) {
		T d = this.cache.getData(o);
		if (d == null) {			
			d =this.extractData(o);			
			if (d!= null) this.cache.setData(d);
		}
		return d;
		
	}
	
	/**
	 * Récupérer une liste de données.
	 * La récupération des données depuis le cache est tenté, puis  pour les références manquantes,
	 * on utilise le DataProvider fournit en constructeur pour récupérer les données
	 * @param key
	 * @return
	 */
	public List<T> getDatas(List<String> oList) {
		
		// Initialisation de la liste finale retournée
		List<T> al = new ArrayList<T>();
		
		if (useCache) {
			//Initialisation de la liste des données absentes du cache
			List<String> missingDataList = new ArrayList<String>();
			
			//
			oList.forEach((o) -> {
				T d = this.cache.getData(o);
				if (d == null ){
					missingDataList.add(o);
				}	else {
					al.add(d);					
				}
			});
			
			//Des données n'ont pas été récupérées depuis le cache
			//On les récupère depuis la base et on les ajoute au cache
			if (! missingDataList.isEmpty()) {		
				List<T> l = this.extractDatas(missingDataList);
				this.cache.setDatas(l);
				al.addAll(this.extractDatas(missingDataList));		
			}
			
		} else {
			al.addAll(this.extractDatas(oList));		
		}
		
		return al;
		
	}
	
	
	/**
	 * Récupérer une liste de données pour un subquery
	 * @param key
	 * @return
	 */
	public List<T> getDatas(String subquery) {
		List<String> keys = this.extractKeysFromQuery(subquery);
		return this.getDatas(keys);
		
		
	}

	
	protected abstract List<T> extractDatas(List<String> oList);
	
	protected abstract T extractData(String o);
	
	protected abstract List<String> extractKeysFromQuery(String query);
	
	protected abstract String getRequest();
	
	protected abstract String getCdstdo();
	
	}
