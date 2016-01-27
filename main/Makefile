BASE := $(shell readlink -f ../)
DEBUG_APK := bin/maxs-main-debug.apk
ANT_BUILD_TARGET ?= debug

.PHONY: all asmack clean deploy distclean eclipse prebuild release resources setupResourceLinks $(DEBUG_APK)

all: main eclipse

include $(BASE)/build/common.mk

# Android lint needs to be run *after* the project has been build
main: prebuild
	ant $(ANT_ARGS) $(ANT_BUILD_TARGET)
	$(MAKE) lint-report.html

prebuild: build.xml resources $(VERSION_XML)

build.xml:
	android update project -p . -n maxs-main -s
#	tools/android may fail but not set its exit status
	test -f build.xml

resources: setupResourceLinks
	cd res-src && $(MAKE)

release:
	make main ANT_BUILD_TARGET=release

deploy: $(DEBUG_APK)
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

$(DEBUG_APK):
	$(MAKE) main ANT_BUILD_TARGET=debug

setupResourceLinks:
	$(BASE)/scripts/setupResourceLinks.sh -c $(CURDIR)

clean: build.xml
	ant clean

distclean: clean lintClean
	[ ! -f res-src/Makefile ] || cd res-src && $(MAKE) clean
	rm -f build.xml
	rm -f $(VERSION_XML)

eclipse: .settings .classpath .project

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
