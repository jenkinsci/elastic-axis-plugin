#!/usr/bin/env groovy

// Use container agents on odd numbered builds
// Assures that we test with container agents and without container agents
def useContainerForOddBuilds = ((BUILD_ID as int) % 2) as boolean

buildPlugin(
  useContainerAgent: useContainerForOddBuilds,
  // Opt-in to the Artifact Caching Proxy, to be removed when it will be in opt-out.
  // See https://github.com/jenkins-infra/helpdesk/issues/2752 for more details and updates.
  artifactCachingProxyEnabled: true,
  configurations: [
    [platform: 'linux',   jdk: '17', jenkins: '2.374'],
    [platform: 'linux',   jdk: '11', jenkins: '2.361.2'],
    [platform: 'windows', jdk:  '8']
  ]
)
