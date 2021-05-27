# Welcome to the New Relic Kafka Connect Sink Connector!

## Getting Up and Running.

### Assumptions: 
- Kafka is installed on user’s machine - A quick start can be found on the Kafka
site.

-  User is competent with Kafka. Although the basic installation, configuration,
and running of this is straight forward and be done quickly, there are more
advanced topics in maintaining a fault tolerant, enterprise Kafka configuration.
DevOps knowledge of Kafka and their enterprise Kafka stack easily translates
to the concepts of Connect, as Connect is a component of Kafka.

### Installing Kafka Connect for New Relic (Sink)

- Download Kafka Connect for New Relic: https://www.confluent.io/hub/newrelic/newrelic-kafka-connector

- During 2.0 development (short-term only):
    - you will need to download this repo,
    - use "mvn install" to create the latest 2.0 jar file (target/newrelic-kafka-connector-2.0.jar),
    - remove the existing jar file:
    - (example if using confluent): mv $CONFLUENT_HOME/share/confluent-hub-components/newrelic-newrelic-kafka-connector/lib/newrelic-kafka-connector-1.1.jar /tmp
    - copy target/newrelic-kafka-connector-2.0.jar into newrelic-newrelic-kafka-connector/lib
    - restart connect:
    - (example if using confluent): confluent local services connect stop && confluent local services connect start)

### 2.0 changes from 1.1
Next we will discuss the changes in 2.0 regarding serialization and transforms, and look at some sample data and sample configurations you can use to get started.

#### Serialization/Deserialization:

The 2.0 connector improves on 1.1 in that you are free to use any of the standard converters for deserializing your messages. For example, you can use any of these converters
- io.confluent.connect.avro.AvroConverter
- io.confluent.connect.protobuf.ProtobufConverter
- io.confluent.connect.json.JsonSchemaConverter
- org.apache.kafka.connect.json.JsonConverter

It just depends on how you are serializing your messages into your topics. The only requirement for 2.0 is that the converter either deliver a Struct, or a Map, into the connector.

Of the above choices, the only one that would deliver a Map would be:
- org.apache.kafka.connect.json.JsonConverter, 
- with value.converter.schemas.enable=false, 
- and no schema included in the JSON message.

All the other choices above, including JsonConverter with value.converter.schemas.enable=true, will deliver a Struct.


#### Sending some test data from the command line:

Here is an example of settting up a Connector config, assuming you are:
- sending JSON messages,
- serializing them using org.apache.kafka.connect.json.JsonConverter,
- and including a schema with your JSON:

```
{
  "name": "my-json-with-schema-connector",
  "config": {
    "connector.class": "com.newrelic.telemetry.logs.TelemetryLogsSinkConnector",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "true",
    "value.converter.schema.registry.url": "http://localhost:8081"
    "topics":"my-json-with-schema-topic",
    "api.key": "${NR_INSERT_KEY}",
  }
}
```

And then to confirm it's created:
```
curl --silent -X GET http://localhost:8083/connectors/my-json-with-schema-connector | jq
{
  "name": "my-json-with-schema-connector",
  "config": {
    "connector.class": "com.newrelic.telemetry.logs.TelemetryLogsSinkConnector",
    "value.converter.schema.registry.url": "http://localhost:8081",
    "api.key": "[YOUR_NR_INSERT_KEY]",
    "topics": "my-json-with-schema-topic",
    "value.converter.schemas.enable": "true",
    "name": "my-json-with-schema-connector",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  },
  "tasks": [
    {
      "connector": "my-json-with-schema-connector",
      "task": 0
    }
  ],
  "type": "sink"
}
```

At this point, you could test sending Log data to New Relic, with the following (example assumes you are on Confluent):
```
confluent local services kafka produce my-json-with-schema-topic
```

And enter:
```
{"schema":{"type":"struct","fields":[{"type":"string","optional":false,"field":"message"},{"type":"string","optional":true,"field":"partitionName"},{"type":"string","optional":true,"field":"logLevel"},{"type":"string","optional":true,"field":"aStringAttribute"},{"type":"int32","optional":true,"field":"anIntAttribute"}],"optional":false,"name":"foobar"},"payload":{"message":"A Purchase Did Once Happen","partitionName":"partitionA","logLevel":"DEBUG","aStringAttribute":"this came from json+schema","anIntAttribute":123}}
```

If you tail the Connect logs, you should shortly see a message that the Log has been sent to New Relic, and you should be able to go into the Logs UI and see your resulting message.


### Notes on data format and transforms:

Regardless of how you manage your message schema, the requirements for the data are built around the design of the New Relic Telemetry SDK (which is included as a dependency in this connector.) Using Log data as an example, this connector only requires that the "message" field be included. The above example includes some other optional fields you could include in your schema; in the case of Logs, these would become additional fields in your log record when it is created in New Relic.

Please refer to the Logs API documentation for additional information about how log data can/must be structured.
- https://docs.newrelic.com/docs/logs/log-management/log-api/introduction-log-api/#json-logs
- https://docs.newrelic.com/docs/logs/log-management/log-api/introduction-log-api/#message-attribute-parsin

If your data is not structured as required by this connector, you are free to use Transforms as part of the connector setup to modify the data into a format which works.

### TODO: 
- provide an exmaple which uses a Hoist transform to provide the "message" field.
- provide a JSON example with no schema provided
- provide an example using Avro
- provide an example using Protobuf


## MORE TODOS:

- examples of Event data
- examples of Metric data
- quick walkthrough of code
- bring in remaning relevant sections from below:



## ORIGINAL TEXT:

- Download any SMT’s relevant for your environment.
    - In this case we are using test data based on a current customer’s use
case.
    - Custom SMTs (Single Message Transformations) can be developed by
end user or through a NR services engagement. Based on experience
with out home grown solution, this is a highly beneficial capability.
    - New Relic can continue to develop and make available Transforms for
common message formats such as Prometheus
- Create a folder to put your downloaded files into
    ````
      $ mkdir /opt/kafka/plugins
      $ cp ~/Downloads/*.jar /opt/kafka/plugins
    ````
- Configure your plugins directory in Kafka by updating the `connect-distributed.properties` file
    - Update plugin.path to include your plugins directory created above
- Stop and restart Kafka / Connect if it is already running
- To check if your connector is available head over to the connect rest endpoint, by default it will be http://localhost:8083/connector-plugins/. Make sure our 3 connectors are listed `com.newrelic.telemetry.events.TelemetryEventsSinkConnector`,`com.newrelic.telemetry.metrics.TelemetryMetricsSinkConnector`, and `com.newrelic.telemetry.logs.TelemetryLogsSinkConnector`.

### Create a Telemetry Events Connector job

- Use Curl or Postman to post the following json on the Connect rest URL http://localhost:8083/connectors.
  ```
  {
   "name": "events-connector",
   "config": {
   "connector.class": "com.newrelic.telemetry.events.TelemetryEventsSinkConnector",
   "value.converter": "com.newrelic.telemetry.events.EventsConverter",
   "topics": "nrevents",
   "api.key": "<NEW_RELIC_API_KEY>"
   }
  }
  ```
  
  
- This will create the connector job. To check the list of running Connector jobs head over to http://localhost:8083/connectors
- Make sure you see your connector, in this case `events-connector` listed
- To check the status (RUNNING OR PAUSED OR FAILED) use this URL http://localhost:8083/connectors/events-connector/status

### Create a Telemetry Metrics Connector job
- Metrics have the same configuration as events.
- Use Curl or Postman to post the following json on the Connect rest URL http://localhost:8083/connectors.
  ```
  {
   "name": "metrics-connector",
   "config": {
   "connector.class": "com.newrelic.telemetry.events.TelemetryMetricsSinkConnector",
   "value.converter":"com.newrelic.telemetry.metrics.MetricsConverter",
   "value.converter.schemas.enable": false,
   "topics": "nrmetrics",
   "api.key": "<NEW_RELIC_API_KEY>"
   }
  }
    ```

### Create a Telemetry Logs Connector job
- Logs have the same configuration as events.
- Use Curl or Postman to post the following json on the Connect rest URL http://localhost:8083/connectors.
  ```
  {
   "name": "logs-connector",
   "config": {
   "connector.class": "com.newrelic.telemetry.logs.TelemetryLogsSinkConnector",
   "value.converter":"com.newrelic.telemetry.logs.LogsConverter",
   "value.converter.schemas.enable": false,
   "topics": "nrlogs",
   "api.key": "<NEW_RELIC_API_KEY>"
   }
  }
    ```

#### Timestamps in Logs
-  If `timestamp` field is found in the record, it will be used. 
-  If `timestamp` is not found and `use.record.timestamp` is set to `true` then the timestamp retrieved from the Kafka record will be used.
-  If `timestamp` is not found and `use.record.timestamp` is set to `false` then the ingestion timestamp will be used..


### Full list of variables you can send to connector 
  | attribute     | Required |                          description          |
  | ------------- | -------- | --------------------------------------------- |
  | name          | yes | user definable name for identifying connector |
  |connector.class| yes | com.newrelic.telemetry.events.TelemetryEventsSinkConnector(Events), com.newrelic.telemetry.events.TelemetryMetricsSinkConnector(Metrics), or com.newrelic.telemetry.logs.TelemetryLogsSinkConnector(Logs)|
  |value.converter| yes | com.newrelic.telemetry.events.EventsConverter(Events), com.newrelic.telemetry.metrics.MetricsConverter(Metrics), or com.newrelic.telemetry.logs.LogsConverter(Logs) |
  |topics         | yes | Coma seperated list of topics the connector listens to.|
  |api.key        | yes | NR api key |
  |nr.max.retries | no  | set max number of retries on the NR server, default is 5 |
  |nr.timeout     | no  | set number of seconds to wait before throwing a timeout exception, default is 2 |  
  |nr.retry.interval.ms | no | set interval between retries in milli seconds, default is 1000 |
  |errors.tolerance | no | all(ignores all json errors) or none(makes connector fail on messages with incorrect format) |
  |errors.deadletterqueue.topic.name| no | dlq topic name ( messages with incorrect format are sent to this topic) |
  |errors.deadletterqueue.topic.replication.factor| no | dlq topic replication factor |
  |use.record.timestamp        | no | When set to `true`, the timestamp is retrieved from the Kafka record and passed to New Relic. When set to false, the timestamp will be the ingestion timestamp. default is true |  

### Simple Message Transforms 
- Sometimes customers want to use their own message format which is different from the standard `events` or `metrics` format.
- In that case we develop [Simple Message Transforms](https://docs.confluent.io/current/connect/transforms/index.html#:~:text=Kafka%20Connect%20Transformations-,Kafka%20Connect%20Transformations,sent%20to%20a%20sink%20connector.)  
- Currently we have developed two SMTs [Agent Rollup](https://github.com/newrelic/kafka-connect-newrelic/tree/master/smts/Kafka-connect-new-relic-agent-rollup-smt) and [Statsd](https://github.com/newrelic/kafka-connect-newrelic/tree/master/smts/kafka-connect-new-relic-statsd-smt) 
