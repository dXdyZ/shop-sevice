#!/usr/bin/env bash
set -euo pipefail

KC_URL=${KC_URL:-http://localhost:8080}
REALM=${REALM:-shop}
CLIENT_ID=${CLIENT_ID:-dev-cli}
USERNAME=${USERNAME:-another}
PASSWORD=${PASSWORD:-password}
SCOPE=${SCOPE:-openid}

RESP=$(curl -s -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}" \
  -d "scope=${SCOPE}" \
  "${KC_URL}/realms/${REALM}/protocol/openid-connect/token")

echo "$RESP" | jq .
