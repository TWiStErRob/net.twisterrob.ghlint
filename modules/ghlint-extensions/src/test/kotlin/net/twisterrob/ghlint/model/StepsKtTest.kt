package net.twisterrob.ghlint.model

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.beUnique
import io.kotest.matchers.collections.contain
import io.kotest.matchers.collections.endWith
import io.kotest.matchers.collections.haveElementAt
import io.kotest.matchers.collections.haveSize
import io.kotest.matchers.collections.startWith
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import net.twisterrob.ghlint.testing.load
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class StepsKtTest {

	private fun loadStep(index: Int, @Language("yaml") yaml: String, fileName: String = "test.yml"): Step {
		val file = load(yaml, fileName)
		return when (val content = file.content) {
			is Workflow -> (content.jobs.values.single() as Job.NormalJob).steps[index]
			is Action -> (content.runs as Action.Runs.CompositeRuns).steps[index]
			is InvalidContent -> error("Invalid content: ${content.error}")
		}
	}

	private fun test(@Language("yaml") yaml: String, fileName: String = "test.yml", stepsLength: Int, stepIndex: Int) {
		val step = loadStep(stepIndex, yaml, fileName)

		withClue("preconditions") {
			step.index.value shouldBe stepIndex
			step.parent.steps should haveSize(stepsLength)
			step.parent.steps should haveElementAt(step.index.value, step)
		}
		withClue("invariants") {
			step.parent.steps should startWith(step.stepsBefore)
			step.parent.steps should endWith(step.stepsAfter)
			step.stepsBefore.size + 1 + step.stepsAfter.size shouldBe stepsLength
		}
		withClue("extra safety") {
			step.stepsBefore should beUnique()
			step.stepsAfter should beUnique()
			step.stepsBefore shouldNot contain(step)
			step.stepsAfter shouldNot contain(step)
		}
	}

	@Test fun `single uses step`() {
		test(
			yaml = """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: some/action@v1
			""".trimIndent(),
			stepsLength = 1,
			stepIndex = 0,
		)
	}

	@Test fun `single run step`() {
		test(
			yaml = """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo Test
			""".trimIndent(),
			stepsLength = 1,
			stepIndex = 0,
		)
	}

	@ValueSource(ints = [0, 1])
	@ParameterizedTest fun `two run steps`(stepIndex: Int) {
		test(
			yaml = """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - run: echo Test1
				      - run: echo Test2
			""".trimIndent(),
			stepsLength = 2,
			stepIndex = stepIndex,
		)
	}

	@ValueSource(ints = [0, 1, 2, 3])
	@ParameterizedTest fun `standard gradle build`(stepIndex: Int) {
		test(
			yaml = """
				on: push
				jobs:
				  test:
				    runs-on: test
				    steps:
				      - uses: actions/checkout@v4
				      - uses: gradle/actions/setup-gradle@v3
				      - run: gradlew build
				      - uses: actions/upload-artifact@v4
			""".trimIndent(),
			stepsLength = 4,
			stepIndex = stepIndex,
		)
	}

	@ValueSource(ints = [0, 1, 2, 3])
	@ParameterizedTest fun `standard gradle build as reusable action`(stepIndex: Int) {
		test(
			yaml = """
				name: Test
				description: Test
				runs:
				  using: composite
				  steps:
				    - uses: actions/checkout@v4
				    - uses: gradle/actions/setup-gradle@v3
				    - run: gradlew build
				      shell: bash
				    - uses: actions/upload-artifact@v4
			""".trimIndent(),
			fileName = "action.yml",
			stepsLength = 4,
			stepIndex = stepIndex,
		)
	}
}
