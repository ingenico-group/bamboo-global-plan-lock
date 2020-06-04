package com.ingenico.bamboo.bpgl.impl;

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
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.LOCK_AQUIRE_RETRY_DELAY_SECONDS;

@Scanned
@Slf4j
public class GlobalPlanLockPreChainAction implements PreChainAction {

    private static final Lock lock = new ReentrantLock(true);

    @Override
    public void execute(final Chain chain, final ChainExecution chainExecution) throws Exception {

        final Optional<String> currentPlanLockKeyMaybe = LockKeyUtils.getLockKey(chain.getBuildDefinition());

        if (!currentPlanLockKeyMaybe.isPresent()) {
            // Current plan does not take part in any lock so we just continue!
            return;
        }

        final PlanKey currentPlanKey = chain.getPlanKey();
        final String currentPlanLockKey = currentPlanLockKeyMaybe.get();

        try {
            lock.lock();
            Optional<PlanKey> planKeyBeingExecuted = RunningPlans.getRunningPlanByLockKey(currentPlanLockKey);

            if (planKeyBeingExecuted.isPresent()) {
                log.info("Build has a the global lock key {}. Only one build with that key can be executed at the same time",
                        currentPlanLockKey);

                planKeyBeingExecuted = RunningPlans.getRunningPlanByLockKey(currentPlanLockKey);
                while (planKeyBeingExecuted.isPresent()) {
                    try {
                        // no not hog the lock when waiting
                        lock.unlock();

                        doWaitForPlan(planKeyBeingExecuted.get());

                        // but when we are done waiting take the lock for ourselves
                        lock.lock();

                        // keep checking if we can proceed
                        planKeyBeingExecuted = RunningPlans.getRunningPlanByLockKey(currentPlanLockKey);
                    } catch (InterruptedException e) {
                        // It will reach here when, for example, someone cancels the build from the UI
                        log.warn("Lock waiting interrupted! - {}", e.getMessage());
                        final PlanResultKey planResultKey = chainExecution.getPlanResultKey();
                        getChainExecutionManager().stop(planResultKey);

                        Thread.currentThread().interrupt();

                        return;
                    }
                }
            }

            RunningPlans.setRunningPlan(currentPlanLockKey, currentPlanKey);
        } finally {
            lock.unlock();
        }
    }

    private void doWaitForPlan(final PlanKey planKeyBeingExecuted) throws InterruptedException {

        final ImmutablePlan planBeingExecuted = getCachedPlanManager().getPlanByKey(planKeyBeingExecuted);
        if (planBeingExecuted == null) {
            log.info("Plan {} not found. Not waiting!", planKeyBeingExecuted);
            return;
        }

        // wait for ~1% of the build time or the default minimum
        final long minimumTimeToWait = TimeUnit.SECONDS.toMillis(LOCK_AQUIRE_RETRY_DELAY_SECONDS);

        final long averageDuration = planBeingExecuted.getAverageBuildDuration() / 100;
        final long timeToWaitMs = Math.max(averageDuration, minimumTimeToWait);

        log.info("Build is blocked because {} is currently executing. Will retry in {} seconds",
                planKeyBeingExecuted, TimeUnit.MILLISECONDS.toSeconds(timeToWaitMs));

        Thread.sleep(timeToWaitMs);
    }

    public CachedPlanManager getCachedPlanManager() {
        return ComponentLocator.getComponent(CachedPlanManager.class);
    }

    public ChainExecutionManager getChainExecutionManager() {
        return ComponentLocator.getComponent(ChainExecutionManager.class);
    }
}
