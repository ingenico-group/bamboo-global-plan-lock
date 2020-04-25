package com.ingenico.bamboo.bpgl.impl;

import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.DEFAULT_GLOBAL_LOCK_KEY;
import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.GLOBAL_LOCK_ENABLED;
import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.GLOBAL_LOCK_KEY;

import java.util.Map;
import java.util.Optional;

import com.atlassian.bamboo.build.BuildDefinition;

public abstract class LockKeyUtils {

	private LockKeyUtils() {
		// nop
	}

	public static Optional<String> getLockKey(BuildDefinition buildDefinition) {
		Map<String, String> planConfiguration = buildDefinition.getCustomConfiguration();

		boolean hasLockEnabled = Boolean.valueOf(planConfiguration.getOrDefault(GLOBAL_LOCK_ENABLED, "false"));

		if (hasLockEnabled) {
			return Optional.of(planConfiguration.getOrDefault(GLOBAL_LOCK_KEY, DEFAULT_GLOBAL_LOCK_KEY));
		}

		return Optional.empty();
	}

}
