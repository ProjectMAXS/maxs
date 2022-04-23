ifeq ($(CONTRIB), true)
GIT_DIR := $(BASE)/..
else
GIT_DIR := $(BASE)
endif

VERSION_XML := res/values/version.xml
GIT_LOG_HEAD := $(GIT_DIR)/.git/logs/HEAD

GRADLE ?= ./gradlew

.IGNORE : $(GIT_LOG_HEAD)

.PHONY: android-studio lintClean

android-studio: prebuild

res/values/version.xml: $(GIT_LOG_HEAD) AndroidManifest.xml
	$(BASE)/scripts/createVersionXML.sh -c .

LINT_BINARY := $(ANDROID_HOME)/tools/lint

lint-results.html: lint.xml $(wildcard src/**/*) $(wildcard res/**/*)
	$(GRADLE) lint
	cp --reflink=auto build/reports/$@ $@

lint.xml:
	ln -rs $(BASE)/build/lint.xml

lintClean:
	rm -f lint-results.html

# Symlink the gradle wrapper from all modules and transports to the
# one from MAXS' main component.
.PHONY: gradlew-symlinks
gradlew-symlinks: gradle gradlew gradlew.bat

gradle gradlew gradlew.bat:
	ln -rs $(MAIN)/$@
