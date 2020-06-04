package com.ingenico.bamboo.bpgl.impl;

import com.atlassian.bamboo.plan.PlanKey;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RunningPlans {

    private static final ConcurrentHashMap<String, PlanKey> plansExecutingForLocks = new ConcurrentHashMap<>();

    private RunningPlans() {
        // nop
    }

    public static Optional<PlanKey> getRunningPlanByLockKey(final String lockKey) {
        return Optional.ofNullable(plansExecutingForLocks.get(lockKey));
    }

    public static void removeRunningPlanByLockKey(final String lockKey) {
        plansExecutingForLocks.remove(lockKey);
    }

    public static void setRunningPlan(final String lockKey, final PlanKey planKey) {
        plansExecutingForLocks.put(lockKey, planKey);
    }

}
