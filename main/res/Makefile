SVGS := $(shell find . -type f -name '*.svg')
PNGS := $(SVGS:.svg=.png)

.PHONEY: all clean

all: $(PNGS)

clean:
	rm -f $(PNGS)

%.png : %.svg
	convert $< $@
