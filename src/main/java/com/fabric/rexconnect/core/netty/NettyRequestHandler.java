package com.fabric.rexconnect.core.netty;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringEncoder;

import org.apache.log4j.Logger;

import com.fabric.rexconnect.core.CommandHandler;
import com.fabric.rexconnect.core.SessionContext;
import com.fabric.rexconnect.core.io.PrettyJson;
import com.fabric.rexconnect.core.io.TcpResponse;

/*================================================================================================*/
public class NettyRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger vLog = Logger.getLogger(NettyRequestHandler.class);
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	@Override
	public void channelRead(ChannelHandlerContext pCtx, Object pMsg) {
		try {
			long t = System.currentTimeMillis();
			ByteBuf bb = (ByteBuf)pMsg;
			SessionContext sessCtx = new SessionContext();
			
			TcpResponse resp = CommandHandler.getResponse(t, sessCtx, new ByteBufInputStream(bb));
			ByteArrayOutputStream os = PrettyJson.getJsonStream(resp, false);
			pCtx.writeAndFlush(Unpooled.copiedBuffer(os.toByteArray()));
		}
		catch ( Exception e ) {
			vLog.error(e.getMessage(), e);
		}
	}
	
	/*--------------------------------------------------------------------------------------------* /
	@Override
	public void channelReadComplete(ChannelHandlerContext pCtx) {
		
	}

	/*--------------------------------------------------------------------------------------------*/
	@Override
	public void exceptionCaught(ChannelHandlerContext pCtx, Throwable pCause) {
		pCause.printStackTrace();
		pCtx.close();
	}

}