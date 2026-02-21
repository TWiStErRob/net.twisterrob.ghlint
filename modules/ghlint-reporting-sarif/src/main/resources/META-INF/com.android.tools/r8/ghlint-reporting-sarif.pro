# Hide the following message:
# > > Task :ghlint-cli:r8Jar
# > Info in ....jar:META-INF/com.android.tools/r8/kotlinx-serialization-common.pro at line 9, column 1:
# > Proguard configuration rule does not match anything:
# > `-if @kotlinx.serialization.internal.NamedCompanion class *
# > -keepclassmembers class * {
# >   static <1> *;
# > }`
# > Info in ....jar:META-INF/com.android.tools/r8/kotlinx-serialization-r8.pro at line 16, column 1:
# > Proguard configuration rule does not match anything:
# > `-if @kotlinx.serialization.internal.NamedCompanion class *
# > -keep,allowaccessmodification,allowrepackaging,allowobfuscation,allowshrinking,allowoptimization class <1> {
# >   <init>();
# > }`
# @NamedCompanion is added by the compiler plugin to companions with a non-default name.
# The class is manually annotated, so the `if` can match.
# The ProguardWorkaround class is not used anywhere, but here.
-keep class net.twisterrob.ghlint.reporting.sarif.internal.ProguardWorkaroundForNamedCompanion
