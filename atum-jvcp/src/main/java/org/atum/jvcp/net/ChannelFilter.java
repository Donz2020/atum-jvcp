package org.atum.jvcp.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class ChannelFilter extends ChannelInboundHandlerAdapter {

	  @Override
	  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		  
	  }
	  
	  @Override
	  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		  
	  }
}
