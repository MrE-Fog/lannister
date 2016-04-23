package net.anyflow.lannister.messagehandler;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.hazelcast.core.ITopic;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import net.anyflow.lannister.session.LiveSessions;
import net.anyflow.lannister.session.Message;
import net.anyflow.lannister.session.Repository;
import net.anyflow.lannister.session.Session;
import net.anyflow.lannister.session.SessionTopic;

public class MqttSubscribeMessageHandler extends SimpleChannelInboundHandler<MqttSubscribeMessage> {

	// TODO send retain message [MQTT-3.3.1-6]
	// TODO retain message flag should be true [MQTT-3.3.1-8]

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MqttSubscribeMessageHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MqttSubscribeMessage msg) throws Exception {
		logger.debug("packet incoming : {}", msg.toString());

		Session session = LiveSessions.SELF.getByChannelId(ctx.channel().id());
		if (session == null) {
			logger.error("None exist session message : {}", msg.toString());
			return;
		}

		session.setLastIncomingTime(new Date());

		List<MqttTopicSubscription> topics = msg.payload().topicSubscriptions();

		List<Integer> grantedQoss = Lists.newArrayList();

		for (MqttTopicSubscription item : topics) {
			ITopic<Message> topic = Repository.SELF.topic(item.topicName());

			String registrationId = topic.addMessageListener(session);
			session.topics().put(topic.getName(), new SessionTopic(registrationId, item.qualityOfService()));

			grantedQoss.add(item.qualityOfService().value());
		}

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_LEAST_ONCE, false,
				2 + grantedQoss.size());
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(msg.variableHeader().messageId());
		MqttSubAckPayload payload = new MqttSubAckPayload(grantedQoss);

		session.send(new MqttSubAckMessage(fixedHeader, variableHeader, payload));
	}
}