package net.twisterrob.ghlint.cli

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import net.twisterrob.ghlint.test.captureSystemStreams
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import java.nio.file.Path
import kotlin.io.path.writeText

@ResourceLock(value = Resources.SYSTEM_OUT, mode = ResourceAccessMode.READ_WRITE)
@ResourceLock(value = Resources.SYSTEM_ERR, mode = ResourceAccessMode.READ_WRITE)
class SchemaValidationTest {

	@Test fun `embedded workflow schema still validates input`(@TempDir dir: Path) {
		val workflow = dir.resolve("workflow.yml").apply { writeText("on: push\njobs: {}") }

		val result = captureSystemStreams {
			main("--no-exit", "--workflow-schema", ":embedded", workflow.toString())
		}

		result.stderr shouldBe ""
		result.stdout shouldContain "JsonSchemaValidation"
	}

	@Test fun `local workflow schema does not change action schema`(@TempDir dir: Path) {
		val schema = dir.resolve("custom.json").apply { writeText("""{"required":["workflowMarker"]}""") }
		val workflow = dir.resolve("workflow.yml").apply { writeText("on: push\njobs: {}") }
		val action = dir.resolve("action.yml").apply { writeText("name: Example") }

		val result = captureSystemStreams {
			main(
				"--no-exit", "--workflow-schema", schema.toString(), "--action-schema", ":embedded",
				workflow.toString(), action.toString(),
			)
		}

		result.stderr shouldBe ""
		result.stdout shouldContain "workflowMarker"
		result.stdout shouldContain "description"
	}

	@Test fun `local action schema does not change workflow schema`(@TempDir dir: Path) {
		val schema = dir.resolve("custom.json").apply { writeText("""{"required":["actionMarker"]}""") }
		val workflow = dir.resolve("workflow.yml").apply { writeText("on: push\njobs: {}") }
		val action = dir.resolve("action.yml").apply { writeText("name: Example") }

		val result = captureSystemStreams {
			main(
				"--no-exit", "--workflow-schema", ":embedded", "--action-schema", schema.toString(),
				workflow.toString(), action.toString(),
			)
		}

		result.stderr shouldBe ""
		result.stdout shouldContain "actionMarker"
		result.stdout shouldContain "JsonSchemaValidation"
		result.stdout shouldNotContain "description"
	}

	@Test fun `local file named embedded is not a reserved value`(@TempDir dir: Path) {
		val schema = dir.resolve("embedded").apply { writeText("""{"required":["localMarker"]}""") }
		val workflow = dir.resolve("workflow.yml").apply { writeText("on: push\njobs: {}") }

		val result = captureSystemStreams {
			main("--no-exit", "--workflow-schema", schema.toString(), workflow.toString())
		}

		result.stderr shouldBe ""
		result.stdout shouldContain "localMarker"
	}

	@Test fun `missing local schema produces an option error`(@TempDir dir: Path) {
		val workflow = dir.resolve("workflow.yml").apply { writeText("on: push\njobs: {}") }

		val result = captureSystemStreams {
			main("--no-exit", "--workflow-schema", dir.resolve("missing.json").toString(), workflow.toString())
		}

		result.stderr shouldContain "readable file"
	}

	@Test fun `schema choices do not leak to the next invocation`(@TempDir dir: Path) {
		val schema = dir.resolve("custom.json").apply { writeText("""{"required":["localMarker"]}""") }
		val workflow = dir.resolve("workflow.yml").apply { writeText("on: push\njobs: {}") }
		captureSystemStreams {
			main("--no-exit", "--workflow-schema", schema.toString(), workflow.toString())
		}

		val result = captureSystemStreams {
			main("--no-exit", "--workflow-schema", ":embedded", workflow.toString())
		}

		result.stderr shouldBe ""
		result.stdout shouldContain "JsonSchemaValidation"
		result.stdout shouldNotContain "localMarker"
	}
}
