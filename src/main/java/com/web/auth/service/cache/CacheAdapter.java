package com.web.auth.service.cache;



public interface CacheAdapter<K, T> {
	public void cacheObject(K uniqueId,T objToBeCached);
	public T getCachedObject(K uniqueId);
	public void removeCache()  throws Exception;
    public void removeCache(K uniqueId);
}
