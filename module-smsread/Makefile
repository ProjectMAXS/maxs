SHARED_SOURCE=src/org/projectmaxs/shared

.PHONY: all clean eclipse

all: module

module: $(SHARED_SOURCE)
	echo "compiling module"

$(SHARED_SOURCE):
	ln -s ../../../../main/$(SHARED_SOURCE) $(SHARED_SOURCE)

eclipse: .settings .classpath .project

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
