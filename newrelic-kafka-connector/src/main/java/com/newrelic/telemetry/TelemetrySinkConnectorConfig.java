package com.newrelic.telemetry;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Importance;

import java.util.Map;


public class TelemetrySinkConnectorConfig extends AbstractConfig {

    public static final String API_KEY = "api.key";
    private static final String API_KEY_DOC = "API Key for New Relic.";
    public static final String MAX_RETRIES = "nr.max.retries";
    private static final String RETRIES_DOC = "Number of retries when New Relic servers are down.";

    public static final String RETRY_INTERVAL_MS = "nr.retry.interval.ms";
    private static final String RETRY_INTERVAL_MS_DOC = "Interval between retries in milliseconds.";

    public static final String USE_RECORD_TIMESTAMP = "use.record.timestamp";    
    private static final String USE_RECORD_TIMESTAMP_DOC = "When set to `true`, The timestamp "
    + "is retrieved from the Kafka record and passed to New Relic. When set to false, the timestamp will be the ingestion timestamp. By "
    + "default, this is set to true.";

    public static final String TIMEOUT_SECONDS = "nr.timeout";
    private static final String TIMEOUT_SECONDS_DOC = "Timeout for API calls in seconds. By default, this is set to 2 seconds";

    public static final String VALUE_CONVERTER_SCHEMA_REGISTRY_URL = "value.converter.schema.registry.url";
    private static final String VALUE_CONVERTER_SCHEMA_REGISTRY_URL_DOC = "Location of the schema registry";

    public static final String VALUE_CONVERTER_SCHEMAS_ENABLE = "value.converter.schemas.enable";
    private static final String VALUE_CONVERTER_SCHEMAS_ENABLE_DOC = "set to false if inbound JSON is not carrying a schema";

    public final boolean useRecordTimestamp;

    public TelemetrySinkConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
        super(config, parsedConfig);
        useRecordTimestamp = getBoolean(USE_RECORD_TIMESTAMP);
    }

    public TelemetrySinkConnectorConfig(Map<String, String> parsedConfig) {
        this(conf(), parsedConfig);
    }

    public static ConfigDef conf() {
        ConfigDef configDef = new ConfigDef()
                .define(API_KEY, Type.PASSWORD, Importance.HIGH, API_KEY_DOC)
                .define(USE_RECORD_TIMESTAMP, ConfigDef.Type.BOOLEAN, true, ConfigDef.Importance.MEDIUM, USE_RECORD_TIMESTAMP_DOC)
                .define(TIMEOUT_SECONDS, Type.INT, 2, Importance.LOW, TIMEOUT_SECONDS_DOC)
                .define(MAX_RETRIES, Type.INT, 5, Importance.LOW, RETRIES_DOC)
                .define(RETRY_INTERVAL_MS, Type.LONG, 1000, Importance.LOW, RETRY_INTERVAL_MS_DOC)
                .define(VALUE_CONVERTER_SCHEMA_REGISTRY_URL, Type.STRING, "http://localhost:8081", Importance.LOW, VALUE_CONVERTER_SCHEMA_REGISTRY_URL_DOC)
                .define(VALUE_CONVERTER_SCHEMAS_ENABLE, Type.BOOLEAN, true, Importance.LOW, VALUE_CONVERTER_SCHEMAS_ENABLE_DOC);
        return configDef;
    }

}
