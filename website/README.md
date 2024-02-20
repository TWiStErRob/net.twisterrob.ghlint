# GH-Lint Website

See it live at https://ghlint.twisterrob.net/.

## Project Setup

Useful read: https://www.freecodecamp.org/news/python-requirementstxt-explained/

```shell
python -m venv .venv
.venv\Scripts\activate
pip install mkdocs-material
pip --require-virtualenv --isolated freeze > requirements.txt
mkdocs new .
```

## Generated files in `docs/issues`
See Gradle `:ghlint-docs:run` task.
