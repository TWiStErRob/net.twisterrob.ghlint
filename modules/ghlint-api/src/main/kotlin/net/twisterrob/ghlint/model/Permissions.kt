package net.twisterrob.ghlint.model

// https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token
public interface Permissions {
	public val actions: Access
	public val attestations: Access
	public val checks: Access
	public val contents: Access
	public val deployments: Access
	public val idToken: Access
	public val issues: Access
	public val metadata: Access
	public val packages: Access
	public val pages: Access
	public val pullRequests: Access
	public val repositoryProjects: Access
	public val securityEvents: Access
	public val statuses: Access

	public fun asMap(): Map<String, String>
}
