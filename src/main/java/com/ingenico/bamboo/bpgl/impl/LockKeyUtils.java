package com.ingenico.bamboo.bpgl.impl;

import com.atlassian.bamboo.build.BuildDefinition;

import java.util.Map;
import java.util.Optional;

import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.DEFAULT_GLOBAL_LOCK_KEY;
import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.GLOBAL_LOCK_ENABLED;
import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.GLOBAL_LOCK_KEY;

public final class LockKeyUtils {

    private LockKeyUtils() {
        // nop
    }

    public static Optional<String> getLockKey(final BuildDefinition buildDefinition) {
        final Map<String, String> planConfiguration = buildDefinition.getCustomConfiguration();

        final boolean hasLockEnabled = Boolean.parseBoolean(planConfiguration.getOrDefault(GLOBAL_LOCK_ENABLED, "false"));

        if (hasLockEnabled) {
            return Optional.of(planConfiguration.getOrDefault(GLOBAL_LOCK_KEY, DEFAULT_GLOBAL_LOCK_KEY));
        }

        return Optional.empty();
    }

}
