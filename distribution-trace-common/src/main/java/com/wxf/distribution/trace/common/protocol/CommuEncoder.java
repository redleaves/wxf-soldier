package com.wxf.distribution.trace.common.protocol;

import com.wxf.distribution.trace.common.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器
 *
 */
public class CommuEncoder extends MessageToByteEncoder {

	private Class<?> genericClass;

	// 构造函数传入向反序列化的class
	public CommuEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	public void encode(ChannelHandlerContext ctx, Object inob, ByteBuf out)
			throws Exception {
		//序列化
		if (genericClass.isInstance(inob)) {
			byte[] data = SerializationUtil.serialize(inob);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}
}