# -*- Mode: Makefile -*-
#
# Makefile - Java version
#
# use: make 
# or:  make test
#

JAVA = /usr/bin/java
JAVAC = /usr/bin/javac

all: RouterSimulator.class

RouterSimulator.class: RouterSimulator.java
	-@$(JAVAC) RouterSimulator.java


clean:
	-@touch ./abc~ core
	-@rm -f *~ core 

clobber: clean
	-@touch ./abc.class 
	-@rm -f *.class 

test: RouterSimulator.class
	$(JAVA) -DTrace=3 RouterSimulator

install4:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@echo 
	-@echo "Continue with ^D, exit with ^C"
	-@cat > /dev/null
	-@make clobber
	-@cp test/RouterSimulator4.java RouterSimulator.java

install5:
	-@echo 
	-@echo "  Warning: this command will overwrite file ./RouterSimulator.java"
	-@echo 
	-@echo "Continue with ^D, exit with ^C"
	-@cat > /dev/null
	-@make clobber
	-@cp test/RouterSimulator5.java RouterSimulator.java

