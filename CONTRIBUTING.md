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

* Ensure the Pull Request template is filled out
* Ensure that your commit history is clean, lean and follows our [Branching Structure](#branching-structure)
* Include screenshots and animated GIFs in your Pull Request when possible
* Follow our [styling best practices](#styling-best-practices) for all code and documentation

## Branching Structure

We essentially use trunk based development.

* **master** is the main branch and contains all the code that is released to the public.
* **develop** is where all in development work is merged to and is considered the main branch in the sense that all pull
    requests should be sent to the develop branch
* **feature/\*** will contain single issues being developed. Once developed they're merged into develop. They should
    be named 'feature/22-some-brief-description' as an example

Once a release is ready to go, a release branch will be created from the develop branch and merged into master.

For any hotfixes, a hotfix branch will be created from the master branch and merged into master.

All tags are done on the **master** branch only and should be in format **v(version number)**. Most versioning and
tagging is done automatically, so please check before doing so manually.

Feature branches are deleted once merged into develop. Any issues that come up after the features branch has been
merged should be resolved by creating a new issue and feature branch.

An example of a good name for a feature branch is say there is an issue (#44) which is about not being able to delete a
pack. A good name for a feature branch would be `feature/44-unable-to-delete-packs` and go from there.

## Issue and Pull Request Labels

This section lists the labels we use to help us track and manage issues and pull requests. While some labels are not
available in older repositories, most public repositories will use these conventions for labels.

[GitHub search](https://help.github.com/articles/searching-issues/) makes it easy to use labels for finding groups of
issues or pull requests you're interested in.

### Issue Labels

| Label name                | Description                                                                             |
| ------------------------- | --------------------------------------------------------------------------------------- |
| `enhancement`             | Feature requests.                                                                       |
| `bug`                     | Confirmed bugs or reports that are very likely to be bugs.                              |
| `question`                | Questions more than bug reports or feature requests (e.g. how do I do X).               |
| `feedback`                | General feedback more than bug reports or feature requests.                             |
| `help-wanted`             | The ATLauncher team would appreciate help from the community in resolving these issues. |
| `beginner`                | Less complex issues which would be good first issues to work on.                        |
| `more-information-needed` | More information needs to be collected about these problems or feature requests.        |
| `triage-help-needed`      | Help is needed to triage the issue.                                                     |
| `blocked`                 | Issues blocked on other issues.                                                         |
| `known-workaround`        | Issues which have a known workaround.                                                   |
| `duplicate`               | Issues which are duplicates of other issues, i.e. they have been reported before.       |
| `wontfix`                 | The ATLauncher team has decided not to fix these issues for now for some reason.        |
| `invalid`                 | Issues which aren't valid (e.g. user errors).                                           |
| `wrong-repo`              | Issues reported on the wrong repository.                                                |
| `discussion`              | Issue that is having/needs discussion.                                                  |
| `support`                 | User has asked for support, but support is not given via issues.                        |

### Pull Request Labels

| Label name         | Description                                                                              |
| ------------------ | ---------------------------------------------------------------------------------------- |
| `work-in-progress` | Pull requests which are still being worked on, more changes will follow.                 |
| `needs-review`     | Pull requests which need code review, and approval from maintainers or ATLauncher team.  |
| `under-review`     | Pull requests being reviewed by maintainers and/or ATLauncher team.                      |
| `requires-changes` | Pull requests which need to be updated based on review comments and then reviewed again. |
| `needs-testing`    | Pull requests which need manual testing.                                                 |
| `ready-to-merge`   | Pull requests which have been reviewed and are ready to merge.                           |

### Priority Labels

| Label name          | Description                                    |
| ------------------- | ---------------------------------------------- |
| `critical-priority` | Should be addressed with the highest priority. |
| `high-priority`     | High priority to be addressed.                 |
| `low-priority`      | Low priority to be addressed.                  |

### Roadmap Labels

| Label name            | Description                                           |
| --------------------- | ----------------------------------------------------- |
| `roadmap-short-term`  | Items on the roadmap in the short term (1 month).     |
| `roadmap-medium-term` | Items on the roadmap in the medium term (3-6 months). |
| `roadmap-long-term`   | Items on the roadmap in the long term (6-12 months).  |
