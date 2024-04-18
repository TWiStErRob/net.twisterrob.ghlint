package net.twisterrob.ghlint.docs.issues

import io.kotest.matchers.paths.aDirectory
import io.kotest.matchers.paths.exist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.rule.Issue
import net.twisterrob.ghlint.ruleset.RuleSet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.relativeTo

class FileLocatorTest {

	@Test fun testRuleSet(@TempDir temp: Path) {
		val locator = FileLocator(temp)

		val ruleSet: RuleSet = mock()
		whenever(ruleSet.id).thenReturn("test-ruleset")

		val result = locator.ruleSetFile(ruleSet)

		result.name shouldBe "index.md"
		result shouldNot exist()

		result.parent should exist()
		result.parent shouldBe aDirectory()
		result.parent.name shouldBe "test-ruleset"

		result.parent.parent shouldBe temp
	}

	@Test fun testIssue(@TempDir temp: Path) {
		val locator = FileLocator(temp)

		val ruleSet: RuleSet = mock()
		whenever(ruleSet.id).thenReturn("test-ruleset")
		val issue: Issue = mock()
		whenever(issue.id).thenReturn("TestIssue")

		val result = locator.issueFile(ruleSet, issue)

		result.name shouldBe "TestIssue.md"
		result shouldNot exist()

		result.parent should exist()
		result.parent shouldBe aDirectory()
		result.parent.name shouldBe "test-ruleset"

		result.parent.parent shouldBe temp
	}

	@Test fun testDoc(@TempDir temp: Path) {
		val locator = FileLocator(temp)

		val result = locator.docFile("foo/bar")

		result.name shouldBe "bar"
		result shouldNot exist()

		result.parent shouldNot exist()
		result.parent.name shouldBe "foo"

		result.parent.parent shouldBe temp
	}

	@Test fun `markdown paths are escaped`(@TempDir temp: Path) {
		val locator = FileLocator(temp)

		val result = locator.docFile("foo/bar")

		result.relativeTo(temp).asMarkdownPath() shouldBe "foo/bar"
	}
}
