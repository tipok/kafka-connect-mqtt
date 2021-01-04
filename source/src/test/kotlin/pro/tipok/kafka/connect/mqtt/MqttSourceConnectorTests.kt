package pro.tipok.kafka.connect.mqtt

import org.apache.kafka.connect.errors.ConnectException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pro.tipok.kafka.connect.mqtt.utils.Configuration

/**
 * MqttSourceConnectorTests.
 *
 * @author  tipok
 * @version 1.0
 * @since   04.01.21
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MqttSourceConnectorTests {

    private val mqttSourceConnector: MqttSourceConnector = MqttSourceConnector()

    @Test
    fun `Testing start method`() {
        val props: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "ssl://localhost:8883",
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )
        mqttSourceConnector.start(props = props)

        val props2: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "ssl://localhost:8883",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )

        Assertions.assertThrows(ConnectException::class.java) {
            mqttSourceConnector.start(props = props2)
        }

        val props3: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )

        Assertions.assertThrows(ConnectException::class.java) {
            mqttSourceConnector.start(props = props3)
        }
    }

    @Test
    fun `Testing tasks configuration creation`() {
        val props: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to " ssl://localhost:8883 , ssl://127.0.0.1:8883, ssl://127.0.0.2:8883 ",
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )
        mqttSourceConnector.start(props = props)

        val configs = mqttSourceConnector.taskConfigs(2)
        Assertions.assertEquals(2, configs.size)

        val first = configs.first()
        val last = configs.last()

        Assertions.assertEquals("mqtt", first[Configuration.MQTT_KAFKA_TOPIC_CONFIG])
        Assertions.assertEquals("mqtt", last[Configuration.MQTT_KAFKA_TOPIC_CONFIG])
        Assertions.assertEquals("80", first[Configuration.MQTT_KEEPALIVE_CONFIG])
        Assertions.assertEquals("80", last[Configuration.MQTT_KEEPALIVE_CONFIG])

        Assertions.assertEquals(
            "ssl://localhost:8883,ssl://127.0.0.1:8883",
            first[Configuration.MQTT_HOSTS_CONFIG]
        )
        Assertions.assertEquals(
            "ssl://127.0.0.2:8883",
            last[Configuration.MQTT_HOSTS_CONFIG]
        )

        last[Configuration.MQTT_KEEPALIVE_CONFIG] = "90"
        Assertions.assertEquals("80", props[Configuration.MQTT_KEEPALIVE_CONFIG])
    }
}
