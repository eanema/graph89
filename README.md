# Graph 89

Graph89 is an emulator targeting the Android platform for TI89, TI89T, TI92, TI92+, V200, TI84+, TI84+SE, TI83, TI83+ and TI83+SE calculators.

This is a continuation of the awesome work done by Dritan Hashorva to bring a bit of UI polish and modern feature enhancements to the project.
It was forked from the [last version distributed by the original author](https://bitbucket.org/dhashoandroid/graph89-paid/).

Kudos to him and everyone involved in the TI emulation projects, especially the TiEmu and TilEm developers.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.eanema.graph89/)

Or download the latest APK from the [Releases Section](https://github.com/eanema/graph89/releases/latest).

## This Fork

This is a fork from https://github.com/milaq/graph89 to add support for 64-bit android/x86 platforms. 
The important change for 64-bit support was to patch glib to use 64-bit integers for gsize & gssize. 
There was a bit of warning clean-up (there are still hundreds of warnings in the arm64-v8a build but the 
app appears to work). After that the ROM manager was updated to support Android SDK 34 and the scoped storage paradigm.

## Implementation

Graph89 consists of 4 distinct parts.

1. 	TILP libraries located under jni/libticables2-1.3.3  jni/libticalcs2-1.1.7  jni/libticonv-1.1.3  jni/libtifiles2-1.1.5 
	Each library has its own copyright notice located in the root of the library
	Note that the TILP libraries included in Graph89 are not in their original state. Certain parts might be removed or modified or modified for different reasons.
	The original TILP libraries are located in http://lpg.ticalc.org/prj_tilp/

2. 	TiEmu library located under jni/tiemu-3.03
	The library has its own copyright notice located in jni/tiemu-3.03
	Note that the TiEmu library included in Graph89 is not in its original state. TiEmu is modified to compile in the Android OS. Certain parts might be removed or modified for different reasons.
	The original TiEmu library is located in http://lpg.ticalc.org/prj_tiemu/

3. 	TilEm library located under jni/tilem-2.0
	The library has its own copyright notice located in jni/tilem-2.0
	Note that the TilEm library included in Graph89 is not in its original state. TiEmu is modified to compile in the Android OS. Certain parts might be removed or modified or modified for different reasons.
	The original TilEm library is located in http://lpg.ticalc.org/prj_tilem/

4. 	Graph89 Android. This is the UI layer running in the Android OS. 
	Graph89 is licensed with a GPL V3 license.
