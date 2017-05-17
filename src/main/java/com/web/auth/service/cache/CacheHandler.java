package com.web.auth.service.cache;



public class CacheHandler {

	static CacheAdapter _adapter = null;
	public final static CacheHandler _instance = new CacheHandler();
	
	public static CacheHandler getInstance() { 
		return _instance; 
	} 

	// default constructor 
	private CacheHandler() { 	
        //removing this until we figure out what is wrong with WAS clustering
		//_adapter = JBossCacheAdapter.getInstance();
        //@TODO need to move this to a factory or at least pull from
        // properties
        _adapter = InMemoryCache.getInstance();
	}

	// clone 
	protected Object clone() throws CloneNotSupportedException { 
		throw new CloneNotSupportedException(); 
	} 

	public CacheAdapter getCacheAdapterImpl(){
		return _adapter;		  
	}

	

}
