# Contributing to the Elastic Axis Plugin

Plugin source code is hosted on [GitHub](https://github.com/jenkinsci/elastic-axis-plugin).
New feature proposals and bug fix proposals should be submitted as
[GitHub pull requests](https://help.github.com/articles/creating-a-pull-request).
Your pull request will be evaluated by the [Jenkins job](https://ci.jenkins.io/job/Plugins/job/elastic-axis-plugin/).

Before submitting your change, please assure that you've added tests that verify your change.

## Code Coverage

Code coverage reporting is available as a maven target.
Please try to improve code coverage with tests when you submit.
* `mvn -P enable-jacoco clean install jacoco:report` to report code coverage

Please don't introduce new spotbugs output.
* `mvn spotbugs:check` to analyze project using [Spotbugs](https://spotbugs.github.io)
* `mvn spotbugs:gui` to review report using GUI

## Code Formatting

Code formatting in the Elastic Axis plugin is maintained by the git code format maven plugin.
The pom file format is maintained by the tidy plugin.
Before submitting a pull request, confirm the formatting is correct with:

* `mvn compile`

If the formatting is not correct, the build will fail.  Correct the formatting with:

* `mvn tidy:pom git-code-format:format-code -Dgcf.globPattern=**/*`

## Pre-commit Hooks

File content is consistency checked at commit time by the [pre-commit framework](https://pre-commit.com/).
Refer to [.pre-commit-config.yaml](.pre-commit-config.yaml) for the current checks.

To install the pre-commit framework into this repository on your computer, use the commands:

```
$ pip install --user pre-commit
$ pre-commit install
```

To upgrade the pre-commit framework into this repository on your computer, use the commands:

```
$ pip install --user --upgrade pre-commit
$ pre-commit install
```

Pre-commit checks are run on modified files during `git commit`.
Files which fail pre-commit checks will abort the `git commit`.

Run pre-commit checks on all files:
```
$ pre-commit run --all-files
```

## Security Issues

Follow the [Jenkins project vulnerability reporting instructions](https://jenkins.io/security/reporting/) to report vulnerabilities.
