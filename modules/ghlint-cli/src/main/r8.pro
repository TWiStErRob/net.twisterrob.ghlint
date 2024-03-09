# Keep entry point of CLI.
-keep public class net.twisterrob.ghlint.cli.MainKt {
	public static void main(java.lang.String[]);
}

# Try to emulate --debug until it can be turned on (https://issuetracker.google.com/issues/328859009)
-keepattributes SourceFile,LineNumberTable
