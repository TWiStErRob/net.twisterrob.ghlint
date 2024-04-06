# Command Line Interface

GH-Lint comes as an executable JAR file that can be run from the command line.

## Installation

### GitHub Releases

Download the latest "CLI executable" from the [GitHub Releases][releases] page.

[releases]: https://github.com/TWiStErRob/net.twisterrob.ghlint/releases

### GitHub pre-releases

Most commits to the repository are built with GitHub Actions with a ready to use artifact.
To get the latest `-SNAPSHOT` version,
download the "CLI Application" artifact from [one of the workflow runs on `main`][snapshots].
It's possible to download the same artifacts for individual PR commits as well.

[snapshots]: https://github.com/TWiStErRob/net.twisterrob.ghlint/actions/workflows/ci.yml?query=branch%3Amain

## Execution

Regardless of where you downloaded the JAR file, you can run it from the command line.

The JAR file can be run with Java on all operating systems:

```shell
java -jar ghlint.jar
```

On Unix systems (Linux, Mac), you can make the JAR executable and run it directly:

```shell
mv ghlint.jar ghlint
chmod +x ghlint
./ghlint --version
```

On Windows, you can simulate the same behavior with a batch file (`ghlint.bat` in the same folder as the JAR file):

```batch
@echo off
java -jar "%~dp0\ghlint.jar" %*
```

If you put the executable JAR (or BAT) on the `PATH`, you can run it from anywhere:

```shell
ghlint my-workflow.yml
```

## Usage

```text
{{ghlint --help}}
```
