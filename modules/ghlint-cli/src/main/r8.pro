# Keep entry point of CLI.
-keep public class net.twisterrob.ghlint.cli.MainKt {
	public static void main(java.lang.String[]);
}

# Try to emulate --debug until it can be turned on (https://issuetracker.google.com/issues/328859009)
-keepattributes SourceFile,LineNumberTable

# TODEL once https://github.com/ajalt/mordant/issues/233 is resolved somehow.
# Release https://github.com/ajalt/mordant/releases/tag/3.0.0
# > Added new terminal implementation that uses the Foreign Function and Memory (FFM) API added in JDK 22.
# R8 uses the executing JDK to determine the target classfile version, which is low.
# R8 uses the -lib option to find the JDK's rt.jar, which does not seem to solve the problem.
# exclude(group: "com.github.ajalt.mordant", "module": "mordant-jvm-ffm-jvm") is an option,
# but I went with suppressing the ffm problems and running JVM matrix tests to make sure it's OK to do so.
-dontwarn com.github.ajalt.mordant.terminal.terminalinterface.ffm.*
# This needs to be explicitly suppressed because the error is not coming from ffm:
# > Missing class java.lang.foreign.MemorySegment (referenced from: java.lang.Object java.lang.invoke.MethodHandle.invoke(java.lang.Object[]) and 3 other contexts)]
-dontwarn java.lang.foreign.MemorySegment
