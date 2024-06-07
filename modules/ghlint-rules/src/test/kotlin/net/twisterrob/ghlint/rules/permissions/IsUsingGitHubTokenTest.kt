package net.twisterrob.ghlint.rules.permissions

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class IsUsingGitHubTokenTest {

	@Test
	fun `no inputs implies github token`() {
		null.isUsingGitHubToken("token") shouldBe true
	}

	@Test
	fun `missing input implies github token`() {
		null.isUsingGitHubToken("not-declared-token") shouldBe true
	}

	@Test
	fun `the right token value is used`() {
		val with = mapOf(
			"token" to "\${{ github.token }}",
			"other" to "\${{ inputs.github-token }}",
		)

		with.isUsingGitHubToken("token") shouldBe true
		with.isUsingGitHubToken("other") shouldBe false
	}

	@MethodSource("gitHubTokens")
	@ParameterizedTest
	fun `GitHub token is detected`(token: String) {
		val with = mapOf("token" to token)

		val result = with.isUsingGitHubToken("token")

		result shouldBe true
	}

	@MethodSource("otherTokens")
	@ParameterizedTest
	fun `other token is ignored`(token: String) {
		val with = mapOf("token" to token)

		val result = with.isUsingGitHubToken("token")

		result shouldBe false
	}

	companion object {
		@JvmStatic
		fun gitHubTokens(): List<String> = listOf(
			"\${{ github.token }}",
			"\${{github.token}}",
			"\${{   github.token}}",
			"\${{github.token   }}",
			"\${{ secrets.GITHUB_TOKEN }}",
			"\${{secrets.GITHUB_TOKEN}}",
			"\${{   secrets.GITHUB_TOKEN}}",
			"\${{   secrets.GITHUB_TOKEN   }}",
		)

		@JvmStatic
		fun otherTokens(): List<String> = listOf(
			"\${{ inputs.github-token }}",
			"\${{ secrets.NON_GITHUB_TOKEN }}",
			"hardcoded_token",
		)
	}
}
