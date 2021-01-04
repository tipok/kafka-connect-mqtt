package pro.tipok.kafka.connect.mqtt

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.source.SourceConnector
import org.slf4j.Logger
import pro.tipok.kafka.connect.mqtt.utils.Configuration
import pro.tipok.kafka.connect.mqtt.utils.Utils.logger
import pro.tipok.kafka.connect.mqtt.utils.Version
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.connect.errors.ConnectException


/**
 * pro.tipok.kafka.connect.mqtt.MqttSourceConnector.
 *
 * @author  tipok
 * @version 1.0
 * @since   03.01.21
 */
@Suppress("unused")
class MqttSourceConnector: SourceConnector() {

    private var props: MutableMap<String, String>? = null

    companion object {
        val logger: Logger = logger()
    }

    override fun version(): String {
        return Version.VERSION
    }

    override fun start(props: MutableMap<String, String>?) {
        try {
            Configuration.validate(props)
        } catch (e: ConfigException) {
            throw ConnectException("Couldn't start MqttSourceConnector due to configuration error", e)
        }
        this.props = props
    }

    override fun taskClass(): Class<out Task> {
        return MqttSourceTask::class.java
    }

    override fun taskConfigs(maxTasks: Int): MutableList<MutableMap<String, String>> {
        val result: MutableList<MutableMap<String, String>> = arrayListOf()
        // split uris among available tasks
        val uris = props?.get(Configuration.MQTT_HOSTS_CONFIG)?.split(",")?.map { uri -> uri.trim() } ?: emptyList()
        var numberOfUris = uris.size
        var rest = numberOfUris % maxTasks
        val urisBatchSize = (numberOfUris - rest) / maxTasks
        var offset = 0
        while (numberOfUris > 0) {
            var partOfRest = 1
            if (rest <= 0) {
                partOfRest = 0
            }
            rest -= 1
            val limit = urisBatchSize + partOfRest
            numberOfUris -= limit
            val urisSlice = uris.subList(offset, (offset + limit))
            offset += limit
            val taskProps = HashMap(this.props)
            taskProps[Configuration.MQTT_HOSTS_CONFIG] = urisSlice.joinToString(separator = ",") { it }
            result.add(taskProps)
        }

        return result
    }

    override fun stop() {
        logger.info("Stopped MqttSourceConnector")
    }

    override fun config(): ConfigDef {
        return Configuration.CONFIG_DEF
    }
}
