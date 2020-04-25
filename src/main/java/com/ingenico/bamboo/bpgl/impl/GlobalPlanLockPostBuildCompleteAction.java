package com.ingenico.bamboo.bpgl.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.build.BuildDefinition;
import com.atlassian.bamboo.build.CustomPostBuildCompletedAction;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.component.ComponentLocator;

@Scanned
public class GlobalPlanLockPostBuildCompleteAction implements CustomPostBuildCompletedAction {

	public static final Logger log = LoggerFactory.getLogger(GlobalPlanLockPostBuildCompleteAction.class);

	private BuildContext buildContext;

	@Override
	public void init(BuildContext buildContext) {
		this.buildContext = buildContext;
	}

	public CachedPlanManager getCachedPlanManager() {
		return ComponentLocator.getComponent(CachedPlanManager.class);
	}

	public BuildContext call() throws InterruptedException, Exception {
		BuildContext parentBuildContext = buildContext.getParentBuildContext();
		if (parentBuildContext == null) {
			return buildContext;
		}

		BuildDefinition buildDefinition = parentBuildContext.getBuildDefinition();
		Optional<String> currentPlanLockKeyMaybe = LockKeyUtils.getLockKey(buildDefinition);

		if (currentPlanLockKeyMaybe.isPresent()) {

			String currentPlanLockKey = currentPlanLockKeyMaybe.get();
			PlanKey currentPlanKey = buildContext.getTypedPlanKey();

			log.info("Releasing lock from plan '{}' with lock key '{}'", currentPlanKey.toString(), currentPlanLockKey);

			RunningPlans.removeRunningPlanByLockKey(currentPlanLockKey);
		}

		return buildContext;

	}

}
