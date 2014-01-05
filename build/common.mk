ifeq ($(CONTRIB), true)
GIT_DIR := $(BASE)/..
else
GIT_DIR := $(BASE)
endif

VERSION_XML := res/values/version.xml
GIT_LOG_MASTER := $(GIT_DIR)/.git/logs/refs/heads/master

.IGNORE : $(GIT_LOG_MASTER)

res/values/version.xml: $(GIT_LOG_MASTER) AndroidManifest.xml
	$(BASE)/scripts/createVersionXML.sh -c .
