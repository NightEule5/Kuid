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

import dev.strixpyrr.kuid.internal.maxCountOf
import dev.strixpyrr.kuid.internal.maxValueOf
import kotlinx.datetime.Instant
import kotlin.jvm.JvmField
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

/**
 * @property maxWorkers
 * Gets the maximum unique worker count each process can support without collision.
 * This is equal to `2^n` where `n` is [workerIdLength].
 * @property maxProcesses
 * Gets the maximum unique process count supported without collision.
 * This is equal to `2^n` where `n` is [processIdLength].
 * @property maxGenerators
 * Gets the maximum unique generator count supported without collision.
 * This is equal to the sum of [maxWorkers] and [maxProcesses].
 * @property maxIncrements
 * Gets the maximum increments until a 1ms delay is needed to avoid collision.
 * This is equal to `2^n` where `n` is [incrementLength].
 * @property timeOverflowDuration
 * Gets the time [Duration] until timestamp field will overflow.
 * @property timeOverflowInstant
 * Gets the [Instant] the timestamp field will overflow.
 */
@OptIn(ExperimentalTime::class)
sealed class SnowflakeLayout(
	@JvmField val timestampEpoch: Long,
	@JvmField val timestampLength: Int = DefaultTimestampLength,
	@JvmField val  workerIdLength: Int = DefaultWorkerIdLength,
	@JvmField val processIdLength: Int = DefaultProcessIdLength,
	@JvmField val incrementLength: Int = DefaultIncrementLength
)
{
	// Adapted from the now archived Twitter Snowflake code.
	@JvmField val timestampShift =             64 - timestampLength
	@JvmField val  workerIdShift = timestampShift -  workerIdLength
	@JvmField val processIdShift =  workerIdShift - processIdLength
	@JvmField val  processIdMask = (-1L shl processIdLength).inv() shl processIdShift
	@JvmField val   workerIdMask = (-1L shl  workerIdLength).inv() shl  workerIdShift
	@JvmField val  incrementMask = (-1L shl incrementLength).inv()
	
	private inline val idLength get() = workerIdLength + processIdLength
	
	// Metadata from snowflake-id and by extension, IdGen
	val maxWorkers    get() = maxCountOf( workerIdLength)
	val maxProcesses  get() = maxCountOf(processIdLength)
	val maxGenerators get() = maxCountOf(       idLength)
	val maxIncrements get() = maxCountOf(incrementLength)
	val maxTimestamps get() = maxCountOf(timestampLength)
	val timeOverflowDuration get() =
		milliseconds(maxTimestamps)
	val timeOverflowInstant  get() =
		Instant.fromEpochMilliseconds(timestampEpoch) +
		timeOverflowDuration
	
	object Twitter : SnowflakeLayout(1288834974657L, 42, 5, 5, 12)
	object Discord : SnowflakeLayout(1420070400000L, 42, 5, 5, 12)
	
	class Custom(
		timestampEpoch: Long,
		timestampLength: Int,
		workerIdLength: Int,
		processIdLength: Int,
		incrementLength: Int
	) : SnowflakeLayout(
		timestampEpoch,
		timestampLength,
		workerIdLength,
		processIdLength,
		incrementLength
	)
	{
		init
		{
			val total = timestampLength + workerIdLength + processIdLength + incrementLength
			
			require(timestampLength > 0) { "The timestamp length $timestampLength is invalid: it must be positive."  }
			require( workerIdLength > 0) { "The worker Id length $workerIdLength is invalid: it must be positive."   }
			require(processIdLength > 0) { "The process Id length $processIdLength is invalid: it must be positive." }
			require(incrementLength > 0) { "The increment length $incrementLength is invalid: it must be positive."  }
			require(total == 64) { "The total length $total is invalid: the lengths must add up to 64." }
		}
	}
	
	class Mutable
	{
		var timestampEpoch: Long? = null
		var timestampLength: Int  = DefaultTimestampLength
		var  workerIdLength: Int  = DefaultWorkerIdLength
		var processIdLength: Int  = DefaultProcessIdLength
		var incrementLength: Int  = DefaultIncrementLength
		
		private inline val idLength get() = workerIdLength + processIdLength
		
		// Metadata
		val maxWorkers    get() = maxValueOf( workerIdLength)
		val maxProcesses  get() = maxValueOf(processIdLength)
		val maxGenerators get() = maxValueOf(       idLength)
		val maxIncrements get() = maxCountOf(incrementLength)
		val maxTimestamps get() = maxCountOf(timestampLength)
		val timeOverflowDuration get() =
			milliseconds(maxTimestamps)
		val timeOverflowInstant  get() =
			Instant.fromEpochMilliseconds(timestampEpochOrThrow) +
			timeOverflowDuration
		
		private val timestampEpochOrThrow get() =
			timestampEpoch ?:
			throw Exception("The TimestampEpoch property was not set.")
		
		fun finalize(): SnowflakeLayout
		{
			val timestampEpoch = timestampEpochOrThrow
			
			return Custom(
				timestampEpoch,
				timestampLength,
				 workerIdLength,
				processIdLength,
				incrementLength
			)
		}
	}
	
	companion object
	{
		private const val DefaultTimestampLength = 42
		private const val DefaultWorkerIdLength  = 5
		private const val DefaultProcessIdLength = 5
		private const val DefaultIncrementLength = 12
		
		inline operator fun invoke(block: Mutable.() -> Unit) =
			Mutable().apply(block).finalize()
		
		operator fun invoke(
			timestampEpoch: Instant,
			timestampLength: Int = DefaultTimestampLength,
			 workerIdLength: Int = DefaultWorkerIdLength,
			processIdLength: Int = DefaultProcessIdLength,
			incrementLength: Int = DefaultIncrementLength
		) = invoke(
				timestampEpoch.toEpochMilliseconds(),
				timestampLength,
				 workerIdLength,
				processIdLength,
				incrementLength
			)
		
		operator fun invoke(
			timestampEpoch: Long,
			timestampLength: Int = DefaultTimestampLength,
			 workerIdLength: Int = DefaultWorkerIdLength,
			processIdLength: Int = DefaultProcessIdLength,
			incrementLength: Int = DefaultIncrementLength
		): SnowflakeLayout =
			Custom(
				timestampEpoch,
				timestampLength,
				workerIdLength,
				processIdLength,
				incrementLength
			)
	}
	
	private lateinit var _timestamp: Property<Instant>
	private lateinit var  _workerId: Property<Int>
	private lateinit var _processId: Property<Int>
	private lateinit var _increment: Property<Int>
	
	val timestamp: Property<Instant>
		get() =
			if (::_timestamp.isInitialized)
				_timestamp
			else
			{
				val value = Property.Timestamp(parent = this)
				
				_timestamp = value
				
				value
			}
	
	val workerId: Property<Int>
		get() =
			if (::_workerId.isInitialized)
				_workerId
			else
			{
				val value = Property.WorkerId(parent = this)
				
				_workerId = value
				
				value
			}
	
	val processId: Property<Int>
		get() =
			if (::_processId.isInitialized)
				_processId
			else
			{
				val value = Property.ProcessId(parent = this)
				
				_processId = value
				
				value
			}
	
	val increment: Property<Int>
		get() =
			if (::_increment.isInitialized)
				_increment
			else
			{
				val value = Property.Increment(parent = this)
				
				_increment = value
				
				value
			}
	
	sealed class Property<V>(
		@JvmField protected val parent: SnowflakeLayout
	)
	{
		abstract fun from(snowflake: Snowflake): V
		
		internal class Timestamp(
			parent: SnowflakeLayout
		) : Property<Instant>(parent)
		{
			override fun from(snowflake: Snowflake): Instant
			{
				val shift: Int
				val epoch: Long
				
				parent.run()
				{
					shift = timestampShift
					epoch = timestampEpoch
				}
				
				val value = snowflake.value
				
				val timestamp = epoch + (value shr shift)
				
				return Instant.fromEpochMilliseconds(timestamp)
			}
		}
		
		internal class WorkerId(
			parent: SnowflakeLayout
		) : Property<Int>(parent)
		{
			override fun from(snowflake: Snowflake): Int
			{
				val mask: Long
				val shift: Int
				
				parent.run()
				{
					mask  = workerIdMask
					shift = workerIdShift
				}
				
				val value = snowflake.value
				
				return ((value and mask) shr shift).toInt()
			}
		}
		
		internal class ProcessId(
			parent: SnowflakeLayout
		) : Property<Int>(parent)
		{
			override fun from(snowflake: Snowflake): Int
			{
				val mask: Long
				val shift: Int
				
				parent.run()
				{
					mask  = processIdMask
					shift = processIdShift
				}
				
				val value = snowflake.value
				
				return ((value and mask) shr shift).toInt()
			}
		}
		
		internal class Increment(
			parent: SnowflakeLayout
		) : Property<Int>(parent)
		{
			override fun from(snowflake: Snowflake): Int
			{
				val mask: Long
				
				parent.run()
				{
					mask  = incrementMask
				}
				
				val value = snowflake.value
				
				return (value and mask).toInt()
			}
		}
	}
}