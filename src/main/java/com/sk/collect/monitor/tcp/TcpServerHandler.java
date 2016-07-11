package com.sk.collect.monitor.tcp;

import com.sk.collect.monitor.service.ElasticsearchService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

@Sharable
public class TcpServerHandler extends ChannelInboundHandlerAdapter {
	private ElasticsearchService elasticsearchService;

	public TcpServerHandler(ElasticsearchService elasticsearchService) {
		this.elasticsearchService = elasticsearchService;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf in = (ByteBuf) msg;
		String result = "";
		try {
			while (in.isReadable()) {
				result += (char) in.readByte();
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}

		elasticsearchService.indexCount(result);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("TCP Channel Active");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("TCP Channel Inactive");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}