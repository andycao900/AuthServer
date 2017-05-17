package com.web.auth.service.cache;

import java.util.ArrayList;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;


 
public class InMemoryCache<K, T>  implements CacheAdapter<K,T>{
 
    private long timeToLive;
    private LRUMap cacheMap;
    // Test with timeToLiveInSeconds = 100 seconds
    // timerIntervalInSeconds = 100 seconds
    // maxItems = 500000
    private final static InMemoryCache _instance = new InMemoryCache<String, Object>(100, 100, 500000);
 
    protected class CacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
 
        protected CacheObject(T value) {
            this.value = value;
        }
    }
 

    
    public static InMemoryCache getInstance()
    {
		return _instance;
	}
    
    public InMemoryCache(long timeToLive, final long timerInterval, int maxItems) {
        this.timeToLive = timeToLive * 1000;
 
        cacheMap = new LRUMap(maxItems);
 
        if (timeToLive > 0 && timerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(timerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
 
    public void cacheObject(K key, T value) {
        synchronized (cacheMap) {
            cacheMap.put(key, new CacheObject(value));
        }
    }
 
    public T getCachedObject(K key) {
        synchronized (cacheMap) {
            CacheObject c = (CacheObject) cacheMap.get(key);
 
            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }
 
    public void removeCache(K key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }
 
    /**
     * this has not been implimented at this point and needs to be looked into
     * to see if it should be part of the interface.
     * @throws java.lang.Exception
     */
    public void removeCache() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

     
    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
 
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (cacheMap) {
            MapIterator itr = cacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
            K key = null;
            CacheObject c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (CacheObject) itr.getValue();
 
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }
}