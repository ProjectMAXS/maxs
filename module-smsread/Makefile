SHARED_SOURCE=src/org/projectmaxs/shared
ANT_BUILD_TARGET?=debug

.PHONY: all clean eclipse

all: module

module: $(SHARED_SOURCE)
	ant $(ANT_BUILD_TARGET)

$(SHARED_SOURCE):
	ln -s ../../../../main/$(SHARED_SOURCE) $(SHARED_SOURCE)

eclipse: .settings .classpath .project $(SHARED_SOURCE)

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
