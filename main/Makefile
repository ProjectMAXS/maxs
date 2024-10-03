BASE := $(shell readlink -f ../)

DEBUG_APK := build/outputs/apk/debug/main-debug.apk

BUILD_TARGET ?= debug

.PHONY: all clean distclean prebuild release resources setupResourceLinks $(DEBUG_APK)

all: main

include $(BASE)/build/common.mk

# Android lint needs to be run *after* the project has been build
main: prebuild
# Ugly hack to capitalize first letter included. See https://stackoverflow.com/a/50114483/194894
	$(GRADLE) $(GRADLE_EXTRA_ARGS) assemble$(shell BUILD_TARGET=$(BUILD_TARGET); echo $${BUILD_TARGET^})

prebuild: lint.xml resources $(VERSION_XML)

resources: setupResourceLinks
	cd res-src && $(MAKE)

release:
	make main BUILD_TARGET=release

$(DEBUG_APK):
	$(MAKE) main BUILD_TARGET=debug

setupResourceLinks:
	$(BASE)/scripts/setupResourceLinks.sh -c $(CURDIR)

clean:
	$(GRADLE) clean

distclean: clean lintClean
	[ ! -f res-src/Makefile ] || cd res-src && $(MAKE) clean
	rm -f $(VERSION_XML)
