# Contribution Guidelines

## IDE

IntelliJ IDEA Community / Ultimate. (Please report any issues with Community as I'm using Ultimate.)

Recommended plugins:

* detekt (config file: [config/detekt/detekt.yml](../config/detekt/detekt.yml))
  For a faster feedback loop.
* Markdown
  Syntax highlight of .md files, and rule descriptions.

## Local development

It is recommended to run `gradlew build` before `git push`, as CI will do the same.

## Code style

Notable things:

* The project uses TABs as indent, except for YAML.
* CI / `gradlew build` will tell you off for most things, so reviews don't have to.
* The `.idea` folder is version controlled, this means when you open the project it should pick up:
  * Code styles specific to the project
  * Inspection profile specific to the project

  With these, it is recommended to write code without any warnings.
  Use _menu > Code > Reformat Code_ when editing files, or turn on _Commit Checks > Reformat Code_ in the commit dialog.
