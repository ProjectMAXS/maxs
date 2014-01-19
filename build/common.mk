ifeq ($(CONTRIB), true)
GIT_DIR := $(BASE)/..
else
GIT_DIR := $(BASE)
endif

VERSION_XML := res/values/version.xml
GIT_LOG_HEAD := $(GIT_DIR)/.git/logs/HEAD

.IGNORE : $(GIT_LOG_HEAD)

res/values/version.xml: $(GIT_LOG_HEAD) AndroidManifest.xml
	$(BASE)/scripts/createVersionXML.sh -c .
