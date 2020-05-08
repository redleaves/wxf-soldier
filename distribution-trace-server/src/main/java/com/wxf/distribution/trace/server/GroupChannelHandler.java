package com.wxf.distribution.trace.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupChannelHandler extends SimpleChannelInboundHandler<String> {

  //保留所有与服务器建立连接的channel对象，这边的GlobalEventExecutor在写博客的时候解释一下，看其doc
  private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg)
      throws Exception {
    log.info("receive msg:"+msg);
    Channel channel = channelHandlerContext.channel();
    channelGroup.forEach(ch -> {
      if (channel != ch) {
        ch.writeAndFlush(channel.remoteAddress() + " 发送的消息:" + msg + " \n");
      } else {
        ch.writeAndFlush(" 【自己】" + msg + " \n");//TODO 不发给自己
      }
    });
    //ps 还可以通过 channelGroup.remove(channel);channelGroup.writeAndFlush(msg);实现发送除自己之外的client
  }

  //表示服务端与客户端连接建立
  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();  //其实相当于一个connection，获取当前客户端连接的channel
    channelGroup.writeAndFlush(" 【服务器】 -" +channel.remoteAddress() +" 加入\n");
    channelGroup.add(channel);//先写再加入 是为了避免发给自己

  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();
    channelGroup.writeAndFlush(" 【服务器】 -" +channel.remoteAddress() +" 离开\n");

    //验证一下每次客户端断开连接，连接自动地从channelGroup中删除调。
    System.out.println(channelGroup.size());
    //当客户端和服务端断开连接的时候，下面的那段代码netty会自动调用，所以不需要人为的去调用它
    //channelGroup.remove(channel);
  }

  //连接处于活动状态
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();
    System.out.println(channel.remoteAddress() +" 上线了");
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();
    System.out.println(channel.remoteAddress() +" 下线了");
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}
