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
package dev.strixpyrr.kuid

import dev.strixpyrr.kuid.snowflake.SnowflakeGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope

/**
 * Opens a [ReceiveChannel] that produces Ids on an always-ready, as-needed basis.
 * [ReceiveChannel.cancel] must be called when you are done receiving.
 *
 * The resulting channel generates a new Id and waits for it to be accepted. As
 * such, for time-based generators like [SnowflakeGenerator], the time field will
 * be from before an Id is requested on the receive-side. The size of this time
 * discrepancy depends on how long ago an Id was requested. If a more exact time
 * field is desirable, use the [IIdGenerator] interface directly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <I : Any> IIdGenerator<I>.asChannel() = coroutineScope()
{
	produce(capacity = RENDEZVOUS)
	{
		while (!isClosedForSend) send(next())
	}
}