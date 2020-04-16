#!/bin/bash

## Usage: ./bin/idam-authenticate.sh IDAM_URI IDAM_USERNAME IDAM_PASSWORD
##
##
## Make call to IDAM to get auth token

IDAM_URI=$1
IDAM_USERNAME=$2
IDAM_PASSWORD=$3

curl --silent -H 'Content-Type: application/x-www-form-urlencoded' -XPOST "${IDAM_URI}/loginUser?username=${IDAM_USERNAME}&password=${IDAM_PASSWORD}" | jq -r .api_auth_token
