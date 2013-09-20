MODULES := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'module-*')
TRANSPORTS := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'transport-*')
MODULES_MAKEFILE := $(foreach mod, $(MODULES), $(mod)/Makefile)
MIN_DEPLOY := main module-bluetooth transport-xmpp
ALL := main $(MODULES) $(TRANSPORTS)

.PHONY: all $(ALL) eclipse clean distclean mindeploy

all: $(ALL)

eclipse:
	TARGET=$@ $(MAKE) $(ALL)

clean:
	TARGET=$@ $(MAKE) $(ALL)

distclean:
	TARGET=$@ $(MAKE) $(ALL)

deploy:
	TARGET=$@ $(MAKE) $(ALL)

release:
	TARGET=release $(MAKE) $(ALL)

mindeploy:
	TARGET=deploy $(MAKE) $(MIN_DEPLOY)

$(ALL): $(MODULES_MAKEFILE)
	cd $@ && $(MAKE) $(TARGET)

module-%/Makefile:
	 ln -s ../build/module-makefile $@
