package jp.co.sankosc.webapiclientsample

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Message(
    val message: String?
)

@Serializable
data class Login(
    val email: String,
    val password: String
)

@Serializable
data class Token(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int
)

@Serializable
data class Me(
    val id: Int,
    val name: String,
    val email: String,
    @SerialName("email_verified_at")
    val emailVerifiedAt: String?,
    @SerialName("created_at")
    @Serializable(with=DateSerializer::class)
    val createdAt: Date,
    @SerialName("updated_at")
    @Serializable(with=DateSerializer::class)
    val updatedAt: Date
)

@Serializer(forClass = Date::class)
object DateSerializer: KSerializer<Date> {
    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'000000Z'")

    override val descriptor: SerialDescriptor
            = PrimitiveSerialDescriptor("WithCustomDefault", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, obj: Date) {
        encoder.encodeString(df.format(obj))
    }

    override fun deserialize(decoder: Decoder): Date {
        return df.parse(decoder.decodeString())
    }
}
