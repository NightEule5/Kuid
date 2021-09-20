// Copyright 2021 Strixpyrr
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package dev.strixpyrr.kuid.snowflake

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind.LONG
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private const val SerialName = "dev.strixpyrr.kuid.snowflake.Snowflake"

/**
 * Serializes a [Snowflake] as its [Long] value. This is the default behavior.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Snowflake::class)
object SnowflakeValueSerializer : KSerializer<Snowflake>
{
	override val descriptor = PrimitiveSerialDescriptor(SerialName, kind = LONG)
	
	@OptIn(ExperimentalUnsignedTypes::class)
	private val uLongDescriptor = ULong.serializer().descriptor
	
	override fun deserialize(decoder: Decoder) =
		Snowflake(value = decoder.decodeLong())
	
	override fun serialize(encoder: Encoder, value: Snowflake) =
		encoder.encodeLong(value = value.value)
}

/**
 * Serializes a [Snowflake] as a [String] representation of its numeric value.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Snowflake::class)
object SnowflakeStringSerializer : KSerializer<Snowflake>
{
	override val descriptor = PrimitiveSerialDescriptor(SerialName, kind = STRING)
	
	override fun deserialize(decoder: Decoder): Snowflake
	{
		val value = decoder.decodeString()
		
		return Snowflake(
			value.toLongOrNull() ?:
			throw SerializationException(
				"Malformed Snowflake: the decoded string \"$value\" cannot be " +
				"parsed to a long."
			)
		)
	}
	
	override fun serialize(encoder: Encoder, value: Snowflake) =
		encoder.encodeString(value = value.value.toString())
}

// Todo: Create a structural serializer.