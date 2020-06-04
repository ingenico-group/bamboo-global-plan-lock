# Bamboo Global Plan Lock 

This plugin enables a global lock for each plan even across agents. This means that every plan that has the same lock key can only be run exactly once.


## Build Locally

1 - Setup the Atlassian SDK and follow the tutorial (https://developer.atlassian.com/server/framework/atlassian-sdk/set-up-the-atlassian-plugin-sdk-and-build-a-project/)

2 - Launch Bamboo with this plugin with `atlas-run --product bamboo≈` (or `atlas-debug` to allow a debugger to attach to port 5005)
    (Default username/password: admin/admin)
    (To run a different version use the `-version x.y.z` parameter)

3 - While bamboo is running use `atlas-mvn package` to repackage the plugin and reload in bamboo


## Release:

Use the `atlas-release` command.

# References:

Atlassian plugin SDK
 - https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK

Bamboo build process
 - https://developer.atlassian.com/server/bamboo/bamboos-build-process/
 
 Bamboo tasks reference:
  - https://developer.atlassian.com/server/bamboo/bamboo-tasks-api/