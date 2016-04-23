package net.anyflow.lannister.messagehandler;

import java.util.Date;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import net.anyflow.lannister.session.LiveSessions;
import net.anyflow.lannister.session.Repository;
import net.anyflow.lannister.session.Session;
import net.anyflow.lannister.session.SessionTopic;

public class MqttUnsubscribeMessageHandler extends SimpleChannelInboundHandler<MqttUnsubscribeMessage> {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MqttUnsubscribeMessageHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) throws Exception {
		logger.debug("packet incoming : {}", msg.toString());

		Session session = LiveSessions.SELF.getByChannelId(ctx.channel().id());
		if (session == null) {
			logger.error("None exist session message : {}", msg.toString());
			return;
		}

		session.setLastIncomingTime(new Date());

		List<String> topicNames = msg.payload().topics();
		for (String item : topicNames) {
			SessionTopic tr = session.topics().remove(item);
			if (tr == null) {
				continue;
			}

			Repository.SELF.topic(item).removeMessageListener(tr.registrationId());
		}

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_LEAST_ONCE, false,
				2);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(msg.variableHeader().messageId());

		session.send(new MqttUnsubAckMessage(fixedHeader, variableHeader));
	}
}