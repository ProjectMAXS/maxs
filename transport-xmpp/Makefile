TRANSPORT_NAME := $(shell basename `pwd`)
BASE := $(shell readlink -f ../)
MAIN := $(BASE)/main
SHARED := $(BASE)/shared
SHARED_SOURCE := src/org/projectmaxs/shared
SHARED_GLOBAL_SOURCE := $(SHARED_SOURCE)/global
SHARED_TRANSPORT_SOURCE := $(SHARED_SOURCE)/transport
SHARED_MAINTRANSPORT_SOURCE := $(SHARED_SOURCE)/maintransport
SHARED_RES_SRC_MAKEFILE := res-src/Makefile
DEBUG_APK := bin/maxs-$(TRANSPORT_NAME)-debug.apk
ANT_BUILD_TARGET ?= debug

.PHONY: all asmack clean deploy distclean eclipse prebuild release resources shared $(DEBUG_APK)

all: transport

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

clean:
	ant clean

distclean: clean
	cd res-src && $(MAKE) clean

prebuild: asmack resources shared

resources: shared
	cd res-src && $(MAKE)

shared: $(SHARED_GLOBAL_SOURCE) $(SHARED_MAINTRANSPORT_SOURCE) $(SHARED_TRANSPORT_SOURCE) $(SHARED_RES_SRC_MAKEFILE)

$(SHARED_SOURCE):
	mkdir $@

$(SHARED_GLOBAL_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $(SHARED_SOURCE)

$(SHARED_MAINTRANSPORT_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $(SHARED_SOURCE)

$(SHARED_TRANSPORT_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(SHARED)/transport $(SHARED_SOURCE)

$(SHARED_RES_SRC_MAKEFILE): $(MAIN)/res-src/Makefile
	[ -d res-src ] || mkdir res-src
	cp $^ $@

eclipse: .settings .classpath .project shared

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
