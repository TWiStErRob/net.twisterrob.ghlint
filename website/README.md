# GH-Lint Website

See it live at https://ghlint.twisterrob.net/.

## Testing the website locally

Run once to set up a Python Virtual Environment:

```shell
python -m venv .venv
# python: No module named venv -> Use Python 3.3+.
.venv\Scripts\activate
pip install -r requirements.txt
```

Run Gradle `:website:generateDocs` task to generate some files in `docs`.

Run MkDocs to serve the website:

```shell
mkdocs serve
```

## Original Project Setup

Useful reads:

1. [`requirements.txt`](https://www.freecodecamp.org/news/python-requirementstxt-explained/)
2. [`pip-compile` and `requirements.in`](https://pip-tools.readthedocs.io/en/latest/)
   ([Why is this better then pip freeze?](https://stackoverflow.com/a/66828887/253468))
3. [Renovate's `pip-compile` manager](https://docs.renovatebot.com/modules/manager/pip-compile/)

```shell
python -m venv .venv
.venv\Scripts\activate
python -m pip install pip-tools
pip-compile --strip-extras --pip-args "--require-virtualenv --isolated" requirements.in --output-file=requirements.txt
pip install -r requirements.txt
mkdocs new .
```
