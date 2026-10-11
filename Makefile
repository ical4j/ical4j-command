SHELL:=/bin/bash
# Optional local settings (GRADLE_VERSION, GRAALVM_HOME, SDKROOT, ...).
-include .env
export GRAALVM_HOME SDKROOT

NEXT_VERSION=$(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
CHANGE_JUSTIFICATION=$(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))

.PHONY: all gradlew clean check test build installDist dist native smoke changelog currentVersion \
	markNextVersion install verify release publish

all: check

gradlew:
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION) --distribution-type=bin

clean:
	./gradlew clean

check:
	./gradlew check

test:
	./gradlew test

build:
	./gradlew build

installDist:
	./gradlew installDist

dist:
	./gradlew distZip

# Native executable at build/native/nativeCompile/ict. Needs GRAALVM_HOME (GraalVM CE for JDK 25+).
native:
	$(if $(GRAALVM_HOME),JAVA_HOME=$(GRAALVM_HOME)) ./gradlew nativeCompile --no-configuration-cache

# Smoke suite and startup time against the native executable built by `make native`.
smoke: native
	./gradlew nativeSmoke nativeStartup -PictBinary=build/native/nativeCompile/ict

changelog:
	git log "$(CHANGELOG_START_TAG)...$(CHANGELOG_END_TAG)" \
    	--pretty=format:'* %s [View commit](http://github.com/ical4j/ical4j-command/commit/%H)' --reverse | grep -v Merge

currentVersion:
	./gradlew -q currentVersion

markNextVersion:
	./gradlew markNextVersion -Prelease.version=$(NEXT_VERSION)

install:
	./gradlew publishToMavenLocal

verify:
	./gradlew check jvmSmoke

# Tags ict-<version> and pushes it; release.yml then builds and smoke-tests the native
# executables on each platform, attaches them to a GitHub Release and publishes the jar.
release: verify
	./gradlew release

publish:
	./gradlew publish
