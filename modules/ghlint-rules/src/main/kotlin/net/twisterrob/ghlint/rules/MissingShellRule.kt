package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Step
import net.twisterrob.ghlint.model.effectiveShell
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.VisitorRule
import net.twisterrob.ghlint.rule.report

public class MissingShellRule : VisitorRule {

	override val issues: List<Issue> = listOf(MissingShell)

	override fun visitRunStep(reporting: Reporting, step: Step.Run) {
		super.visitRunStep(reporting, step)
		if (step.effectiveShell == null) {
			reporting.report(MissingShell, step) {
				"${it} is missing a shell, specify `bash` for better error handling."
			}
		}
	}

	private companion object {

		val MissingShell = Issue(
			id = "MissingShell",
			title = "Run step is missing a shell.",
			description = """
				Specifying a shell explicitly has benefits,
				see the [`shell:` documentation](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsshell) for what changes.
				
				The `shell:` can be specified on 3 levels, and the lowest wins:
				
				 * [`defaults.run.shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#defaultsrun)
				 * [`jobs.<job_id>.defaults.run.shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_iddefaultsrun)
				 * [`jobs.<job_id>.steps[*].shell`](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsshell)
				
				For Linux / MacOS runners it's recommended to specify `bash` explicitly, because it adds additional arguments:
				
				 * `-e`: to stop on first error (used for both)
				 * `-o pipefail`: propagate exit code from error inside a pipe
				 * `--noprofile`/`--norc` start with a clean environment.
				
				For a deeper explanation of `-e`/`-o` read [this gist](https://gist.github.com/mohanpedala/1e2ff5661761d3abd0385e8223e16425).
				This gives us faster failures and more useful error messages.
				
				It's worth noting that simple shell commands might not warrant an explicit shell, but it's worth adding them anyway:
				
				 * for consistency with other steps and workflows.
				 * for maintainability, in case the command becomes more complex, protection is already in place.
				 * for copy-paste-ability, when the command or its modified version is "reused" in other workflows.
				 * for readability, to help the reader understand the environment the script is running in.
				
				Known shortcut: `shell: bash` is also recommended for Windows, but it's supported by GitHub Actions.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Specified shell makes grep fail.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example" | grep "Missing" | sort
						        shell: bash
					""".trimIndent(),
				),
				Example(
					explanation = "Globally specified shell is inherited to the step.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    defaults:
						      run:
						        shell: bash
						    steps:
						      - run: echo "Example" | grep "Missing" | sort
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "Missing shell masks pipe failures.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example" | grep "Missing" | sort
					""".trimIndent(),
				),
			),
		)
	}
}
