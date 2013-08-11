MODULES := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'module-*')
TRANSPORTS := $(shell find -mindepth 1 -maxdepth 1 -type d -name 'transport-*')
MODULES_MAKEFILE := $(foreach mod, $(MODULES), $(mod)/Makefile)
ALL := main $(MODULES) $(TRANSPORTS)

.PHONY: all $(ALL) eclipse

all: $(ALL)

eclipse:
	TARGET=$@ make $(ALL)

clean:
	TARGET=$@ make $(ALL)

deploy:
	TARGET=$@ make $(ALL)

$(ALL): $(MODULES_MAKEFILE)
	make -C $@ $(TARGET)

module-%/Makefile:
	 ln -s ../build/module-makefile $@
