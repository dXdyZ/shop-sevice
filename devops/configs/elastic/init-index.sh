#!/bin/sh
set -eu

# Конфиг из ENV
ES_SCHEME=${ES_SCHEME:-http}
ES_HOST=${ES_HOST:-elasticsearch}
ES_PORT=${ES_PORT:-9200}
if [ -n "${ES_URL:-}" ]; then
  ES_BASE="$ES_URL"
else
  ES_BASE="$ES_SCHEME://$ES_HOST:$ES_PORT"
fi

INDEX_NAME=${INDEX_NAME:-product_v1}
MAPPINGS_FILE=${MAPPINGS_FILE:-/mappings/product_V1-mapping.json}
WAIT_TIMEOUT_SEC=${WAIT_TIMEOUT_SEC:-120}
VERBOSE=${VERBOSE:-false}

echo "Using Elasticsearch URL: $ES_BASE"
echo "Target index: $INDEX_NAME"
echo "Mappings file: $MAPPINGS_FILE"

if [ ! -f "$MAPPINGS_FILE" ]; then
  echo "ERROR: Mapping file not found: $MAPPINGS_FILE"
  echo "Listing directory: $(dirname "$MAPPINGS_FILE")"
  ls -la "$(dirname "$MAPPINGS_FILE")" || true
  exit 1
fi

# Опции curl
CURL_COMMON="-sS"
[ "$VERBOSE" = "true" ] && CURL_COMMON="-v"
[ "${ES_SKIP_TLS_VERIFY:-false}" = "true" ] && CURL_COMMON="$CURL_COMMON -k"
[ -n "${ES_CA_CERT:-}" ] && CURL_COMMON="$CURL_COMMON --cacert $ES_CA_CERT"

# Basic auth (опционально)
AUTH_OPT=""
if [ -n "${ES_USERNAME:-}" ] && [ -n "${ES_PASSWORD:-}" ]; then
  AUTH_OPT="-u ${ES_USERNAME}:${ES_PASSWORD}"
fi

# Ждем готовности (yellow/green)
echo "Waiting for Elasticsearch to become ready..."
end=$(( $(date +%s) + WAIT_TIMEOUT_SEC ))
while :; do
  code=$(sh -c "curl $CURL_COMMON $AUTH_OPT -o /dev/null -w '%{http_code}' \"$ES_BASE/_cluster/health?wait_for_status=yellow&timeout=2s\"" || true)
  if [ "$code" = "200" ]; then
    echo "Elasticsearch is ready."
    break
  fi
  [ "$(date +%s)" -ge "$end" ] && echo "ERROR: Timed out waiting for Elasticsearch." && exit 1
  sleep 2
done

# Проверяем существование индекса
exists_code=$(sh -c "curl $CURL_COMMON $AUTH_OPT -o /dev/null -w '%{http_code}' \"$ES_BASE/$INDEX_NAME\"" || true)
if [ "$exists_code" = "200" ]; then
  echo "Index '$INDEX_NAME' already exists. Nothing to do."
  exit 0
fi

# Создаем индекс
echo "Creating index '$INDEX_NAME'..."
resp_file=$(mktemp)
code=$(sh -c "curl $CURL_COMMON $AUTH_OPT -H 'Content-Type: application/json' -X PUT \"$ES_BASE/$INDEX_NAME\" --data-binary \"@$MAPPINGS_FILE\" -w '%{http_code}' -o \"$resp_file\"" || true)

echo "HTTP: $code"
echo "Body:"
cat "$resp_file" || true
echo

if [ "$code" = "200" ] || [ "$code" = "201" ]; then
  echo "Index '$INDEX_NAME' created."
elif [ "$code" = "400" ] && grep -q 'resource_already_exists_exception' "$resp_file"; then
  echo "Index '$INDEX_NAME' already exists (race)."
  rm -f "$resp_file"
  exit 0
else
  echo "ERROR: Failed to create index, HTTP $code."
  rm -f "$resp_file"
  exit 1
fi

rm -f "$resp_file"