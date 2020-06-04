package com.ingenico.bamboo.bpgl.impl.config;

import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plan.cache.ImmutableTopLevelPlan;
import com.atlassian.bamboo.plan.configuration.MiscellaneousPlanConfigurationPlugin;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.bamboo.v2.build.BaseBuildConfigurationAwarePlugin;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang3.StringUtils;

import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.GLOBAL_LOCK_ENABLED;
import static com.ingenico.bamboo.bpgl.impl.LockingConfiguration.GLOBAL_LOCK_KEY;

@Scanned
public class CustomConfiguration extends BaseBuildConfigurationAwarePlugin
        implements MiscellaneousPlanConfigurationPlugin {

    @SuppressWarnings("deprecation")
    public CustomConfiguration(@ComponentImport final TemplateRenderer templateRenderer) {
        setTemplateRenderer(templateRenderer);
    }

    @Override
    public boolean isApplicableTo(final ImmutablePlan plan) {
        return plan instanceof ImmutableTopLevelPlan;
    }

    @Override
    public void addDefaultValues(final BuildConfiguration buildConfiguration) {
        buildConfiguration.setProperty(GLOBAL_LOCK_ENABLED, "false");
        buildConfiguration.setProperty(GLOBAL_LOCK_KEY, "");
    }

    @Override
    public ErrorCollection validate(final BuildConfiguration buildConfiguration) {

        final String enabledStr = buildConfiguration.getString(GLOBAL_LOCK_ENABLED);
        final String lockKey = buildConfiguration.getString(GLOBAL_LOCK_KEY);

        final boolean enabled = Boolean.parseBoolean(enabledStr);

        final ErrorCollection errors = new SimpleErrorCollection();
        if (enabled && StringUtils.isEmpty(lockKey)) {
            errors.addError(GLOBAL_LOCK_KEY, "If enabled a lock key is mandatory");
        }

        return errors;
    }

    @Override
    public boolean isConfigurationMissing(BuildConfiguration buildConfiguration) {
        final String enabledStr = buildConfiguration.getString(GLOBAL_LOCK_ENABLED);
        return enabledStr == null || StringUtils.isEmpty(enabledStr);
    }
}
