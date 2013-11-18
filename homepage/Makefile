MAXS_SVG := ../main/res-src/drawable-mdpi/ic_maxs.svg

.PHONEY: all assets images publish symlinks

all: publish

publish: assets images symlinks
	../scripts/publish_projectmaxs_homepage.sh

assets:
	../scripts/getAssets.sh -f assets.db

images: maxs.png favicon.png

symlinks:
	../scripts/createSymlinks.sh

maxs.png: $(MAXS_SVG)
	convert -background none $< $@

favicon.png: $(MAXS_SVG)
	convert -background none -resize 32x32 $< $@

