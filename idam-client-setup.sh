#!/bin/bash

IDAM_URI="http://localhost:5000"
IDAM_USERNAME="idamOwner@hmcts.net"
IDAM_PASSWORD="Ref0rmIsFun"

authToken=$(curl -v -H 'Content-Type: application/x-www-form-urlencoded' -XPOST "${IDAM_URI}/loginUser?username=${IDAM_USERNAME}&password=${IDAM_PASSWORD}" | jq -r .api_auth_token)

echo "authtoken is ${authToken}"

#Create a ccd gateway client
curl -XPOST \
  ${IDAM_URI}/services \
 -H "Authorization: AdminApiAuthToken ${authToken}" \
 -H "Content-Type: application/json" \
 -d '{"description": "em", "label": "em", "oauth2ClientId": "webshow", "oauth2ClientSecret": "AAAAAAAAAAAAAAAA", "oauth2RedirectUris": ["http://localhost:8080/oauth2redirect"], "selfRegistrationAllowed": true}'





