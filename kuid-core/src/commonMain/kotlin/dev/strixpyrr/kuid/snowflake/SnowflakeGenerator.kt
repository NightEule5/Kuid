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

import dev.strixpyrr.kuid.IIdGenerator
import dev.strixpyrr.kuid.internal.maxValueOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlin.jvm.JvmField

/**
 * @param layout The layout information used to package the generated values into
 * a [Snowflake].
 * @param workerId The worker Id. To prevent collisions, this must be unique to
 * each worker under the same [processId]. The range is [0,2^n), where n is the bit
 * length given by [SnowflakeLayout.workerIdLength] in the specified layout.
 * @param processId The process Id. To prevent collisions, this must be unique
 * across your application for each process with a distinct set of workers. The range
 * is [0,2^n), where n is the bit length given by [SnowflakeLayout.processIdLength]
 * in the specified layout.
 * @param increment The initial increment value. The default value is recommended
 * for most use cases.
 * @param initialTimestamp The initial timestamp value in epoch milliseconds. The
 * default value is recommended for most use cases.
 */
class SnowflakeGenerator(
	layout: SnowflakeLayout,
	@JvmField val workerId: Int,
	@JvmField val processId: Int,
	private var increment: Long = 0L,
	initialTimestamp: Long = -1L
) : IIdGenerator<Snowflake>
{
	private val timestampEpoch = layout.timestampEpoch
	private val timestampShift = layout.incrementLength + layout.workerIdLength + layout.processIdLength
	private val workerIdShift  = layout.incrementLength + layout.processIdLength
	private val processIdShift = layout.incrementLength
	private val maxIncrement   = layout.incrementMask
	
	private var previous = initialTimestamp
	
	private val clock = Clock.System
	
	init
	{
		require( workerId in 0..maxValueOf(layout. workerIdLength)) // [0,2^n)
		require(processId in 0..maxValueOf(layout.processIdLength))
		require(increment in 0..maxValueOf(layout.incrementLength))
	}
	
	private val mutex = Mutex()
	
	/**
	 * Generates the next [Snowflake], blocking the current thread until
	 * completion.
	 */
	override fun nextBlocking() = super.nextBlocking()
	
	/**
	 * Generates the next [Snowflake], suspending the current coroutine until
	 * completion.
	 */
	override suspend fun next(): Snowflake
	{
		mutex.lock()
		
		var time = clock.now().toEpochMilliseconds()
		
		val prev = previous
		var inc = increment
		
		check(time >= prev) { "The clock is moving backwards." }
		
		if (time == prev)
		{
			// We're generating in the same millisecond, increment the counter. If
			// it's at the maximum, reset it and delay for a millisecond to  avoid
			// a collision.
			
			if (++inc >= maxIncrement) inc = 0
			
			increment = inc
			
			if (inc == 0L)
			{
				delay(1)
				
				time = clock.now().toEpochMilliseconds()
			}
		}
		else increment = 0
		
		previous = time
		
		mutex.unlock()
		
		return Snowflake(generate(time))
	}
	
	/**
	 * Generates the next [Snowflake] asynchronously.
	 */
	override suspend fun nextAsync() = super.nextAsync()
	
	private fun generate(timestamp: Long) =
		genSnowflake(
			timestamp,
			timestampEpoch,
			timestampShift,
			workerId,
			workerIdShift,
			processId,
			processIdShift,
			increment
		)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun genSnowflake(
	timestamp: Long,
	epoch: Long,
	timestampShift: Int,
	workerId: Int,
	workerIdShift: Int,
	processId: Int,
	processIdShift: Int,
	increment: Long
) = ((timestamp - epoch) shl timestampShift)          or
	( workerId           shl  workerIdShift).toLong() or
	(processId           shl processIdShift).toLong() or
	increment