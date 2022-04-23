TRANSPORT_NAME := $(shell basename `pwd`)
BASE := $(shell readlink -f ../)
MAIN := $(BASE)/main
SHARED := $(BASE)/shared
SHARED_SOURCE := src/org/projectmaxs/shared
SHARED_GLOBAL_SOURCE := $(SHARED_SOURCE)/global
SHARED_TRANSPORT_SOURCE := $(SHARED_SOURCE)/transport
SHARED_MAINTRANSPORT_SOURCE := $(SHARED_SOURCE)/maintransport

DEBUG_APK := build/outputs/apk/debug/$(TRANSPORT_NAME)-debug.apk

BUILD_TARGET ?= debug

.PHONY: all clean deploy distclean prebuild release resources setupResourceLinks shared $(DEBUG_APK)

all: transport

include $(BASE)/build/common.mk

# Android lint needs to be run *after* the project has been build
transport: prebuild
	$(MAKE) lint-results.html
# Ugly hack to capitalize first letter included. See https://stackoverflow.com/a/50114483/194894
	$(GRADLE) $(GRADLE_EXTRA_ARGS) assemble$(shell BUILD_TARGET=$(BUILD_TARGET); echo $${BUILD_TARGET^})

release:
	BUILD_TARGET=release $(MAKE) transport

deploy: $(DEBUG_APK)
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

$(DEBUG_APK):
	$(MAKE) transport BUILD_TARGET=debug

clean: build.gradle
	$(GRADLE) clean

distclean: clean lintClean
	[ ! -f res-src/Makefile ] || cd res-src && $(MAKE) clean
	rm -f $(VERSION_XML)

prebuild: lint.xml resources shared $(VERSION_XML)

resources: shared setupResourceLinks
	cd res-src && $(MAKE)

shared: $(SHARED_GLOBAL_SOURCE) $(SHARED_MAINTRANSPORT_SOURCE) $(SHARED_TRANSPORT_SOURCE) gradlew-symlinks

$(SHARED_SOURCE):
	mkdir $@

$(SHARED_GLOBAL_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $(SHARED_SOURCE)

$(SHARED_MAINTRANSPORT_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $(SHARED_SOURCE)

$(SHARED_TRANSPORT_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(SHARED)/transport $(SHARED_SOURCE)

setupResourceLinks:
	$(BASE)/scripts/setupResourceLinks.sh -c $(CURDIR)
