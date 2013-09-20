TRANSPORT_NAME := $(shell basename `pwd`)
BASE := $(shell readlink -f ../)
MAIN := $(BASE)/main/
SHARED := $(BASE)/shared/
SHARED_SOURCE := src/org/projectmaxs/shared
SHARED_GLOBAL_SOURCE := $(SHARED_SOURCE)/global
SHARED_TRANSPORT_SOURCE := $(SHARED_SOURCE)/transport
SHARED_MAINTRANSPORT_SOURCE := $(SHARED_SOURCE)/maintransport
DEBUG_APK := bin/maxs-$(TRANSPORT_NAME)-debug.apk
ANT_BUILD_TARGET ?= debug

.PHONY: all asmack clean deploy distclean eclipse release shared $(DEBUG_APK)

all: transport

transport: asmack shared
	ant $(ANT_BUILD_TARGET)

release:
	ANT_BUILD_TARGET=release $(MAKE) transport

asmack:
	./asmack.sh

deploy: .lastDeployed

$(DEBUG_APK):
	$(MAKE) transport ANT_BUILD_TARGET=debug

.lastDeployed: $(DEBUG_APK)
	touch $@
	adb $(ADB_ARGS) install -r $(DEBUG_APK)

clean:
	ant clean

distclean: clean

shared: $(SHARED_GLOBAL_SOURCE) $(SHARED_MAINTRANSPORT_SOURCE) $(SHARED_TRANSPORT_SOURCE)

$(SHARED_SOURCE):
	mkdir $@

$(SHARED_GLOBAL_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $(SHARED_SOURCE)

$(SHARED_MAINTRANSPORT_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $(SHARED_SOURCE)

$(SHARED_TRANSPORT_SOURCE): | $(SHARED_SOURCE)
	ln -rs $(SHARED)/transport $(SHARED_SOURCE)

eclipse: .settings .classpath .project shared

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
