
dependencies {
    implementation("org.apache.kafka:connect-api:${project.rootProject.ext["kafka_version"]}")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:${project.rootProject.ext["paho_version"]}")
    implementation("com.beust:klaxon:${project.rootProject.ext["klaxon_version"]}")
}
