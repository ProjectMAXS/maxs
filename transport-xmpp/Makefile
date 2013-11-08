TRANSPORT_NAME := $(shell basename `pwd`)
BASE := $(shell readlink -f ../)
MAIN := $(BASE)/main
SHARED := $(BASE)/shared
SHARED_SOURCE := src/org/projectmaxs/shared
SHARED_GLOBAL_SOURCE := $(SHARED_SOURCE)/global
SHARED_TRANSPORT_SOURCE := $(SHARED_SOURCE)/transport
SHARED_MAINTRANSPORT_SOURCE := $(SHARED_SOURCE)/maintransport
DEBUG_APK := bin/maxs-$(TRANSPORT_NAME)-debug.apk
ANT_BUILD_TARGET ?= debug

.PHONY: all asmack clean deploy distclean eclipse prebuild release resources setupResourceLinks shared $(DEBUG_APK)

all: transport eclipse

transport: prebuild
	ant $(ANT_ARGS) $(ANT_BUILD_TARGET)

release:
	ANT_BUILD_TARGET=release $(MAKE) transport

asmack:
	./asmack.sh

deploy: $(DEBUG_APK)
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

$(DEBUG_APK):
	$(MAKE) transport ANT_BUILD_TARGET=debug

clean: build.xml
	ant clean

distclean: clean
	[ ! -f res-src/Makefile ] || cd res-src && $(MAKE) clean
	rm -f build.xml

prebuild: asmack build.xml resources shared

build.xml:
	android update project -p . -n maxs-$(TRANSPORT_NAME)

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

eclipse: .settings .classpath .project prebuild

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
