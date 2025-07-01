# Makefile for SmartFlow

# Define colors
GREEN=\033[0;32m
YELLOW=\033[0;33m
RED=\033[0;31m
NC=\033[0m # No Color

# Maven command
MVN = ./mvnw

# Default target
all: help

# Banner
define BANNER
@echo "${GREEN}"
@echo "      |\\__/,|   (\`("
@echo "    _.|o o  |_   ) ) "
@echo "---(((---(((-------- "
@echo "      Cat!           "
@echo "                     "
@echo "                     "
@echo "${NC}"
endef

# Targets
help:
	$(call BANNER)
	@echo "Usage: make [target]"
	@echo ""
	@echo "Available targets:"
	@echo "  ${YELLOW}build${NC}    Builds the project"
	@echo "  ${YELLOW}run${NC}      Runs the application"
	@echo "  ${YELLOW}clean${NC}    Cleans the project"
	@echo "  ${YELLOW}help${NC}     Shows this help message"

build:
	$(call BANNER)
	@echo "${YELLOW}Building the project...${NC}"
	@$(MVN) clean install

run:
	@echo "${YELLOW}Running the application...${NC}"
	@$(MVN) javafx:run

clean:
	@echo "${YELLOW}Cleaning the project...${NC}"
	@$(MVN) clean


.PHONY: all build run clean help
