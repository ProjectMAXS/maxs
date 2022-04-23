BASE := $(shell readlink -f ../)

ifeq ($(MAXS_BUILD_SYSTEM),ant)
DEBUG_APK := bin/maxs-main-debug.apk
else
DEBUG_APK := build/outputs/apk/debug/main-debug.apk
endif

BUILD_TARGET ?= debug

.PHONY: all clean deploy distclean prebuild release resources setupResourceLinks $(DEBUG_APK)

all: main
ifeq ($(MAXS_BUILD_SYSTEM),ant)
.PHONY: eclipse
all: eclipse
endif

include $(BASE)/build/common.mk

# Android lint needs to be run *after* the project has been build
main: prebuild
ifeq ($(MAXS_BUILD_SYSTEM),ant)
	ant $(ANT_ARGS) $(BUILD_TARGET)
	$(MAKE) lint-report.html
else
# Ugly hack to capitalize first letter included. See https://stackoverflow.com/a/50114483/194894
	$(GRADLE) $(GRADLE_EXTRA_ARGS) assemble$(shell BUILD_TARGET=$(BUILD_TARGET); echo $${BUILD_TARGET^})
endif

prebuild: lint.xml resources $(VERSION_XML)

ifeq ($(MAXS_BUILD_SYSTEM),ant)
prebuild: build.xml
build.xml:
	android update project -p . -n maxs-main -s
#	tools/android may fail but not set its exit status
	test -f build.xml
endif

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

ifeq ($(MAXS_BUILD_SYSTEM),ant)
clean: build.xml
else
clean: build.gradle
endif

clean:
ifeq ($(MAXS_BUILD_SYSTEM),ant)
	ant clean
else
endif
	$(GRADLE) clean

distclean: clean lintClean
	[ ! -f res-src/Makefile ] || cd res-src && $(MAKE) clean
ifeq ($(MAXS_BUILD_SYSTEM),ant)
	rm -f build.xml
endif
	rm -f $(VERSION_XML)

ifeq ($(MAXS_BUILD_SYSTEM),ant)
eclipse: .settings .classpath .project

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
endif
