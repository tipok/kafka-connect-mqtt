# kafka-connect-mqtt


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
