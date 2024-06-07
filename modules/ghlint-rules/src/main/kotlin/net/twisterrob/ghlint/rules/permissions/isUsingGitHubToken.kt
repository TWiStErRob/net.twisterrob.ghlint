package net.twisterrob.ghlint.rules.permissions

/**
 * `github.token` is defined by default in many actions, however their naming varies significantly.
 *
 * This function assumes that the [inputKey] is defined similar to this:
 * ```yaml
 * inputs:
 *   my-token:
 *     default: ${{ github.token }}
 * ```
 * ```yaml
 * uses: my-action
 * with:
 *   #my-token: ${{ github.token }} # Default, no need to list.
 * ```
 * It is possible that the user defined a custom, but default token, those should be all equivalent:
 *
 * ```yaml
 * uses: my-action
 * with:
 *   my-token: ${{ github.token }}
 * ```
 * ```yaml
 * uses: my-action
 * with:
 *   my-token: ${{ secrets.GITHUB_TOKEN }}
 * ```
 */
internal fun Map<String, String>?.isUsingGitHubToken(inputKey: String): Boolean {
	val token = this?.get(inputKey)
	return token == null || GITHUB_TOKEN_REGEX.matches(token)
}

private val GITHUB_TOKEN_REGEX = Regex("""^\s*${"\\$"}\{\{\s*(github\.token|secrets.GITHUB_TOKEN)\s*\}\}\s*$""")
