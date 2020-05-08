package com.wxf.distribution.trace.server;

import com.wxf.distribution.trace.common.protocol.CommuDecoder;
import com.wxf.distribution.trace.common.protocol.CommuEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CenterServer {

  /**
   * 单例
   */
  private static CenterServer singleInstance = new CenterServer();

  public static CenterServer inst() {
    return singleInstance;
  }

  /**
   * 启动即时通讯服务器
   */
  public void start() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new CenterServerInitializer())
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true);
    ChannelFuture future = null;
    try {
      future = bootstrap.bind("127.0.0.1", 8084).sync();
      log.info("netty server started on port {}", 8084);
      future.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }


  }

  /**
   * 内部类
   */
  class CenterServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();
      pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
      // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段
      pipeline.addLast(new LengthFieldPrepender(2));
      // 对经过粘包和拆包处理之后的数据进行json反序列化，从而得到User对象
      pipeline.addLast(new CommuDecoder(String.class));
      pipeline.addLast(new CommuEncoder(String.class));
      pipeline.addLast(new GroupChannelHandler());
    }
  }
}
