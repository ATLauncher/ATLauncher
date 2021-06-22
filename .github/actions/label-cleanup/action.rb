require 'octokit'

repo = ENV["GITHUB_REPOSITORY"]
label = ENV["LABEL"]
comment = ENV["COMMENT"]
token = ENV["TOKEN"]

client = Octokit::Client.new(:access_token => token)
client.auto_paginate = true

open_issues = client.list_issues(repo, { :labels => label, :state => 'open'})

open_issues.each do |issue|
    client.add_comment(repo, issue.number, comment)
    client.remove_label(repo, issue.number, label)
    client.close_issue(repo, issue.number)
end
