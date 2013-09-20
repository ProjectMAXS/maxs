SVGS := $(shell find . -type f -name '*.svg')
PNGS := $(foreach png,$(SVGS:.svg=.png),../res/$(png))

.PHONEY: all clean

all: $(PNGS)

clean:
	rm -f $(PNGS)

../res/%.png: %.svg
	mkdir -p $(dir $@)
	convert $< $@
