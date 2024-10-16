package org.atum.jvcp.cache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.EcmRequest;

/**
 * A cam message exchange based upon the CSP cache implementation. Allows the
 * querying of ECM's via reusing DCW's supplied by readers for multiple clients.
 * 
 * Developer Note: Due to some strange casting issue map value must be type T. Some
 * Basic performance testing using 8 threads on a cache of 8000 - 80000
 * suggested no latency increase as the cache grows and seek times remain
 * competitive compared to {@link HashMap} while cache cleanup is simplified as
 * removeEldestEntry is called for every add into the map.
 * 
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 8 Dec 2016 21:11:04
 */
@SuppressWarnings("rawtypes")
public class ClusteredCache extends LinkedHashMap implements CacheExchangeInterface {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(ClusteredCache.class);
	private static final long serialVersionUID = 4261198925084345053L;
	private long timeout;

	public ClusteredCache() {
		timeout = 20000L;
	}

	public ClusteredCache(long timeout) {
		this.timeout = timeout;
	}

	protected boolean removeEldestEntry(Map.Entry eldest) {
		long now = System.currentTimeMillis();
		EcmRequest req = (EcmRequest) eldest.getValue();
		if (now - req.getTimestamp() > timeout) {
			return true;
		}
		return false;
	}

	public synchronized EcmRequest peekCache(int ecmHash) {
		EcmRequest answer = (EcmRequest) this.get(ecmHash);
		if (answer == null) {
			// add listener to map?

		}
		return answer;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addEntry(int ecmHash,EcmRequest dcw){
		synchronized(this){
			@SuppressWarnings("unused")
			EcmRequest previousEntry = (EcmRequest) this.put(ecmHash, dcw);
		}
		return true;
	}

	/**
	 * @param ecmHash Removes an ECM request with a specified ecmHash/cspHash
	 */
	public synchronized void removeRequest(int ecmHash) {
		this.remove(ecmHash);
	}

}
