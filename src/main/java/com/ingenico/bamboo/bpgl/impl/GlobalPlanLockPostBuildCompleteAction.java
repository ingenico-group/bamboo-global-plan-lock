package com.ingenico.bamboo.bpgl.impl;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.build.CustomPostBuildCompletedAction;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.component.ComponentLocator;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Scanned
@Slf4j
public class GlobalPlanLockPostBuildCompleteAction implements CustomPostBuildCompletedAction {

    private BuildContext buildContext;

    @Override
    public void init(final BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    public CachedPlanManager getCachedPlanManager() {
        return ComponentLocator.getComponent(CachedPlanManager.class);
    }

    @Override
    public BuildContext call() {
        final BuildContext parentBuildContext = buildContext.getParentBuildContext();
        if (parentBuildContext == null) {
            return buildContext;
        }

        final BuildDefinition buildDefinition = parentBuildContext.getBuildDefinition();
        final Optional<String> currentPlanLockKeyMaybe = LockKeyUtils.getLockKey(buildDefinition);

        if (currentPlanLockKeyMaybe.isPresent()) {

            final String currentPlanLockKey = currentPlanLockKeyMaybe.get();
            final PlanKey currentPlanKey = buildContext.getTypedPlanKey();

            log.info("Releasing lock from plan '{}' with lock key '{}'", currentPlanKey, currentPlanLockKey);

            RunningPlans.removeRunningPlanByLockKey(currentPlanLockKey);
        }

        return buildContext;

    }

}
