package no.nav.syfo.kafka

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.ExtendedDeserializer

/*
Use the multi function deserializer from https://github.com/navikt/syfokafka/blob/master/serialisering/src/main/kotlin/no/nav/syfo/kafka/soknad/deserializer/MultiFunctionDeserializer.kt
and adopt it for a older spring version
 */
class LegacyMultiFunctionDeserializer<T>(
    private val deserializeMap: Map<String, (h: Headers?, b: ByteArray?) -> T>,
    private val deserializeDefault: ((b: ByteArray?) -> T)? = null
) : ExtendedDeserializer<T> {
    override fun configure(configuration: MutableMap<String, *>, p1: Boolean) {}

    override fun deserialize(topic: String, headers: Headers?, bytes: ByteArray?): T =
        getLastHeaderByKeyAsString(headers, MELDINGSTYPE)
            ?.let { deserializeMap[it]?.invoke(headers, bytes) } ?: deserialize(topic, bytes)

    override fun deserialize(topic: String, bytes: ByteArray?): T {
        val deserialize = deserializeDefault
            ?: throw IllegalArgumentException("Default mapperfunksjon er ikke definert")
        return deserialize(bytes)
    }

    override fun close() {}
}
