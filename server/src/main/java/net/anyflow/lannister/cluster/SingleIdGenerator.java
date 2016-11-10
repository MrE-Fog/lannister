/*
 * Copyright 2016 The Lannister Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anyflow.lannister.cluster;

import com.hazelcast.core.IdGenerator;

public class SingleIdGenerator implements IdGenerator {

	private final String name;
	private long id;

	public SingleIdGenerator(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void destroy() {
		// DO NOTHING
	}

	@Override
	public boolean init(long id) {
		if (id < 0) { return false; }

		this.id = id;
		return true;
	}

	@Override
	public long newId() {
		return ++id;
	}

	@Override
	public String getPartitionKey() {
		throw new Error("The method should not be called");
	}

	@Override
	public String getServiceName() {
		throw new Error("The method should not be called");
	}
}