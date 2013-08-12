BASE := $(shell readlink -f ../)
MAIN := $(BASE)/main/
SHARED := $(BASE)/shared/
SHARED_SOURCE := src/org/projectmaxs/shared
SHARED_GLOBAL_SOURCE := $(SHARED_SOURCE)/global
SHARED_TRANSPORT_SOURCE := $(SHARED_SOURCE)/transport
SHARED_MAINTRANSPORT_SOURCE := $(SHARED_SOURCE)/maintransport
TRANSPORT_NAME := $(shell basename `pwd`)
DEBUG_APK := bin/maxs-$(TRANSPORT_NAME)-debug.apk
ANT_BUILD_TARGET ?= debug

.PHONY: all clean deploy eclipse shared $(DEBUG_APK)

all: transport

transport: asmack shared
	ant $(ANT_BUILD_TARGET)

asmack: libs-sources libs
	./asmack.sh

libs-sources:
	mkdir $@

libs:
	mkdir $@

deploy: .lastDeployed

$(DEBUG_APK):
	make transport ANT_BUILD_TARGET=debug

.lastDeployed: $(DEBUG_APK)
	touch $@
	adb install -r $(DEBUG_APK)

clean:
	ant clean

shared: $(SHARED_GLOBAL_SOURCE) $(SHARED_MAINTRANSPORT_SOURCE) $(SHARED_TRANSPORT_SOURCE)

$(SHARED_SOURCE):
	mkdir $@

$(SHARED_GLOBAL_SOURCE): $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $@

$(SHARED_MAINTRANSPORT_SOURCE): $(SHARED_SOURCE)
	ln -rs $(MAIN)/$@ $@

$(SHARED_TRANSPORT_SOURCE): $(SHARED_SOURCE)
	ln -rs $(SHARED)/transport $@

eclipse: .settings .classpath .project shared

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
