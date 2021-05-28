while getopts i:v:t:s: flag
do
    case "${flag}" in
        i) insightsKey=${OPTARG};;
        v) valueConverter=${OPTARG};;
        t) topic=${OPTARG};;
        s) schemasEnabled=${OPTARG};;
    esac
done
echo "Insights Key: ****************************** ";
echo "Value Converter: $valueConverter";
echo "Topic: $topic";
echo "Schemas Enabled: $schemasEnabled";

# create the topic
kafka-topics --bootstrap-server localhost:9092 --create --topic $topic --partitions 1 --replication-factor 1

generate_post_data()
{
  cat <<EOF
{
  "name": "${topic}",
  "config": {
    "connector.class": "com.newrelic.telemetry.logs.TelemetryLogsSinkConnector",
    "tasks.max": "1",
    "value.converter": "${valueConverter}",
    "value.converter.schemas.enable": "${schemasEnabled}",
    "errors.tolerance": "all",
    "errors.log.enable": true,
    "errors.log.include.messages": true,
    "topics":"${topic}",
    "api.key": "${insightsKey}",
    "value.converter.schema.registry.url": "http://localhost:8081"
  }
}
EOF
}

curl \
--header "Accept: application/json" \
--header "Content-Type: application/json" \
--request POST \
-X POST --data "$(generate_post_data)" \
http://localhost:8083/connectors

curl --silent -X GET http://localhost:8083/connectors | jq

curl --silent -X GET http://localhost:8083/connectors/${topic} | jq

curl --silent -X GET http://localhost:8083/connectors/${topic}/status | jq
