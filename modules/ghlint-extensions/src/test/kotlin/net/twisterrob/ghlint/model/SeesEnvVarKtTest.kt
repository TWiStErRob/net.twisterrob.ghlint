package net.twisterrob.ghlint.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.twisterrob.ghlint.testing.load
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock

class SeesEnvVarKtTest {

	private fun loadRunStep(@Language("yaml") yaml: String, fileName: String = "test.yml"): Step {
		val file = load(yaml, fileName)
		return when (val content = file.content) {
			is Workflow -> (content.jobs.values.single() as Job.NormalJob).steps.single()
			is Action -> (content.runs as Action.Runs.CompositeRuns).steps.single()
			is InvalidContent -> error("Invalid content: ${content.error}")
		}
	}

	@Test fun `fails on unknown step`() {
		val step: Step.BaseStep = mock()

		shouldThrow<IllegalStateException> { step.seesEnvVar("MY_VAR") }
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `no variables defined in workflow`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - ${stepKey}: ${stepValue}
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe false
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `explicit variable is defined for step in workflow`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - ${stepKey}: ${stepValue}
				        env:
				          MY_VAR: value
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `job variable is defined for step in workflow`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_VAR: value
				    steps:
				      - ${stepKey}: ${stepValue}
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `workflow variable is defined for step in workflow`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				env:
				  MY_VAR: value
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - ${stepKey}: ${stepValue}
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `workflow and job variables are defined for step in workflow`(
		stepKey: String,
		stepValue: String,
	) {
		val step = loadRunStep(
			"""
				on: push
				env:
				  MY_VAR: value1
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_VAR: value2
				    steps:
				      - ${stepKey}: ${stepValue}
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `all possible variables is defined for step in workflow - workflow`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				env:
				  MY_VAR: value
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_VAR: value
				    steps:
				      - ${stepKey}: ${stepValue}
				        env:
				          MY_VAR: value
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `many different variables for step in workflow - workflow`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				env:
				  MY_VAR1: value1
				  MY_VAR2: value2
				  MY_VAR: value
				  MY_VAR3: value3
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_VAR4: value4
				      MY_VAR5: value5
				      MY_VAR6: value6
				    steps:
				      - ${stepKey}: ${stepValue}
				        env:
				          MY_VAR7: value7
				          MY_VAR8: value8
				          MY_VAR9: value9
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `many different variables for step in workflow - job`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				env:
				  MY_VAR1: value1
				  MY_VAR2: value2
				  MY_VAR3: value3
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_VAR4: value4
				      MY_VAR5: value5
				      MY_VAR: value
				      MY_VAR6: value6
				    steps:
				      - ${stepKey}: ${stepValue}
				        env:
				          MY_VAR7: value7
				          MY_VAR8: value8
				          MY_VAR9: value9
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `many different variables for step in workflow - step`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				on: push
				env:
				  MY_VAR1: value1
				  MY_VAR2: value2
				  MY_VAR: value
				  MY_VAR3: value3
				jobs:
				  test:
				    runs-on: test
				    env:
				      MY_VAR4: value4
				      MY_VAR5: value5
				      MY_VAR6: value6
				    steps:
				      - ${stepKey}: ${stepValue}
				        env:
				          MY_VAR7: value7
				          MY_VAR8: value8
				          MY_VAR: value
				          MY_VAR9: value9
			""".trimIndent(),
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `no variables defined in action`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - ${stepKey}: ${stepValue}
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		step.seesEnvVar("MY_VAR") shouldBe false
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `variable is defined for step in action`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - ${stepKey}: ${stepValue}
				      env:
				        MY_VAR: value
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	@MethodSource("getActions")
	@ParameterizedTest
	fun `multiple variables are defined for step in action`(stepKey: String, stepValue: String) {
		val step = loadRunStep(
			"""
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - ${stepKey}: ${stepValue}
				      env:
				        MY_VAR1: value1
				        MY_VAR: value
				        MY_VAR2: value2
				      shell: bash
			""".trimIndent(),
			fileName = "action.yml",
		)

		step.seesEnvVar("MY_VAR") shouldBe true
		step.seesEnvVar("NOT_MY_VAR") shouldBe false
	}

	companion object {
		@JvmStatic
		val actions = arrayOf(
			Arguments.of("uses", "some/action@v1"),
			Arguments.of("run", "echo Hello"),
		)
	}
}
