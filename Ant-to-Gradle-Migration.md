Project MAXS is currently in the phase of migration for Apache Ant using the Android Development Tools (ADT) for Eclipse towards the Android Plugin for Gradle and Android Studio.

Once the transition to Gradle is complete, remove the traces of

- All eclipse related make targets
- All ant related stuff in make and the various scripts
- MAXS_BUILD_SYSTEM
- Artifacts and MavenToAndroidAnt
- The Ant build.xml and similar .xml files
- custom-rules.xml
- This file
