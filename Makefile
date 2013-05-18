MODULES := $(shell find -name 'module-*' -type d)
MODULES_MAKEFILE := $(foreach mod, $(MODULES), $(mod)/Makefile)
ALL := main $(MODULES)

.PHONY: all $(ALL) eclipse

all: $(ALL)

eclipse:
	TARGET=eclipse make $(ALL)

clean:
	TARGET=clean make $(ALL)

$(ALL): $(MODULES_MAKEFILE)
	make -C $@ $(TARGET)

%/Makefile:
	 ln -s ../build/module-makefile $@

