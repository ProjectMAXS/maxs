MODULES := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'module-*')
TRANSPORTS := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'transport-*')
MODULES_MAKEFILE := $(foreach mod, $(MODULES), $(mod)/Makefile)
MIN_DEPLOY := main module-bluetooth transport-xmpp
ALL := main $(MODULES) $(TRANSPORTS)
TABLET_DEPLOY := $(filter-out ./module-sms% ./module-phone%, $(ALL))
CPUS := $(shell grep -c ^processor /proc/cpuinfo)

.PHONY: all $(ALL) clean distclean deplyg eclipse homepage makefiles mindeploy parallel parclean pardeploy parrelease prebuild release tabletdeploy

all: $(ALL)

clean:
	TARGET=$@ $(MAKE) $(ALL)

parclean:
	TARGET=clean $(MAKE) -j$(CPUS) $(ALL)

distclean:
	TARGET=$@ $(MAKE) $(ALL)
	[ ! -d .git ] || git clean -x -d -f

deploy:
	TARGET=$@ $(MAKE) $(ALL)

homepage:
	$(MAKE) -C homepage

pardeploy:
	TARGET=deploy $(MAKE) -j$(CPUS) $(ALL)

eclipse:
	TARGET=$@ $(MAKE) $(ALL)

mindeploy:
	TARGET=deploy $(MAKE) $(MIN_DEPLOY)

tabletdeploy:
	TARGET=deploy $(MAKE) $(TABLET_DEPLOY)

parallel:
	$(MAKE) -j$(CPUS)

release:
	TARGET=$@ $(MAKE) $(ALL)

parrelease:
	TARGET=release $(MAKE) -j$(CPUS) $(ALL)

prebuild:
	TARGET=prebuild $(MAKE) $(ALL)

makefiles: $(MODULES_MAKEFILE)

$(ALL): makefiles
	$(MAKE) -C $@ $(TARGET)

module-%/Makefile:
	 ln -rs build/module-makefile $@
