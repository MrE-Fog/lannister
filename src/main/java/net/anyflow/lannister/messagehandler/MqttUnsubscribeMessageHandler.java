package net.anyflow.lannister.messagehandler;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import net.anyflow.lannister.session.Session;
import net.anyflow.lannister.session.SessionNexus;
import net.anyflow.lannister.session.TopicNexus;
import net.anyflow.lannister.session.TopicRegister;

public class MqttUnsubscribeMessageHandler extends SimpleChannelInboundHandler<MqttUnsubscribeMessage> {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MqttUnsubscribeMessageHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) throws Exception {
		logger.debug(msg.toString());

		Session session = SessionNexus.SELF.getByChannelId(ctx.channel().id().toString());
		if (session == null) {
			// TODO handing null session
			return;
		}

		List<String> topicNames = msg.payload().topics();
		for (String item : topicNames) {
			TopicRegister tr = session.topicRegisters().remove(item);
			if (tr == null) {
				continue;
			}

			TopicNexus.SELF.get(item).removeMessageListener(tr.registrationId());
		}

		MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false,
				2);

		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(session.nextMessageId());

		ctx.channel().writeAndFlush(new MqttUnsubAckMessage(fixedHeader, variableHeader));

		logger.debug("MqttUnsubscribeMessageHandler execution finished.");
	}
}