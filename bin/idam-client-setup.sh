#!/bin/bash

## Usage: ./idam-client-setup.sh IDAM_URI task token request_body
##
## Options:
##    - IDAM_URI
##    - task: Option of which task to set (services or roles)
##    - token: Auth token
##    - request_body: Sets parameters
##
## Add service or role to IDAM

IDAM_URI=$1
task=$2
token=$3
request_body=$4

curl -XPOST \
  ${IDAM_URI}/${task} \
 -H "Authorization: AdminApiAuthToken ${token}" \
 -H "Content-Type: application/json" \
 -d "${request_body}"

