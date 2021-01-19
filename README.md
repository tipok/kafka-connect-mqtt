# kafka-connect-mqtt

**Source Only**
This source is not creating new topics for MQTT topics, but sends all the data into a single topic.
Data is sent as a schema:

```kotlin
SchemaBuilder.struct()
    .name(MqttMessage::class.java.simpleName)
    .field(TOPIC, Schema.STRING_SCHEMA)
    .field(DATE_TIME, Timestamp.SCHEMA)
    .field(ID_PROPERTY, Schema.INT32_SCHEMA)
    .field(RETAINED, Schema.BOOLEAN_SCHEMA)
    .field(DUPLICATE, Schema.BOOLEAN_SCHEMA)
    .field(QOS, Schema.INT32_SCHEMA)
    .field(PAYLOAD, Schema.STRING_SCHEMA)
    .build()
```

Payload is read is String and Base64 encoded.

## Configure Connector

```json
{
  "name": "mqtt-test",
  "connector.class": "pro.tipok.kafka.connect.mqtt.MqttSourceConnector",
  "mqtt.server.uri": [
    "tcp://mqtt-uri:1883"
  ],
  "mqtt.password": "********************************",
  "mqtt.username": "tipok",
  "mqtt.clean.session.enabled": true,
  "kafka.topic": "mqtt-topic",
  "mqtt.topics": [
    "temp"
  ]
}
```
