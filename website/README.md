# GH-Lint Website

See it live at https://ghlint.twisterrob.net/.

## Testing the website locally
Run once to set up a Python Virtual Environment:
```shell
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```
Run Gradle `:ghlint-docs:run` task to generate the files in `docs/issues`:
```shell
ghlint$ gradlew .. :ghlint-docs:run
# or 
website$ ..\gradlew -p .. :ghlint-docs:run
```
Run MkDocs to serve the website:
```shell
mkdocs serve
```

## Original Project Setup

Useful read: https://www.freecodecamp.org/news/python-requirementstxt-explained/

```shell
python -m venv .venv
.venv\Scripts\activate
pip install mkdocs-material
pip --require-virtualenv --isolated freeze > requirements.txt
mkdocs new .
```
