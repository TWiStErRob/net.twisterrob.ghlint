## Project Setup

```shell
python -m venv .venv
.venv\Scripts\activate
pip install mkdocs-material
pip --require-virtualenv --isolated freeze > requirements.txt
mkdocs new .
```
