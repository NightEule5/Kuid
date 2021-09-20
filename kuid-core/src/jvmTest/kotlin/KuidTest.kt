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

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone.Companion.UTC
import kotlinx.datetime.toInstant
import dev.strixpyrr.kuid.snowflake.SnowflakeGenerator
import dev.strixpyrr.kuid.snowflake.SnowflakeLayout
import dev.strixpyrr.kuid.snowflake.SnowflakeLayout.Discord
import java.time.Month.JANUARY
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
object KuidTest : FunSpec()
{
	init
	{
		context(name = "Snowflakes")
		{
			val epoch =
				LocalDateTime(
					year = 2021,
					month = JANUARY,
					dayOfMonth = 1,
					hour = 1,
					minute = 1
				).toInstant(timeZone = UTC).toEpochMilliseconds()
			
			val customLayout = SnowflakeLayout(epoch)
			
			context(name = "Generation")
			{
				fun discordGenerator(increment: Long = 0L, timestamp: Long = -1L) =
					SnowflakeGenerator(Discord, workerId = 12, processId = 7, increment, timestamp)
				
				var generator = discordGenerator()
				
				val snowflake = shouldNotThrowAny { generator.next() }
				
				test("Property Retrieval")
				{
					shouldNotThrowAny()
					{
						val time = Clock.System.now().toEpochMilliseconds()
						
						(snowflake[Discord.timestamp].toEpochMilliseconds() - time) shouldBeLessThanOrEqual 50L
						snowflake[Discord. workerId] shouldBe 12
						snowflake[Discord.processId] shouldBe 7
						snowflake[Discord.increment] shouldBe 0
					}
				}
				
				test("Succession")
				{
					var last = snowflake
					
					repeat(10)
					{
						val next = generator.nextAsync().await()
						
						next.shouldBeGreaterThan(last)
						
						last = next
					}
				}
				
				// Test the rollover behavior. It should reset the increment and delay 1ms.
				test("Increment Rollover")
				{
					// Todo: Is the JVM too slow to reliably test this to within 1ms?
					
					val time = Clock.System.now().toEpochMilliseconds()
					
					generator = discordGenerator(increment = 4095L, timestamp = time)
					
					val rollover = generator.next()
					
					rollover[Discord.increment].shouldBe(0)
					rollover[Discord.timestamp].toEpochMilliseconds().shouldBeGreaterThan(time) // Delayed 1ms
				}
				
				test("Parameter Range Exceptions")
				{
					shouldThrow<IllegalArgumentException>
					{
						SnowflakeGenerator(Discord, workerId = 32, processId = 0)
					}
					
					shouldThrow<IllegalArgumentException>
					{
						SnowflakeGenerator(Discord, workerId = 0, processId = 32)
					}
					
					shouldThrow<IllegalArgumentException>
					{
						SnowflakeGenerator(Discord, workerId = 0, processId = 0, increment = 4096L)
					}
				}
				
				test("1 million Benchmark")
				{
					generator = discordGenerator()
					
					val time = measureTime()
					{
						repeat(1_000_000)
						{
							generator.next()
						}
					}
					
					val ms = time.inWholeMilliseconds
					val ns = (ms * 1000).toDouble()
					
					// Yes, all this to round to 1 digit of precision.
					var speed = floor(ns / 1_000_000)
					speed += (((ns / 1_000_000) - speed) * 10).roundToLong() / 10.0
					
					println(
						"1 million Ids took ${ms}ms to generate, at a speed of " +
						"~${speed}ns per Id."
					)
				}
			}
		}
	}
}