# Contributing to the Elastic Axis Plugin

Plugin source code is hosted on [GitHub](https://github.com/jenkinsci/elastic-axis-plugin).
New feature proposals and bug fix proposals should be submitted as
[GitHub pull requests](https://help.github.com/articles/creating-a-pull-request).
Your pull request will be evaluated by the [Jenkins job](https://ci.jenkins.io/job/Plugins/job/elastic-axis-plugin/).

Before submitting your change, please assure that you've added tests which verify your change.

## Code Coverage

Code coverage reporting is available as a maven target.
Please try to improve code coverage with tests when you submit.
* `mvn -P enable-jacoco clean install jacoco:report` to report code coverage

Please don't introduce new spotbugs output.
* `mvn spotbugs:check` to analyze project using [Spotbugs](https://spotbugs.github.io)
* `mvn spotbugs:gui` to review report using GUI

## Code Formatting

Code formatting is maintained by the spotless maven plugin.
Before submitting a pull request, confirm the formatting is correct with:

* `mvn spotless:check compile`

If the formatting is not correct, the build will fail.  Correct the formatting with:

* `mvn spotless:apply`

## Releases

Releases are performed automatically using the `jx-release-version` command to increment the version number.
Special thanks to Gareth Evans for his help configuring the automated release action.
The version number is incremented based on the category of entries in the `next` changelog draft on GitHub.
