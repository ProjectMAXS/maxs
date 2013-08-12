MODULES := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'module-*')
TRANSPORTS := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'transport-*')
MODULES_MAKEFILE := $(foreach mod, $(MODULES), $(mod)/Makefile)
MIN_DEPLOY := main module-bluetooth transport-xmpp
ALL := main $(MODULES) $(TRANSPORTS)

.PHONY: all $(ALL) eclipse clean mindeploy

all: $(ALL)

eclipse:
	TARGET=$@ make $(ALL)

clean:
	TARGET=$@ make $(ALL)

deploy:
	TARGET=$@ make $(ALL)

mindeploy:
	TARGET=deploy make $(MIN_DEPLOY)

$(ALL): $(MODULES_MAKEFILE)
	make -C $@ $(TARGET)

module-%/Makefile:
	 ln -s ../build/module-makefile $@
