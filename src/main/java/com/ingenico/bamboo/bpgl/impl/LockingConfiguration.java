package com.ingenico.bamboo.bpgl.impl;

public interface LockingConfiguration {

	public final static String GLOBAL_LOCK_ENABLED = "custom.com.ingenico.bamboo.bpgl.globalLock_enabled";
	public final static String GLOBAL_LOCK_KEY = "custom.com.ingenico.bamboo.bpgl.globalLock_key";
	public final static String DEFAULT_GLOBAL_LOCK_KEY = "1b7f0382bb464bd0a2baf751641eb31379281cc575e34fa19004a1e7cc37d8b7e3b5083acea940fb9540f5208468c619";

	final int LOCK_AQUIRE_RETRY_DELAY_SECONDS = 15;
}
