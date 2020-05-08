package com.wxf.distribution.trace.client;

import com.wxf.distribution.trace.common.protocol.CommuDecoder;
import com.wxf.distribution.trace.common.protocol.CommuEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TraceClient extends SimpleChannelInboundHandler<String> {

  private String host;
  private int port;

  private ChannelFuture future = null;

  private volatile String response;

  public TraceClient(String host, int port) {
    this.host = host;
    this.port = port;
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group).channel(NioSocketChannel.class)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) {
              // 向pipeline中添加编码、解码、业务处理的handler
              channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
              channel.pipeline().addLast(new LengthFieldPrepender(2));
              channel.pipeline().addLast(new CommuEncoder(String.class));
              channel.pipeline().addLast(new CommuDecoder(String.class));
              channel.pipeline().addLast(TraceClient.this);                   //IN - 2
            }
          }).option(ChannelOption.SO_KEEPALIVE, true);
      // 链接服务器
      future = bootstrap.connect(host, port).sync();
    } catch (Exception e) {
      e.printStackTrace();
      try {
        future.channel().closeFuture().sync();
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      group.shutdownGracefully();
    }
  }

  /**
   * 链接服务端，发送请求消息
   */
  public void send(String request) throws Exception {
    future.channel().writeAndFlush(request);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s)
      throws Exception {
    log.info("server resp:" + s);
  }
}
