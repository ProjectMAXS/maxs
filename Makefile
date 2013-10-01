MODULES := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'module-*')
TRANSPORTS := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'transport-*')
MODULES_MAKEFILE := $(foreach mod, $(MODULES), $(mod)/Makefile)
MIN_DEPLOY := main module-bluetooth transport-xmpp
ALL := main $(MODULES) $(TRANSPORTS)

.PHONY: all $(ALL) clean distclean eclipse makefiles mindeploy parallel release

all: $(ALL) eclipse

clean:
	TARGET=$@ $(MAKE) $(ALL)

distclean:
	TARGET=$@ $(MAKE) $(ALL)
	[ -d .git ] && git clean -x -d -f

deploy:
	TARGET=$@ $(MAKE) $(ALL)

eclipse:
	TARGET=$@ $(MAKE) $(ALL)

mindeploy:
	TARGET=deploy $(MAKE) $(MIN_DEPLOY)

parallel:
	$(MAKE) -j$(shell grep -c ^processor /proc/cpuinfo)

release:
	TARGET=release $(MAKE) $(ALL)

makefiles: $(MODULES_MAKEFILE)

$(ALL): makefiles
	cd $@ && $(MAKE) $(TARGET)

module-%/Makefile:
	 ln -rs build/module-makefile $@
