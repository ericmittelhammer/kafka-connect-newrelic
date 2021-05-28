# Integration Tests

## Prerequisites

- OS: Linux
- Kafka Platform: Confluent Kafka
    - (TODO: require only Kafka)
- The following executables must be availble on the path
    - kafka-topics
    - confluent
    - curl
- New Relic Account, New Relic Insights Insert Key
- This project cloned to host
<br/><br/>

## Supported Tests
- Build Topic and Connector
- Push Logs (no check if arrived at NR)
- Teardown Connector
<br/><br/>

## Planned Tests
- Push Logs + check arrived at NR
- Push Events + check arrived at NR
- Push Metrics + check arrived at NR
- Teardown Topic
<br/><br/>


## Steps:

### First, set up the follwing exports:

export NR_INSERT_KEY=[a valid NR insights insert key]

export VALUE_CONVERTER=[a Kafka Connect converter]

export TOPIC=[a topic name]

export VALUE_CONVERTER_SCHEMAS_ENABLE=[true or false]
<br/><br/>

### examples:

export NR_INSERT_KEY=NRII-1234567890...

export VALUE_CONVERTER=org.apache.kafka.connect.json.JsonConverter

export TOPIC=my-topic-name

export VALUE_CONVERTER_SCHEMAS_ENABLE=true
<br/><br/>
[Note that the above examples assume that your messages are being sent in JSON format, with accompanying schema.]

[###TODO: offer more options.]
<br/><br/>

### Next, follow these steps:
<br/><br/>
cd [this project]/integration-tests
<br/><br/>

### 1) to build a topic and connector with same name:
./build-topic-and-connector.sh -i ${NR_INSERT_KEY} -v ${VALUE_CONVERTER} -t ${TOPIC} -s ${VALUE_CONVERTER_SCHEMAS_ENABLE}
<br/><br/>
### 2) to push Logs:

This step is currently manual, will be automated soon. 

run: 

confluent local services kafka produce ${TOPIC}

pass the following test data:

{"schema":{"type":"struct","fields":[{"type":"string","optional":false,"field":"message"},{"type":"string","optional":true,"field":"partitionName"},{"type":"string","optional":true,"field":"logLevel"},{"type":"string","optional":true,"field":"aStringAttribute"},{"type":"int32","optional":true,"field":"anIntAttribute"}],"optional":false,"name":"foobar"},"payload":{"message":"This is a demo log message","partitionName":"partitionA","logLevel":"DEBUG","aStringAttribute":"this time it was with a schema","anIntAttribute":124}}

<br/><br/>

### 3) to tear down the connector built by 1) above
./teardown-connector.sh -t ${TOPIC}