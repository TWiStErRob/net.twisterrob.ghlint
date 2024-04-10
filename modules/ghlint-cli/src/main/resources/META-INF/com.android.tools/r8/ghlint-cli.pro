# It was working before without these missing classes, it should keep working now.
# Missing class com.oracle.svm.core.annotate.Delete (referenced from: com.github.ajalt.mordant.internal.*)
# Missing class org.graalvm.nativeimage.* (referenced from: com.github.ajalt.mordant.internal.nativeimage.*)
-dontwarn com.github.ajalt.mordant.internal.**

# Keep annotations, some of them are reflected on.
# > java.lang.Error: Structure.getFieldOrder() on class com.github.ajalt.mordant.internal.jna.WinKernel32Lib$COORD
# >                  does not provide enough names [0] ([]) to match declared fields [2] ([X, Y])
# >     at com.sun.jna.Structure.getFields(SourceFile:1110)
# >     at ...
# >     at com.sun.jna.Structure.<init>(SourceFile:183)
# >     at com.github.ajalt.mordant.internal.jna.WinKernel32Lib$CONSOLE_SCREEN_BUFFER_INFO.<init>(SourceFile:50)
# >     at ...
# >     at com.github.ajalt.mordant.terminal.Terminal.<init>(SourceFile:45)
# >     at com.github.ajalt.clikt.core.Context$Builder.<init>(SourceFile:227)
# >     at com.github.ajalt.clikt.core.CliktCommand.createContext(SourceFile:312)
# >     at com.github.ajalt.clikt.core.CliktCommand.parse$default(SourceFile:455)
# >     at net.twisterrob.ghlint.cli.MainKt.main(SourceFile:474)
-keepattributes RuntimeVisibleAnnotations
