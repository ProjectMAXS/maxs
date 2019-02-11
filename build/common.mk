ifeq ($(CONTRIB), true)
GIT_DIR := $(BASE)/..
else
GIT_DIR := $(BASE)
endif

VERSION_XML := res/values/version.xml
GIT_LOG_HEAD := $(GIT_DIR)/.git/logs/HEAD

.IGNORE : $(GIT_LOG_HEAD)

.PHONY: artifacts android-studio lintClean lintFull

android-studio: prebuild

ifeq ($(MAXS_BUILD_SYSTEM),ant)
prebuild: artifacts
endif

res/values/version.xml: $(GIT_LOG_HEAD) AndroidManifest.xml
	$(BASE)/scripts/createVersionXML.sh -c .

LINT_BINARY := $(ANDROID_HOME)/tools/lint

lint-report.html: lint.xml $(wildcard src/**/*) $(wildcard res/**/*)
	 $(LINT_BINARY) --nowarn --exitcode --quiet --html lint-report.html --disable LintError $(CURDIR)

lintFull: lint.xml
	$(LINT_BINARY) --exitcode --html lint-report-full.html --disable LintError $(CURDIR)

lint.xml:
	ln -rs $(BASE)/build/lint.xml

artifacts:
	$(BASE)/scripts/MavenToAndroidAnt/getMavenArtifactsNG.py -f $(BASE)/build/global_artifacts.csv -p .

lintClean:
	rm -f lint-report.html
