ANT_BUILD_TARGET?=debug
DEBUG_APK := bin/maxs-main-debug.apk

.PHONY: all asmack clean deploy distclean eclipse prebuild release resources $(DEBUG_APK)

all: main

main: prebuild
	ant $(ANT_ARGS) $(ANT_BUILD_TARGET)

prebuild: build.xml resources

build.xml:
	android update project -p . -n maxs-main

resources:
	cd res-src && $(MAKE)

release:
	make main ANT_BUILD_TARGET=release

deploy: $(DEBUG_APK)
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

$(DEBUG_APK):
	make main ANT_BUILD_TARGET=debug

clean:
	ant clean

distclean: clean
	cd res-src && $(MAKE) clean
	rm -f build.xml

eclipse: .settings .classpath .project

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
