package com.snw.cache;

/**
 * This exception is thrown when the json serializer encounters a cached object that has a different
 * serial versionUID than the one in the current class loader.
 */
public class CachedSerialVersionMisMatch extends Exception {
	private static final long serialVersionUID = 1L;

	public CachedSerialVersionMisMatch(String className, long cachedVersionUid, long currentVersionUid) {
		super("Class [" + className + "] : Cached Version [ " + cachedVersionUid + "], Current Version [" + currentVersionUid + "]");
	}

}
