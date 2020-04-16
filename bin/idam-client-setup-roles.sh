#!/bin/bash

## Usage: ./idam-client-setup-roles.sh IDAM_URI role token request_body
##
## Options:
##    - IDAM_URI
##    - token: Auth token
##    - role: Name of role to added
##
## Add role to IDAM

IDAM_URI=$1
token=$2
role=$3

(./bin/idam-client-setup.sh ${IDAM_URI} roles ${token} '{"description": "'${role}'", "id": "'${role}'", "name": "'${role}'"}')
