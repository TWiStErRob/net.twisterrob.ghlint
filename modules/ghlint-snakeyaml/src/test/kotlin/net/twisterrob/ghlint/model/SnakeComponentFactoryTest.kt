package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.instanceOf
import net.twisterrob.ghlint.testing.file
import net.twisterrob.ghlint.testing.yaml
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

private val file = yaml("content", "test.yml")

class SnakeComponentFactoryTest {

	private fun subject(file: RawFile = net.twisterrob.ghlint.model.file): SnakeComponentFactory =
		SnakeComponentFactory(file)

	@Nested
	inner class `createFile Test` {

		@Test fun `creates SnakeFile`() {
			val subject = subject()
			val content = mock<InvalidContent>()

			val spy = spy(subject)
			doReturn(content).whenever(spy).createContent(any(), any())

			val result = spy.createFile(file)

			result.expectFileContent<Content>(file) shouldBe content
			inOrder(spy) {
				verify(spy).createFile(file)
				verify(spy).loadYaml(file)
				verify(spy).createContent(any(), any())
				verifyNoMoreInteractions()
			}
		}

		@Test fun `captures syntax error`() {
			val subject = subject()
			val spy = spy(subject)
			val syntaxError = RuntimeException("Syntax error")
			doThrow(syntaxError).whenever(spy).loadYaml(file)

			val result = spy.createFile(file)

			val content: SnakeSyntaxErrorContent = result.expectFileContent(file)
			content.error shouldBe syntaxError
			inOrder(spy) {
				verify(spy).createFile(file)
				verify(spy).loadYaml(file)
				verifyNoMoreInteractions()
			}
		}

		@Test fun `creates invalid content for unknown file`() {
			val file = file("content", "test.unknown")
			val subject = subject(file)

			val result = subject.createFile(file)

			val content: SnakeUnknownContent = result.expectFileContent(file)
			content.error shouldHaveMessage "Unknown file type of test.unknown"
		}

		private inline fun <reified T : Content> File.expectFileContent(file: RawFile): T {
			this shouldBe instanceOf<SnakeFile>()
			origin shouldBe file
			location shouldBe file.location
			content shouldBe instanceOf<T>()
			return content as T
		}
	}
}
