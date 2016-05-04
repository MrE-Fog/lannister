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

package net.anyflow.lannister.message;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import net.anyflow.lannister.Literals;

public abstract class MessageStatus implements com.hazelcast.nio.serialization.Portable {

	@JsonProperty
	private String clientId;
	@JsonProperty
	private int messageId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Literals.DATE_DEFAULT_FORMAT, timezone = Literals.DATE_DEFAULT_TIMEZONE)
	@JsonProperty
	private Date createTime;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Literals.DATE_DEFAULT_FORMAT, timezone = Literals.DATE_DEFAULT_TIMEZONE)
	@JsonProperty
	protected Date updateTime;

	public MessageStatus() { // just for Serialization
	}

	protected MessageStatus(String clientId, int messageId) {
		this.clientId = clientId;
		this.messageId = messageId;
		this.createTime = new Date();
		this.updateTime = createTime;
	}

	public String key() {
		return Message.key(clientId, messageId);
	}

	public String clientId() {
		return clientId;
	}

	public int messageId() {
		return messageId;
	}

	public Date createTime() {
		return createTime;
	}

	public Date updateTime() {
		return updateTime;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF("clientId", clientId);
		writer.writeInt("messageId", messageId);
		writer.writeLong("createTime", createTime.getTime());
		writer.writeLong("updateTime", updateTime.getTime());
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		clientId = reader.readUTF("clientId");
		messageId = reader.readInt("messageId");
		createTime = new Date(reader.readLong("createTime"));
		updateTime = new Date(reader.readLong("updateTime"));
	}
}