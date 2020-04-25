package com.ingenico.bamboo.bpgl.impl;

import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.LOCK_AQUIRE_RETRY_DELAY_SECONDS;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.ChainExecutionManager;
import com.atlassian.bamboo.chains.plugins.PreChainAction;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.component.ComponentLocator;

@Scanned
public class GlobalPlanLockPreChainAction implements PreChainAction {

	public static final Logger log = LoggerFactory.getLogger(GlobalPlanLockPreChainAction.class);

	@Override
	public void execute(Chain chain, ChainExecution chainExecution) throws Exception {

		Optional<String> currentPlanLockKeyMaybe = LockKeyUtils.getLockKey(chain.getBuildDefinition());

		if (!currentPlanLockKeyMaybe.isPresent()) {
			// Current plan does not take part in any lock so we just continue!
			return;
		}

		String currentPlanLockKey = currentPlanLockKeyMaybe.get();

		Optional<PlanKey> planKeyBeingExecuted = RunningPlans.getRunningPlanByLockKey(currentPlanLockKey);

		PlanKey currentPlanKey = chain.getPlanKey();

		if (planKeyBeingExecuted.isPresent()) {
			chain.setSuspendedFromBuilding(true);
			log.info(
					"Build has a the global lock key {}. Only one build with that key can be executed at the same time",
					currentPlanLockKey);

			planKeyBeingExecuted = RunningPlans.getRunningPlanByLockKey(currentPlanLockKey);
			while (planKeyBeingExecuted.isPresent()) {
				try {
					doWaitForPlan(planKeyBeingExecuted.get(), chainExecution);
					planKeyBeingExecuted = RunningPlans.getRunningPlanByLockKey(currentPlanLockKey);
				} catch (InterruptedException e) {
					log.warn("Lock waiting interruped! - {}", e.getMessage());
					PlanResultKey planResultKey = chainExecution.getPlanResultKey();
					getChainExecutionManager().stop(planResultKey);
					return;
				}
			}
			chain.setSuspendedFromBuilding(false);
		}

		RunningPlans.setRunningPlan(currentPlanLockKey, currentPlanKey);
	}

	private void doWaitForPlan(PlanKey planKeyBeingExecuted, ChainExecution chainExecution)
			throws InterruptedException {
		// wait for ~1% of the build time or the default minimum
		long minimumTimeToWait = TimeUnit.SECONDS.toMillis(LOCK_AQUIRE_RETRY_DELAY_SECONDS);

		ImmutablePlan planBeingExecuted = getCachedPlanManager().getPlanByKey(planKeyBeingExecuted);
		long averageDuration = planBeingExecuted.getAverageBuildDuration() / 100;
		long timeToWaitMs = Math.max(averageDuration, minimumTimeToWait);

		log.info("Build is blocked because {} is currently executing. Will retry in {} seconds",
				planKeyBeingExecuted.toString(), TimeUnit.MILLISECONDS.toSeconds(timeToWaitMs));

		Thread.sleep(timeToWaitMs);

	}

	public CachedPlanManager getCachedPlanManager() {
		return ComponentLocator.getComponent(CachedPlanManager.class);
	}

	public ChainExecutionManager getChainExecutionManager() {
		return ComponentLocator.getComponent(ChainExecutionManager.class);
	}
}
