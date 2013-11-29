VERSION_XML := res/values/version.xml
GIT_LOG_MASTER := $(BASE)/.git/logs/refs/heads/master

.IGNORE : $(GIT_LOG_MASTER)

res/values/version.xml: $(GIT_LOG_MASTER) AndroidManifest.xml
	$(BASE)/scripts/createVersionXML.sh -c .
