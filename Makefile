MODULES := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'module-*')
TRANSPORTS := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'transport-*')
MODULES_MAKEFILE := $(foreach mod, $(MODULES), $(mod)/Makefile)
MIN_DEPLOY := main module-bluetooth transport-xmpp
ALL := main $(MODULES) $(TRANSPORTS)
ALL_NON_ROOT := $(filter-out ./module-phonestatemodify, $(ALL))
TABLET_DEPLOY := $(filter-out ./module-sms% ./module-phone%, $(ALL))
NPROC := $(shell nproc)
JOBS := $(shell echo $$(( $(NPROC) + 1)))
MAKE_PARALLEL_ARGS := -j$(JOBS) -l$(NPROC)

.PHONY: all $(ALL) clean distclean deplyg eclipse homepage makefiles mindeploy parallel parclean pardeploy parrelease prebuild release tabletdeploy

all: $(ALL)

clean:
	TARGET=$@ $(MAKE) $(ALL)

parclean:
	TARGET=clean $(MAKE) $(MAKE_PARALLEL_ARGS) $(ALL)

distclean:
	TARGET=$@ $(MAKE) $(ALL)

pardistclean:
	TARGET=distclean $(MAKE) $(MAKE_PARALLEL_ARGS) $(ALL)

deploy:
	TARGET=$@ $(MAKE) $(ALL_NON_ROOT)

homepage:
	$(MAKE) -C homepage

pardeploy:
	TARGET=deploy $(MAKE) $(MAKE_PARALLEL_ARGS)  $(ALL_NON_ROOT)

eclipse:
	TARGET=$@ $(MAKE) $(ALL)

mindeploy:
	TARGET=deploy $(MAKE) $(MIN_DEPLOY)

tabletdeploy:
	TARGET=deploy $(MAKE) $(TABLET_DEPLOY)

parallel:
	$(MAKE) $(MAKE_PARALLEL_ARGS)

release:
	TARGET=$@ $(MAKE) $(ALL)

parrelease:
	TARGET=release $(MAKE) $(MAKE_PARALLEL_ARGS) $(ALL)

prebuild:
	TARGET=prebuild $(MAKE) $(ALL)

makefiles: $(MODULES_MAKEFILE)

$(ALL): makefiles
	$(MAKE) -C $@ $(TARGET)

module-%/Makefile:
	 ln -rs build/module-makefile $@
