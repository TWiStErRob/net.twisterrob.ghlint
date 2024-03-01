package net.twisterrob.ghlint

import io.kotest.matchers.paths.shouldContainFile
import io.kotest.matchers.paths.shouldContainNFiles
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.nio.file.Path
import kotlin.io.path.writeText

class GHLintTest {

	@Test
	fun `no files`() {
		val configuration: Configuration = mock()
		whenever(configuration.files).thenReturn(emptyList())

		val result = GHLint().run(configuration)

		result shouldBe 0
	}

	@Test
	fun `single valid file`(@TempDir tempDir: Path) {
		val test = tempDir.resolve("test.yml")
		test.writeText(
			"""
				name: "Test"
				on: push
				jobs:
				  test:
				    name: "Test"
				    runs-on: ubuntu-latest
				    timeout-minutes: 1
				    permissions: {}
				    steps:
				      - name: "Test"
				        shell: bash
				        run: echo "Test"
			""".trimIndent()
		)
		val configuration: Configuration = mock()
		whenever(configuration.root).thenReturn(tempDir)
		whenever(configuration.files).thenReturn(listOf(test))
		whenever(configuration.isReportExitCode).thenReturn(true)

		val result = GHLint().run(configuration)

		result shouldBe 0
		tempDir shouldContainFile "test.yml"
		tempDir shouldContainNFiles 1
	}

	@Test
	fun `single invalid file`(@TempDir tempDir: Path) {
		val test = tempDir.resolve("test.yml")
		test.writeText(
			"""
			""".trimIndent()
		)
		val configuration: Configuration = mock()
		whenever(configuration.root).thenReturn(tempDir)
		whenever(configuration.files).thenReturn(listOf(test))
		whenever(configuration.isReportExitCode).thenReturn(true)

		val result = GHLint().run(configuration)

		result shouldBe 1
		tempDir shouldContainFile "test.yml"
		tempDir shouldContainNFiles 1
	}
}
