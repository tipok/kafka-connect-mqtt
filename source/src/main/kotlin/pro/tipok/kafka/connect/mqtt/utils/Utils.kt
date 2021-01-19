package pro.tipok.kafka.connect.mqtt.utils

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.data.Timestamp
import org.apache.kafka.connect.source.SourceRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.*
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

    //val DATETIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}

typealias SourceRecordQueue = LinkedBlockingQueue<SourceRecord>

private const val TOPIC = "topic"
private const val DATE_TIME = "dateTime"
private const val ID_PROPERTY = "id"
private const val RETAINED = "retained"
private const val DUPLICATE = "duplicate"
private const val QOS = "qos"
private const val PAYLOAD = "payload"

val SCHEMA: Schema = SchemaBuilder.struct()
    .name(MqttMessage::class.java.simpleName)
    .field(TOPIC, Schema.STRING_SCHEMA)
    .field(DATE_TIME, Timestamp.SCHEMA)
    .field(ID_PROPERTY, Schema.INT32_SCHEMA)
    .field(RETAINED, Schema.BOOLEAN_SCHEMA)
    .field(DUPLICATE, Schema.BOOLEAN_SCHEMA)
    .field(QOS, Schema.INT32_SCHEMA)
    .field(PAYLOAD, Schema.STRING_SCHEMA)
    .build()


data class MqttMessage(
    val topic: String,
    val dateTime: ZonedDateTime,
    val id: Int,
    val retained: Boolean,
    val duplicate: Boolean,
    val qos: Int,
    val payload: String
) {
    fun toStruct(): Struct {
        return Struct(SCHEMA)
            .put(DATE_TIME, Date.from(dateTime.toInstant()))
            .put(TOPIC, topic)
            .put(ID_PROPERTY, id)
            .put(RETAINED, retained)
            .put(DUPLICATE, duplicate)
            .put(QOS, qos)
            .put(PAYLOAD, payload)
    }
}
