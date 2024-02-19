# Release Process

1. Run ["Release: Prepare"][prepare] workflow on the `main` branch.
2. Close and immediately reopen the pull request to [trigger CI][reopen].
3. Review and merge the "Release v..." pull request.
4. Wait for ["Release: Publish"][publish] workflow to complete.
5. Review and merge the "Prepare next development version v..." pull request.

[prepare]: https://github.com/TWiStErRob/net.twisterrob.ghlint/actions/workflows/release-prepare.yml
[publish]: https://github.com/TWiStErRob/net.twisterrob.ghlint/actions/workflows/release-publish.yml
[reopen]: https://github.com/peter-evans/create-pull-request/blob/main/docs/concepts-guidelines.md#triggering-further-workflow-runs:~:text=Manually,reopen%20them.
