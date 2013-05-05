ANT_BUILD_TARGET?=debug

.PHONY: all asmack clean eclipse

all: main

main:
	ant $(ANT_BUILD_TARGET)

asmack:
	./asmack.sh

eclipse: .settings .classpath .project

.settings:
	ln -s ../build/eclipse/settings .settings

.classpath:
	ln -s build/eclipse/classpath .classpath

.project:
	ln -s build/eclipse/project .project
