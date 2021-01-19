package pro.tipok.kafka.connect.mqtt.utils

import com.beust.klaxon.Klaxon
import org.apache.kafka.connect.source.SourceRecord
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.slf4j.Logger
import pro.tipok.kafka.connect.mqtt.utils.Utils.logger
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import org.eclipse.paho.client.mqttv3.MqttMessage as MqttMes

/**
 * MessageListener.
 *
 * @author  tipok
 * @version 1.0
 * @since   03.01.21
 */
class MessageListener(
    private val kafkaTopic: String,
    private val queue: SourceRecordQueue
): MqttCallback {
    companion object {
        private val logger: Logger = logger()
    }

    private val base64Encoder = Base64.getEncoder()
    private val k = Klaxon()

    override fun connectionLost(cause: Throwable?) {
        logger.info("Connection lost:", cause)
    }

    override fun messageArrived(topic: String?, message: MqttMes?) {
        val incomingTopic = topic ?: ""

        if (message == null) {
            return
        }

        val payload = this.base64Encoder.encodeToString(message.payload)

        val now = ZonedDateTime.now(ZoneOffset.UTC)

        val incomingMessage = MqttMessage(
            incomingTopic,
            now,
            message.id,
            message.isRetained,
            message.isDuplicate,
            message.qos,
            payload
        )

        val jsonMessage: String = try { k.toJsonString(incomingMessage) } catch (e: Exception) {
            logger.error("Could not serialize MqttMessage: {}", e, incomingMessage)
            null
        } ?: return

        logger.info("Handling incoming message on topic {}: {}", incomingTopic, jsonMessage)

        val record = SourceRecord(
            null,
            null,
            this.kafkaTopic,
            null,
            SCHEMA,
            incomingMessage.toStruct()
        )
        this.queue.put(record)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {

    }
}
