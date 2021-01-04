package pro.tipok.kafka.connect.mqtt.utils

import org.apache.kafka.connect.source.SourceRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

/**
 * Utils.
 *
 * @author  tipok
 * @version 1.0
 * @since   03.01.21
 */
object Utils {
    inline fun <reified R : Any> R.logger(): Logger =
        LoggerFactory.getLogger(this::class.java)
}

typealias SourceRecordQueue = LinkedBlockingQueue<SourceRecord>

data class MqttMessage(
    val topic: String,
    val id: Int,
    val retained: Boolean,
    val duplicate: Boolean,
    val qos: Int,
    val payload: String
)
