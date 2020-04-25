package com.ingenico.bamboo.bpgl.impl;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.atlassian.bamboo.plan.PlanKey;

public class RunningPlans {

	private static final ConcurrentHashMap<String, PlanKey> plansExecutingForLocks = new ConcurrentHashMap<>();

	public static Optional<PlanKey> getRunningPlanByLockKey(String lockKey) {
		return Optional.ofNullable(plansExecutingForLocks.get(lockKey));
	}

	public static void removeRunningPlanByLockKey(String lockKey) {
		plansExecutingForLocks.remove(lockKey);
	}

	public static void setRunningPlan(String lockKey, PlanKey planKey) {
		plansExecutingForLocks.put(lockKey, planKey);
	}

}
