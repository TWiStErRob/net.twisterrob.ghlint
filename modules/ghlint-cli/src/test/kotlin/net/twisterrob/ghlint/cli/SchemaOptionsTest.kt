package net.twisterrob.ghlint.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.SchemaSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

class SchemaOptionsTest {

	@Test fun `both schemas default to latest`() {
		val command = Command()
		command.parse(emptyList())

		command.schemas.workflow shouldBe SchemaSource.Latest
		command.schemas.action shouldBe SchemaSource.Latest
	}

	@Test fun `latest can be selected explicitly`() {
		val command = Command()
		command.parse(listOf("--workflow-schema", ":latest", "--action-schema", ":latest"))

		command.schemas.workflow shouldBe SchemaSource.Latest
		command.schemas.action shouldBe SchemaSource.Latest
	}

	@Test fun `workflow can be embedded independently`() {
		val command = Command()
		command.parse(listOf("--workflow-schema", ":embedded"))

		command.schemas.workflow shouldBe SchemaSource.Embedded
		command.schemas.action shouldBe SchemaSource.Latest
	}

	@Test fun `action can be embedded independently`() {
		val command = Command()
		command.parse(listOf("--action-schema", ":embedded"))

		command.schemas.workflow shouldBe SchemaSource.Latest
		command.schemas.action shouldBe SchemaSource.Embedded
	}

	@Test fun `local files are accepted`(@TempDir dir: Path) {
		val schema = dir.resolve("schema.json").apply { writeText("{}") }
		val command = Command()
		command.parse(listOf("--workflow-schema", schema.toString(), "--action-schema", schema.toString()))

		command.schemas.workflow shouldBe SchemaSource.LocalFile(schema)
		command.schemas.action shouldBe SchemaSource.LocalFile(schema)
	}

	@Test fun `missing file is rejected`(@TempDir dir: Path) {
		shouldThrow<UsageError> {
			Command().parse(listOf("--workflow-schema", dir.resolve("missing.json").toString()))
		}
	}

	private class Command : CliktCommand() {
		val schemas: SchemaOptions by SchemaOptions()
		override fun run() = Unit
	}
}
