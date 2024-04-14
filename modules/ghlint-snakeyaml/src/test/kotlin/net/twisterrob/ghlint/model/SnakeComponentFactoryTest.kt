package net.twisterrob.ghlint.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import net.twisterrob.ghlint.yaml.SnakeSyntaxErrorContent
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

class SnakeComponentFactoryTest {

	private val subject = SnakeComponentFactory()

	@Nested
	inner class `createFile Test` {

		@Test fun `creates SnakeFile`() {
			val file = RawFile(FileLocation("test.yaml"), "content")
			val content = mock<InvalidContent>()

			val spy = spy(subject)
			doReturn(content).whenever(spy).createContent(any(), any())

			val result = spy.createFile(file)

			result shouldBe instanceOf<SnakeFile>()
			result.origin shouldBe file
			result.location shouldBe file.location
			result.content shouldBe content
			inOrder(spy) {
				verify(spy).createFile(file)
				verify(spy).loadYaml(file)
				verify(spy).createContent(any(), any())
				verifyNoMoreInteractions()
			}
		}

		@Test fun `captures syntax error`() {
			val file = RawFile(FileLocation("test.yaml"), "content")

			val spy = spy(subject)
			val syntaxError = RuntimeException("Syntax error")
			doThrow(syntaxError).whenever(spy).loadYaml(file)

			val result = spy.createFile(file)

			result shouldBe instanceOf<SnakeFile>()
			result.origin shouldBe file
			result.location shouldBe file.location
			result.content shouldBe instanceOf<SnakeSyntaxErrorContent>()
			(result.content as SnakeSyntaxErrorContent).error shouldBe syntaxError
			inOrder(spy) {
				verify(spy).createFile(file)
				verify(spy).loadYaml(file)
				verifyNoMoreInteractions()
			}
		}
	}
}
