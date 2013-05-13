MODULES=$(shell find -name 'module-*' -type d)
ALL = main $(MODULES)

.PHONY: all $(ALL) eclipse

all: $(ALL)

eclipse:
	TARGET=eclipse make $(ALL)

clean:
	TARGET=clean make $(ALL)

$(ALL):
	make -C $@ $(TARGET)

