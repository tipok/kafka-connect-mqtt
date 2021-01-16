# kafka-connect-mqtt

**Source Only**
This source is not creating new topics for MQTT topics, but sents all the data into a single topic.
Data is wrapped into a JSON envelope:

```kotlin
data class MqttMessage(
    val topic: String,
    val id: Int,
    val retained: Boolean,
    val duplicate: Boolean,
    val qos: Int,
    val payload: String
)
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
