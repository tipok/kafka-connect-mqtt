package pro.tipok.kafka.connect.mqtt

import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.errors.ConnectException
import org.apache.kafka.connect.source.SourceRecord
import org.apache.kafka.connect.source.SourceTask
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import pro.tipok.kafka.connect.mqtt.utils.Configuration
import pro.tipok.kafka.connect.mqtt.utils.MessageListener
import pro.tipok.kafka.connect.mqtt.utils.SourceRecordQueue
import pro.tipok.kafka.connect.mqtt.utils.Version


/**
 * pro.tipok.kafka.connect.mqtt.MqttSourceTask.
 *
 * @author  tipok
 * @version 1.0
 * @since   03.01.21
 */
class MqttSourceTask(
    private val mqttClientSupplier: (String, String) -> MqttClient = { uri, clientId -> MqttClient(uri, clientId) }
) : SourceTask() {

    private var kafkaTopic: String = "mqtt"
    private var timeout: Int = 30
    private var qos: Int = 0
    private var topics: List<String> = emptyList()
    internal val queue = SourceRecordQueue()
    private val clients = mutableMapOf<String, MqttClient>()

    override fun version(): String {
        return Version.VERSION
    }

    override fun start(props: MutableMap<String, String>?) {
        try {
            Configuration.validate(props)
        } catch (e: ConfigException) {
            throw ConnectException("Couldn't start MqttSourceTask due to configuration error", e)
        }

        val uris = props?.get(Configuration.MQTT_HOSTS_CONFIG)?.split(",")
            ?.map { uri -> uri.trim() }?.filter { uri -> uri.isNotBlank() } ?: emptyList()
        if (uris.isEmpty()) {
            throw ConnectException(
                "Couldn't start MqttSourceTask due to missing configuration: ${Configuration.MQTT_HOSTS_CONFIG}"
            )
        }

        val cleanSession = props?.get(Configuration.MQTT_CLEAN_SESSION_CONFIG)?.toBoolean() ?: true
        this.timeout = props?.get(Configuration.MQTT_TIMEOUT_CONFIG)?.toInt() ?: 30
        val keepAliveInterval = props?.get(Configuration.MQTT_KEEPALIVE_CONFIG)?.toInt() ?: 60
        val maxReconnectDelay = props?.get(Configuration.MQTT_MAX_RETRY_TIME_CONFIG)?.toInt() ?: 30000
        this.qos = props?.get(Configuration.MQTT_MQTT_QOS_CONFIG)?.toInt() ?: 0
        this.topics = props?.get(Configuration.MQTT_MQTT_TOPICS_CONFIG)?.split(",")
            ?.map { topic -> topic.trim() }?.filter { topic -> topic.isNotBlank() } ?: listOf("#")
        val username = props?.get(Configuration.MQTT_USERNAME_CONFIG) ?: ""
        val password = props?.get(Configuration.MQTT_PASSWORD_CONFIG) ?: ""
        this.kafkaTopic = props?.get(Configuration.MQTT_KAFKA_TOPIC_CONFIG) ?: "mqtt"

        val connOpts = MqttConnectOptions()
        connOpts.isCleanSession = cleanSession
        connOpts.connectionTimeout = this.timeout
        connOpts.keepAliveInterval = keepAliveInterval
        connOpts.maxReconnectDelay = maxReconnectDelay
        connOpts.userName = username
        connOpts.password = password.toCharArray()

        uris.forEach { uri ->
            val clientId = MqttClient.generateClientId()
            val client = mqttClientSupplier(uri, clientId)
            clients[clientId] = client
            client.connect(connOpts)
            client.setCallback(MessageListener(this.kafkaTopic, this.queue))
            this.topics.forEach { topic ->
                client.subscribe(topic, this.qos)
            }
        }
    }

    override fun stop() {
        this.queue.clear()
        this.clients.forEach { (_, client) ->
            this.topics.forEach { topic -> client.unsubscribe(topic) }
            client.disconnectForcibly(this.timeout.toLong() * 1000)
        }
    }

    override fun poll(): MutableList<SourceRecord> {
        val result = ArrayList<SourceRecord>()
        while (this.queue.isNotEmpty()) {
            val record = this.queue.take()
            result.add(record)
        }
        return result
    }
}
