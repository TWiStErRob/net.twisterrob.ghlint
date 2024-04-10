# Hide the following message:
# > Info in ....jar:META-INF/com.android.tools/r8/kotlinx-serialization-common.pro at line 17, column 1:
# > Proguard configuration rule does not match anything: `-if @kotlinx.serialization.Serializable class ** {
# >   public static ** INSTANCE;
# > }
# > -keepclassmembers class <1> {
# >   public static <1> INSTANCE;
# >   kotlinx.serialization.KSerializer serializer(...);
# > }`
# The ProguardWorkaround class is not used anywhere, but here.
# Need to keep it so that the `if` can match.
-keep class net.twisterrob.ghlint.reporting.sarif.ProguardWorkaround
