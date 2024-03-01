package net.twisterrob.ghlint.rule

import net.twisterrob.ghlint.model.File
import net.twisterrob.ghlint.model.Yaml

public interface YamlVisitor {

	public fun visitFile(reporting: Reporting, file: File) {
		visitYaml(reporting, file.content)
	}

	public fun visitYaml(reporting: Reporting, yaml: Yaml) {
		// No children.
	}
}
