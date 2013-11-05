BASE := $(shell readlink -f ../)
DEBUG_APK := bin/maxs-main-debug.apk
ANT_BUILD_TARGET ?= debug

.PHONY: all asmack clean deploy distclean eclipse prebuild release resources setupResourceLinks $(DEBUG_APK)

all: main

main: prebuild
	ant $(ANT_ARGS) $(ANT_BUILD_TARGET)

prebuild: build.xml resources

build.xml:
	android update project -p . -n maxs-main

resources: setupResourceLinks
	cd res-src && $(MAKE)

release:
	make main ANT_BUILD_TARGET=release

deploy: $(DEBUG_APK)
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

$(DEBUG_APK):
	make main ANT_BUILD_TARGET=debug

setupResourceLinks:
	$(BASE)/scripts/setupResourceLinks.sh -c main

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
