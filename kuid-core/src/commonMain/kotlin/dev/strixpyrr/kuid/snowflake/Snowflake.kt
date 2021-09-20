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

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmField

@Serializable(with = SnowflakeValueSerializer::class)
data class Snowflake(@JvmField val value: Long) : Comparable<Snowflake>
{
	override fun compareTo(other: Snowflake) =
		value.compareTo(other.value)
	
	operator fun <V> get(property: SnowflakeLayout.Property<V>) =
		property.from(snowflake = this)
	
	fun convert(from: SnowflakeLayout, to: SnowflakeLayout) =
		if (from == to)
			this
		else
		{
			val timestamp = from.timestamp.from(this).toEpochMilliseconds()
			val  workerId = from. workerId.from(this)
			val processId = from.processId.from(this)
			val increment = from.increment.from(this).toLong()
			
			Snowflake(
				genSnowflake(
					timestamp,
					to.timestampEpoch,
					to.timestampShift,
					workerId,
					to.workerIdShift,
					processId,
					to.processIdShift,
					increment
				)
			)
		}
}
