const core = require("@actions/core");
const github = require("@actions/github");

async function run() {
    try {
        const label = core.getInput("label");
        const comment = core.getInput("comment");
        const token = core.getInput("token");

        const octokit = github.getOctokit(token);

        const issues = await octokit.rest.issues.listForRepo({
            labels: label,
            state: "open",
            owner: github.context.repo.owner,
            repo: github.context.repo.repo,
        });

        issues.data.forEach(async (issue) => {
            await octokit.rest.issues.createComment({
                body: comment,
                issue_number: issue.number,
                owner: github.context.repo.owner,
                repo: github.context.repo.repo,
            });

            await octokit.rest.issues.removeLabel({
                name: label,
                issue_number: issue.number,
                owner: github.context.repo.owner,
                repo: github.context.repo.repo,
            });

            await octokit.rest.issues.update({
                issue_number: issue.number,
                owner: github.context.repo.owner,
                repo: github.context.repo.repo,
                state: "closed",
            });
        });
    } catch (error) {
        core.setFailed(error.message);
    }
}

run();
