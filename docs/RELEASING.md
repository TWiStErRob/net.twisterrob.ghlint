# Release Process

1. Run ["Release: Prepare"][prepare] workflow on the `main` branch.
2. Review and merge the ["Release v..." pull request][pr-release].
3. Wait for ["Release: Publish"][publish] workflow to complete.
4. Review and merge the ["Prepare next development version v..." pull request][pr-next].

[prepare]: https://github.com/TWiStErRob/net.twisterrob.ghlint/actions/workflows/release-prepare.yml

[publish]: https://github.com/TWiStErRob/net.twisterrob.ghlint/actions/workflows/release-publish.yml

[pr-release]: https://github.com/TWiStErRob/net.twisterrob.ghlint/pulls?q=is%3Apr+is%3Aopen+in%3Atitle+%22Release%22

[pr-next]: https://github.com/TWiStErRob/net.twisterrob.ghlint/pulls?q=is%3Apr+is%3Aopen+in%3Atitle+%22Prepare%20next%20development%20version%22
