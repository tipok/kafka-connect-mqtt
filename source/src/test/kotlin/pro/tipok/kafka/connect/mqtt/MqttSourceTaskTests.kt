package pro.tipok.kafka.connect.mqtt

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceRecord
import org.eclipse.paho.client.mqttv3.MqttClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pro.tipok.kafka.connect.mqtt.utils.Configuration
import pro.tipok.kafka.connect.mqtt.utils.MessageListener
import pro.tipok.kafka.connect.mqtt.utils.Version

/**
 * MqttSourceTaskTests.
 *
 * @author  tipok
 * @version 1.0
 * @since   04.01.21
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MqttSourceTaskTests {

    @Test
    fun `Testing start method errors`() {
        val mqttClient: MqttClient = mockk(relaxUnitFun = true)
        val mqttSourceTask = MqttSourceTask { _, _ -> mqttClient }

        val props2: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "ssl://localhost:8883",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )

        Assertions.assertThrows(ConnectException::class.java) {
            mqttSourceTask.start(props = props2)
        }

        val props3: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )

        Assertions.assertThrows(ConnectException::class.java) {
            mqttSourceTask.start(props = props3)
        }

        val props4: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "",
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )

        Assertions.assertThrows(ConnectException::class.java) {
            mqttSourceTask.start(props = props4)
        }
    }

    @Test
    fun `Testing with single host and default MQTT topic`() {
        val mqttClient: MqttClient = mockk(relaxUnitFun = true)
        val mqttSourceTask = MqttSourceTask { _, _ -> mqttClient }

        val props: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "ssl://127.0.0.99:8883",
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )

        mqttSourceTask.start(props)


        verify { mqttClient.connect(any()) }
        verify { mqttClient.setCallback(any<MessageListener>()) }
        verify { mqttClient.subscribe("#", 0) }

        confirmVerified(mqttClient)
    }

    @Test
    fun `Testing with single host and multiple MQTT topics`() {
        val mqttClient: MqttClient = mockk(relaxUnitFun = true)
        val mqttSourceTask = MqttSourceTask { _, _ -> mqttClient }

        val props: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "ssl://127.0.0.99:8883",
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
            Configuration.MQTT_MQTT_TOPICS_CONFIG to "temp, hum",
        )

        mqttSourceTask.start(props)


        verify { mqttClient.connect(any()) }
        verify { mqttClient.setCallback(any<MessageListener>()) }
        verify { mqttClient.subscribe("temp", 0) }
        verify { mqttClient.subscribe("hum", 0) }

        confirmVerified(mqttClient)
    }

    @Test
    fun `Testing with single multiple hosts and default MQTT topic`() {
        val mqttClient: MqttClient = mockk(relaxUnitFun = true)
        val mqttSourceTask = MqttSourceTask { _, _ -> mqttClient }

        val props: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "ssl://127.0.0.99:8883,ssl://127.0.0.88:8883",
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
        )

        mqttSourceTask.start(props)


        verify(exactly = 2) { mqttClient.connect(any()) }
        verify { mqttClient.setCallback(any<MessageListener>()) }
        verify { mqttClient.subscribe("#", 0) }

        confirmVerified(mqttClient)
    }

    @Test
    fun `Testing poll method`() {
        val mqttClient: MqttClient = mockk(relaxUnitFun = true)
        val mqttSourceTask = MqttSourceTask { _, _ -> mqttClient }

        val record = SourceRecord(
            null,
            null,
            "mqtt",
            null,
            Schema.STRING_SCHEMA,
            "abc"
        )
        mqttSourceTask.queue.put(record)

        val polled = mqttSourceTask.poll()

        Assertions.assertEquals(1, polled.size)
        Assertions.assertEquals(record, polled[0])

        mqttSourceTask.queue.put(record)
        mqttSourceTask.queue.put(record)
        mqttSourceTask.queue.put(record)

        val polled2 = mqttSourceTask.poll()

        Assertions.assertEquals(3, polled2.size)
        Assertions.assertEquals(record, polled2[0])
        Assertions.assertEquals(record, polled2[1])
        Assertions.assertEquals(record, polled2[2])
    }

    @Test
    fun `Testing stop method`() {
        val mqttClient: MqttClient = mockk(relaxUnitFun = true)
        val mqttSourceTask = MqttSourceTask { _, _ -> mqttClient }

        val props: MutableMap<String, String> = mutableMapOf(
            Configuration.MQTT_HOSTS_CONFIG to "ssl://127.0.0.99:8883,ssl://127.0.0.88:8883",
            Configuration.MQTT_KAFKA_TOPIC_CONFIG to "mqtt",
            Configuration.MQTT_KEEPALIVE_CONFIG to "80",
            Configuration.MQTT_TIMEOUT_CONFIG to "60",
            Configuration.MQTT_MQTT_TOPICS_CONFIG to "temp, hum",
        )

        mqttSourceTask.start(props)


        verify(exactly = 2) { mqttClient.connect(any()) }
        verify(exactly = 2) { mqttClient.setCallback(any<MessageListener>()) }
        verify { mqttClient.subscribe("temp", 0) }
        verify { mqttClient.subscribe("hum", 0) }


        val record = SourceRecord(
            null,
            null,
            "mqtt",
            null,
            Schema.STRING_SCHEMA,
            "abc"
        )
        mqttSourceTask.queue.put(record)

        mqttSourceTask.stop()

        verify(exactly = 2) { mqttClient.unsubscribe("temp") }
        verify(exactly = 2) { mqttClient.unsubscribe("hum") }
        verify(exactly = 2) { mqttClient.disconnectForcibly(60000) }

        Assertions.assertEquals(0, mqttSourceTask.queue.size)

        confirmVerified(mqttClient)
    }

    @Test
    fun `Testing version method`() {
        val mqttSourceTask2 = MqttSourceTask()
        Assertions.assertNotNull(mqttSourceTask2)
        Assertions.assertEquals(Version.VERSION, mqttSourceTask2.version())
    }
}
