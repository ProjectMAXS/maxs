BASE := $(shell readlink -f ../)

ifneq ($(MAXS_BUILD_SYSTEM),gradle)
DEBUG_APK := bin/maxs-main-debug.apk
else
DEBUG_APK := build/outputs/apk/debug/main-debug.apk
endif

BUILD_TARGET ?= debug

.PHONY: all clean deploy distclean eclipse prebuild release resources setupResourceLinks $(DEBUG_APK)

all: main eclipse

include $(BASE)/build/common.mk

# Android lint needs to be run *after* the project has been build
main: prebuild
ifneq ($(MAXS_BUILD_SYSTEM),gradle)
	ant $(ANT_ARGS) $(BUILD_TARGET)
	$(MAKE) lint-report.html
else
# Ugly hack to capitalize first letter included. See https://stackoverflow.com/a/50114483/194894
	gradle $(GRADLE_EXTRA_ARGS) assemble$(shell BUILD_TARGET=$(BUILD_TARGET); echo $${BUILD_TARGET^})
endif

prebuild: build.xml resources $(VERSION_XML)

build.xml:
	android update project -p . -n maxs-main -s
#	tools/android may fail but not set its exit status
	test -f build.xml

resources: setupResourceLinks
	cd res-src && $(MAKE)

release:
	make main BUILD_TARGET=release

deploy: $(DEBUG_APK)
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

$(DEBUG_APK):
	$(MAKE) main BUILD_TARGET=debug

setupResourceLinks:
	$(BASE)/scripts/setupResourceLinks.sh -c $(CURDIR)

ifneq ($(MAXS_BUILD_SYSTEM),gradle)
clean: build.xml
else
clean: build.gradle
endif

clean:
ifneq ($(MAXS_BUILD_SYSTEM),gradle)
	ant clean
else
	gradle clean
endif

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
