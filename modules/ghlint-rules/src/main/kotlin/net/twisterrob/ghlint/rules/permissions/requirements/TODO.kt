package net.twisterrob.ghlint.rules.permissions.requirements

import net.twisterrob.ghlint.model.Access
import net.twisterrob.ghlint.model.Permission
import net.twisterrob.ghlint.model.Scope

@Suppress("detekt.UnusedPrivateProperty") // To have a clean build, TODO remove before merging.
private val REQUIRED_PERMISSIONS_OLD: Map<String, Set<Scope>> = mapOf(
	// Permissions are only required if `token` is not defined, or it's using github.token explicitly.
	"actions/deploy-pages" to setOf(
		// https://github.com/actions/deploy-pages/blob/main/action.yml
		// Only when `token` is not defined explicitly, or it's using github.token explicitly.
		Scope(Permission.PAGES, Access.WRITE), // To deploy to GitHub Pages.
		Scope(
			Permission.ID_TOKEN,
			Access.WRITE
		), // To verify the deployment originates from an appropriate source.
	),
	"github/codeql-action/upload-sarif" to setOf(
		// https://github.com/github/codeql-action/blob/main/upload-sarif/action.yml
		// Only when `github_token` is not defined, or it's using github.token explicitly.
		Scope(Permission.SECURITY_EVENTS, Access.WRITE), // To upload SARIF files.
		// Only in private repositories / internal organizations.
		Scope(Permission.ACTIONS, Access.WRITE),
	),
	// Permissions are only required if `github_token` is not defined, or it's using github.token explicitly.
	"EnricoMi/publish-unit-test-result-action" to setOf(
		// https://github.com/EnricoMi/publish-unit-test-result-action/blob/master/action.yml
		// Only when check_run == true, or not listed as default is true.
		Scope(Permission.CHECKS, Access.WRITE), // To publish check runs.
		// Only when comment_mode != off.
		// (i.e. always, changes, changes in failures, changes in errors, failures, errors; default is always)
		Scope(Permission.PULL_REQUESTS, Access.WRITE), // To comment on PRs.
		// Only in private repos:
		Scope(Permission.ISSUES, Access.READ),
		Scope(Permission.CONTENTS, Access.READ),
	),
)
