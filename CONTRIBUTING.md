# Contributing

Looking to contribute to ATLauncher? Excellent, we welcome any and all contributions.

When contributing to this repository, please first discuss the change you wish to make via GitHub issue,
[Discord](https://atl.pw/discord), or any other method with the owners of this repository before making a change.

## Code of Conduct

Please note we have a code of conduct, please follow it in all your interactions with the project.

## Reporting an Issue

If you have an issue with any product/service of ATLauncher, please create an issue in the appropriate GitHub
repository.

If there is no appropriate repository for your issue, please speak to any ATLauncher Staff member on our
[Discord](https://atl.pw/discord).

Alternatively you may email support@atlauncher.com with your issue.

If you have a security issue to disclose, please send an email to security@atlauncher.com so we can ensure that it's
looked after in a timely and sensitive manner.

## Pull Request's

* Ensure the [Pull Request Template](PULL_REQUEST_TEMPLATE.md) is filled out
* Ensure that your commit history is clean, lean and follows our [Branching Structure](#branching-structure)
* Include screenshots and animated GIFs in your Pull Request when possible
* Follow our [styling best practices](#styling-best-practices) for all code and documentation

## Branching Structure

Our branching structure is based on
[Gitflow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

* **master** will always contain the code for the latest production release
* **develop** will contain the latest beta version used for testing
* **feature/*** will contain single issues being developed. Once developed they're merged into develop and tagged with
  a beta release version. They should be named 'feature/22-some-brief-description'
* **release/*** will contain all the code from each feature branch that are going out in the next release and should
  all be merged into the branch. Once approved and tested it gets merged into master and tagged. They should be named
  'release/4.1.1'
* **hotfix/*** will contain hotfixes going to be merged directly into master then tagged. Hotfixes should only need to
  occur when there is a very critical bug in the current release that needs to be fixed ASAP. All hotfix branches should
  be branched off of master

All tags are done on the **master** branch only and should be in format **v(version number)**. Most versioning and
tagging is done automatically, so please check before doing so manually.

All develop releases should be versioned as **beta** versions. For instance **4.1.1-beta.15**.

Feature branches are deleted once in a release branch. Any issues that come up after the features branch has been merged
nto a release branch should be resolved by creating a new feature branch off the release branch.

An example of a good name for a feature branch is say there is an issue (#44) which is about not being able to delete a
pack. A good name for a feature branch would be `feature/44-unable-to-delete-packs` and go from there.

## Styling Best Practices

When committing code or documentation, please ensure that you follow our set out
[style guide](https://github.com/ATLauncher/style-guide).

## Issue and Pull Request Labels

This section lists the labels we use to help us track and manage issues and pull requests. While some labels are not
available in older repositories, most public repositories will use these conventions for labels.

[GitHub search](https://help.github.com/articles/searching-issues/) makes it easy to use labels for finding groups of
issues or pull requests you're interested in.

### Issue Labels

| Label name | Description |
| --- | --- |
| `enhancement` | Feature requests. |
| `bug` | Confirmed bugs or reports that are very likely to be bugs. |
| `question` | Questions more than bug reports or feature requests (e.g. how do I do X). |
| `feedback` | General feedback more than bug reports or feature requests. |
| `help-wanted` | The ATLauncher team would appreciate help from the community in resolving these issues. |
| `beginner` | Less complex issues which would be good first issues to work on for users who want to contribute. |
| `more-information-needed` | More information needs to be collected about these problems or feature requests |
| `needs-reproduction` | Likely bugs, but haven't been reliably reproduced. |
| `triage-help-needed` | Help is needed to triage the issue. |
| `blocked` | Issues blocked on other issues. |
| `duplicate` | Issues which are duplicates of other issues, i.e. they have been reported before. |
| `wontfix` | The ATLauncher team has decided not to fix these issues for now for some reason. |
| `invalid` | Issues which aren't valid (e.g. user errors). |
| `wrong-repo` | Issues reported on the wrong repository. |

### Pull Request Labels

| Label name | Description
| --- | --- |
| `work-in-progress` | Pull requests which are still being worked on, more changes will follow. |
| `needs-review` | Pull requests which need code review, and approval from maintainers or ATLauncher team. |
| `under-review` | Pull requests being reviewed by maintainers or ATLauncher team. |
| `requires-changes` | Pull requests which need to be updated based on review comments and then reviewed again. |
| `needs-testing` | Pull requests which need manual testing. |
| `ready-to-merge` | Pull requests which have been reviewed and are ready to merge. |
