package pro.tipok.kafka.connect.mqtt.utils

import org.apache.kafka.common.config.ConfigDef

/**
 * Configuration.
 *
 * @author  tipok
 * @version 1.0
 * @since   03.01.21
 */
object Configuration {
    const val MQTT_HOSTS_CONFIG = "mqtt.server.uri"
    const val MQTT_PASSWORD_CONFIG = "mqtt.password"
    const val MQTT_USERNAME_CONFIG = "mqtt.username"
    const val MQTT_CLEAN_SESSION_CONFIG = "mqtt.clean.session.enabled"
    const val MQTT_TIMEOUT_CONFIG = "mqtt.connect.timeout.seconds"
    const val MQTT_KEEPALIVE_CONFIG = "mqtt.keepalive.interval.seconds"
    const val MQTT_MAX_RETRY_TIME_CONFIG = "max.retry.time.ms"
    const val MQTT_MQTT_TOPICS_CONFIG = "mqtt.topics"
    const val MQTT_KAFKA_TOPIC_CONFIG = "kafka.topic"
    const val MQTT_MQTT_QOS_CONFIG = "mqtt.qos"
    val CONFIG_DEF: ConfigDef = ConfigDef()
        .define(
            MQTT_HOSTS_CONFIG,
            ConfigDef.Type.LIST,
            ConfigDef.Importance.HIGH,
            "List of MQTT brokers to connect to."
        )
        .define(
            MQTT_PASSWORD_CONFIG,
            ConfigDef.Type.PASSWORD,
            "[hidden]",
            ConfigDef.Importance.HIGH,
            "Password to connect with."
        )
        .define(
            MQTT_USERNAME_CONFIG,
            ConfigDef.Type.STRING,
            "",
            ConfigDef.Importance.HIGH,
            "Username to connect with, or blank to connect without a username."
        )
        .define(
            MQTT_CLEAN_SESSION_CONFIG,
            ConfigDef.Type.BOOLEAN,
            true,
            ConfigDef.Importance.LOW,
            "Sets whether the client and server should remember state across restarts and reconnects."
        )
        .define(
            MQTT_TIMEOUT_CONFIG,
            ConfigDef.Type.INT,
            30,
            ConfigDef.Range.atLeast(1),
            ConfigDef.Importance.LOW,
            "Sets the connection timeout value."
        )
        .define(
            MQTT_KEEPALIVE_CONFIG,
            ConfigDef.Type.INT,
            60,
            ConfigDef.Range.atLeast(1),
            ConfigDef.Importance.LOW,
            "Sets the “keep alive” interval."
        )
        .define(
            MQTT_MAX_RETRY_TIME_CONFIG,
            ConfigDef.Type.INT,
            30000,
            ConfigDef.Range.between(10000, Int.MAX_VALUE),
            ConfigDef.Importance.MEDIUM,
            "The maximum time in milliseconds (ms) the connector will spend backing off and retrying " +
                    "failed operations (connecting to the MQTT broker and publishing records)."
        )
        .define(
            MQTT_MQTT_TOPICS_CONFIG,
            ConfigDef.Type.LIST,
            "#",
            ConfigDef.Importance.HIGH,
            "List of MQTT topics to subscribe to."
        )
        .define(
            MQTT_KAFKA_TOPIC_CONFIG,
            ConfigDef.Type.STRING,
            ConfigDef.Importance.HIGH,
            "Kafka topic to publish to."
        )
        .define(
            MQTT_MQTT_QOS_CONFIG,
            ConfigDef.Type.INT,
            0,
            ConfigDef.Range.between(0, 2),
            ConfigDef.Importance.LOW,
            "MQTT QOS level to subscribe with."
        )


    fun validate(props: MutableMap<String, String>?) {
        CONFIG_DEF.parse(props)
    }
}
