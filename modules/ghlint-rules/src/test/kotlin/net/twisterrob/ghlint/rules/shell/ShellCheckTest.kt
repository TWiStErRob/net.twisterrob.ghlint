package net.twisterrob.ghlint.rules.shell

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ShellCheckTest {

	@Test fun test(@TempDir tempDir: Path, @TempDir workingDir: Path) {
		ShellCheck(workingDirectory = workingDir, tempDirectory = tempDir)
			.check("echo ${'$'}FOO", "bash")
	}
}
