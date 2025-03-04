package net.twisterrob.ghlint.rules

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Job
import net.twisterrob.ghlint.rule.Example
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.rule.Reporting
import net.twisterrob.ghlint.rule.report
import net.twisterrob.ghlint.rule.visitor.VisitorRule
import net.twisterrob.ghlint.rule.visitor.WorkflowVisitor

public class ExplicitJobPermissionsRule : VisitorRule, WorkflowVisitor {
	// Note: Sadly, this is not possible for actions, because they don't declare permissions.

	override val issues: List<Issue> = listOf(MissingJobPermissions, ExplicitJobPermissions)

	override fun visitJob(reporting: Reporting, job: Job) {
		super.visitJob(reporting, job)
		val directPermissions = job.permissions
		val parentPermissions = job.parent.permissions
		if (directPermissions == null && parentPermissions == null) {
			reporting.report(MissingJobPermissions, job) { "${it} is missing permissions." }
		}
		if (directPermissions == null && parentPermissions != null) {
			when {
				parentPermissions.map.isEmpty() -> {
					// This is fine, because it's explicitly empty, so no permissions are given to the job.
				}
				parentPermissions.contents == Access.READ && parentPermissions.map.size == 1 -> {
					// This is fine, because it's using the default contents permission.
					// Theoretically, this could still be an elevated permission,
					// but practically it's quite annoying to deal with.
				}
				else -> {
					reporting.report(ExplicitJobPermissions, job) { "${it} should have explicit permissions." }
				}
			}
		}
		if (directPermissions != null && parentPermissions != null) {
			reporting.report(ExplicitJobPermissions, job.parent) { "${it} has redundant permissions." }
		}
	}

	private companion object {

		val MissingJobPermissions = Issue(
			id = "MissingJobPermissions",
			title = "Permissions are not declared.",
			description = """
				Declaring permissions is essential for a safe usage of the GitHub Actions environment.
				It helps reduce the attack surface for malicious actors.
				
				This also prevents accidental leaking of privileged tokens,
				because each job will only have the permissions it actually needs.
				
				Declaring permissions for the `github.token` / `secrets.GITHUB_TOKEN` temporary Access Token is the best practice.
				
				There are three ways to declare permissions:
				
				 * on the repository level
				 * on the workflow level
				 * on the job level
				
				The recommended setting is:
				
				 * Set the organization/repository level permissions to ["Read repository contents and packages permissions"](https://github.blog/changelog/2021-04-20-github-actions-control-permissions-for-github_token/#setting-the-default-permissions-for-the-organization-or-repository).  
				   Sadly, the default is "Read and write permissions" (for everything), which is too permissive.
				 * Do not declare anything on the workflow level.
				 * Declare explicit permissions on the job level.
				
				This will ensure that the tokens will always have the least privilege.
				
				Most of the time all you need is:
				```yaml
				permissions:
				  contents: read
				```
				
				It is possible you don't need any permissions at all, in this case put:
				```yaml
				permissions: {}
				```
				
				References:
				
				 * [Documentation of `GITHUB_TOKEN` permissions](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#modifying-the-permissions-for-the-github_token)
				 * [What can go wrong if a too permissive token leaks?](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions#potential-impact-of-a-compromised-runner)
				 * [List of Available permissions](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token)
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Permissions are explicitly declared on the job level.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    permissions:
						      contents: read
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
				Example(
					explanation = "Permissions are explicitly declared on the workflow level.",
					content = """
						on: push
						permissions:
						  pull-requests: read
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = "No permissions are declared anywhere.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)

		val ExplicitJobPermissions = Issue(
			id = "ExplicitJobPermissions",
			title = "Permissions should be declared on the job level only.",
			description = """
				Declaring permissions on the workflow level leads to elevated permissions for all jobs.
				Even if the workflow has only one job, it is better to declare the permissions on the job level,
				this improves consistency, copy-paste-ability, and forms habits.
				
				Move the permissions declaration from the workflow level to the job level.
				
				References:
				
				 * [Best practice in documentation](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#modifying-the-permissions-for-the-github_token:~:text=The%20two,permissions%27%20scope.)
				   > The two workflow[s ...] show the permissions key being used at the job level,
				   > as it is best practice to limit the permissions' scope.
				 * [Explanation of the above](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#using-the-github_token-in-a-workflow)
				   > As a good security practice, you should always make sure that actions
				   > only have the minimum access they require by limiting the permissions granted to the GITHUB_TOKEN.
			""".trimIndent(),
			compliant = listOf(
				Example(
					explanation = "Permissions are explicitly declared on the job level.",
					content = """
						on: push
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    permissions:
						      contents: read
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
				Example(
					explanation = "All permissions are explicitly forbidden on the workflow level.",
					content = """
						on: push
						permissions: {}
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
			nonCompliant = listOf(
				Example(
					explanation = """
						Permissions are declared on the workflow level, leading to escalated privileges for all jobs.
						
						There are two jobs: `build` and `comment`:
						
						 * The `build` job needs to access contents to invoke the `make` command.
						 * The `comment` job needs to write comments to the pull request.
						
						With the `permissions:` being declared on the workflow,
						both jobs will have the same permissions.
						This leads to a larger attack surface:
						
						 * The comment job will be able to read the repository contents.
						   This means that if the publish-comment-action is compromised,
						   it can read/steal the repository contents.
						 * The build job will have full access to Pull Requests.
						   This means that if the make command is compromised,
						   it can do anything to PRs.
					""".trimIndent(),
					content = """
						on:
						  pull_request:
						permissions:
						  contents: read
						  pull-requests: write
						jobs:
						  build:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: actions/checkout@v4
						      - run: make
						  comment:
						    runs-on: ubuntu-latest
						    steps:
						      - uses: some/publish-comment-action@v0
					""".trimIndent(),
				),
				Example(
					explanation = """
						Permissions are declared on the workflow level.
						
						Note: This could be actually acceptable, because the workflow has only one job,
						but for consistency, copy-paste-ability, and habit-forming,
						it's better to still flag it to enforce declaring it on the job level.
						
						One exemption from this rule is when the permission list only contains `contents: read`.
						This is for practical reasons, as this is quite a common workflow structure.
					""".trimIndent(),
					content = """
						on: push
						permissions:
						  actions: read
						jobs:
						  example:
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
				Example(
					explanation = """
						Redundant permissions declared on workflow-level.
						
						When declaring permissions on the workflow-level as well as the job-level,
						the job-level restricts the workflow-level permissions.
						
						However, it is not necessary to declare permissions on the workflow-level,
						this can help reduce duplication and maintenance overhead.
						
						See [`MissingJobPermissions`](MissingJobPermissions.md),
						which help prevent accidental missing permissions.
					""".trimIndent(),
					content = """
						on: push
						permissions:
						  contents: read
						jobs:
						  example:
						    permissions:
						      contents: read
						    runs-on: ubuntu-latest
						    steps:
						      - run: echo "Example"
					""".trimIndent(),
				),
			),
		)
	}
}
