TRANSPORT_NAME := $(shell basename `pwd`)
BASE := $(shell readlink -f ../)
MAIN := $(BASE)/main
SHARED := $(BASE)/shared
SHARED_SOURCE := src/org/projectmaxs/shared
SHARED_GLOBAL_SOURCE := $(SHARED_SOURCE)/global
SHARED_TRANSPORT_SOURCE := $(SHARED_SOURCE)/transport
SHARED_MAINTRANSPORT_SOURCE := $(SHARED_SOURCE)/maintransport

ifneq ($(MAXS_BUILD_SYSTEM),gradle)
DEBUG_APK := bin/maxs-$(TRANSPORT_NAME)-debug.apk
else
DEBUG_APK := build/outputs/apk/debug/$(TRANSPORT_NAME)-debug.apk
endif

BUILD_TARGET ?= debug

.PHONY: all clean deploy distclean eclipse prebuild release resources setupResourceLinks shared $(DEBUG_APK)

all: transport eclipse

include $(BASE)/build/common.mk

# Android lint needs to be run *after* the project has been build
transport: prebuild
ifneq ($(MAXS_BUILD_SYSTEM),gradle)
	ant $(ANT_ARGS) $(BUILD_TARGET)
	$(MAKE) lint-report.html
else
# Ugly hack to capitalize first letter included. See https://stackoverflow.com/a/50114483/194894
	gradle $(GRADLE_EXTRA_ARGS) assemble$(shell BUILD_TARGET=$(BUILD_TARGET); echo $${BUILD_TARGET^})
endif

release:
	BUILD_TARGET=release $(MAKE) transport

deploy: $(DEBUG_APK)
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

$(DEBUG_APK):
	$(MAKE) transport BUILD_TARGET=debug

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

prebuild: build.xml resources shared $(VERSION_XML)

build.xml:
	android update project -p . -n maxs-$(TRANSPORT_NAME) -s
#	tools/android may fail but not set its exit status
	test -f build.xml

resources: shared setupResourceLinks
	cd res-src && $(MAKE)

shared: $(SHARED_GLOBAL_SOURCE) $(SHARED_MAINTRANSPORT_SOURCE) $(SHARED_TRANSPORT_SOURCE)

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


MTM_PATH := libraryProjects/memorizingTrustManager

eclipse: .settings .classpath .project prebuild $(MTM_PATH)/.project $(MTM_PATH)/.classpath

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project

$(MTM_PATH)/.project:
	ln -rs build/eclipse/project-memorizingTrustManager $@

$(MTM_PATH)/.classpath:
	ln -rs build/eclipse/classpath-memorizingTrustManager $@
