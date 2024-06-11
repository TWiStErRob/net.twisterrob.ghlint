package net.twisterrob.ghlint.model

/**
 * See [list of permissions](
https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token
).
 */
@Suppress("detekt.ComplexInterface")
public interface Permissions {
	public val actions: Access
	public val attestations: Access
	public val checks: Access
	public val contents: Access
	public val deployments: Access
	public val discussions: Access
	public val idToken: Access
	public val issues: Access
	public val packages: Access
	public val pages: Access
	public val pullRequests: Access
	public val repositoryProjects: Access
	public val securityEvents: Access
	public val statuses: Access

	public val map: Map<String, String>

	@Suppress("detekt.CyclomaticComplexMethod")
	public operator fun get(permission: Permission): Access =
		when (permission) {
			Permission.ACTIONS -> actions
			Permission.ATTESTATIONS -> attestations
			Permission.CHECKS -> checks
			Permission.CONTENTS -> contents
			Permission.DEPLOYMENTS -> deployments
			Permission.DISCUSSIONS -> discussions
			Permission.ID_TOKEN -> idToken
			Permission.ISSUES -> issues
			Permission.PACKAGES -> packages
			Permission.PAGES -> pages
			Permission.PULL_REQUESTS -> pullRequests
			Permission.REPOSITORY_PROJECTS -> repositoryProjects
			Permission.SECURITY_EVENTS -> securityEvents
			Permission.STATUSES -> statuses
		}
}
