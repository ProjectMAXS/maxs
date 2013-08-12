ANT_BUILD_TARGET?=debug
DEBUG_APK := bin/MAXS-debug.apk

.PHONY: all asmack clean deploy eclipse $(DEBUG_APK)

all: main

main:
	ant $(ANT_BUILD_TARGET)

deploy: .lastDeployed

$(DEBUG_APK):
	make main ANT_BUILD_TARGET=debug

.lastDeployed: $(DEBUG_APK)
	touch $@
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

clean:
	ant clean

eclipse: .settings .classpath .project

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
