package net.twisterrob.ghlint.testing

import net.twisterrob.ghlint.model.FileLocation
import net.twisterrob.ghlint.model.RawFile
import org.intellij.lang.annotations.Language

public fun file(content: String, fileName: String): RawFile = RawFile(FileLocation(fileName), content)

public fun yaml(@Language("yaml") content: String, fileName: String = "test.yml"): RawFile = file(content, fileName)

public fun workflow(@Language("yaml") content: String, fileName: String = "test.yml"): RawFile = yaml(content, fileName)
