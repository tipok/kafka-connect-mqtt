package pro.tipok.kafka.connect.mqtt.utils

import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.LinkedBlockingQueue
import org.eclipse.paho.client.mqttv3.MqttMessage as MqttMes

/**
 * MessageListenerTests.
 *
 * @author  tipok
 * @version 1.0
 * @since   04.01.21
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageListenerTests {

    @Test
    fun `Test message arrived callback`() {
        val queue = LinkedBlockingQueue<SourceRecord>()
        val listener = MessageListener("mqtt", queue)
        listener.connectionLost(Exception())
        listener.deliveryComplete(null)

        val incomingMessage = MqttMes()
        incomingMessage.id = 12
        incomingMessage.isRetained = true
        incomingMessage.qos = 2
        incomingMessage.payload = "payload".toByteArray(Charsets.UTF_8)

        listener.messageArrived("temp", incomingMessage)

        Assertions.assertEquals(1, queue.size)

        val record = queue.take()

        Assertions.assertEquals("mqtt", record.topic())
        val struct = record.value() as Struct

        Assertions.assertEquals(false, struct.getBoolean("duplicate"))
        Assertions.assertEquals(true, struct.getBoolean( "retained"))
        Assertions.assertEquals(12, struct.getInt32("id"))
        Assertions.assertEquals(2, struct.getInt32("qos"))
        Assertions.assertEquals("temp", struct.getString("topic"))
        Assertions.assertEquals("cGF5bG9hZA==", struct.getString("payload"))

        listener.messageArrived("temp", null)
        Assertions.assertEquals(0, queue.size)
    }
}
