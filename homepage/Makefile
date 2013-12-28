MAXS_SVG := ../main/res-src/drawable-mdpi/ic_maxs.svg

.PHONEY: all assets images maxs-binary publish symlinks

all: publish

publish: assets images symlinks maxs-binary
	../scripts/publish_projectmaxs_homepage.sh

assets:
	../scripts/getAssets.sh -f assets.db

images: maxs.png favicon.png

maxs-binary:
	../scripts/syncRepos.sh -r $@
	$(MAKE) -C ../documentation/images

symlinks:
	../scripts/createSymlinks.sh

maxs.png: $(MAXS_SVG)
	convert -background none $< $@

favicon.png: $(MAXS_SVG)
	convert -background none -resize 32x32 $< $@

