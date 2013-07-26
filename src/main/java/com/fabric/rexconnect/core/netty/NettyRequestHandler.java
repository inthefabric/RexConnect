package com.fabric.rexconnect.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayOutputStream;

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
			/*System.out.println("READ: "+bb.readableBytes());
			
			while ( !bb.isReadable(4) ) {}
			int len = bb.readInt();
			System.out.println("SIZE: "+len+" ... "+bb.readableBytes());
			int fail = 10;
			
			while ( !bb.isReadable(len) ) {
				System.out.println("READ: "+bb.readableBytes()+" / "+len);
				if ( --fail <= 0 ) { break; }
			}*/
			
			SessionContext sessCtx = new SessionContext();
			ByteBufInputStream bbs = new ByteBufInputStream(bb);
			/*StringWriter json = new StringWriter(); 
			String jsonLine;
			
			while ( (jsonLine = bbs.readLine()) != null ) {
				json.append(jsonLine);
			}
			
			bbs.close();
			System.out.println("LINE: "+jsonLine);
			System.out.println("JSON: "+json.toString());*/
			
			TcpResponse resp = CommandHandler.getResponse(t, sessCtx, bbs);
			ByteArrayOutputStream os = PrettyJson.getJsonStream(resp, false);
			pCtx.writeAndFlush(Unpooled.copiedBuffer(os.toByteArray()));
			pCtx.close();
		}
		catch ( Exception e ) {
			vLog.error(e.getMessage(), e);
		}
	}

	/*--------------------------------------------------------------------------------------------*/
	@Override
	public void exceptionCaught(ChannelHandlerContext pCtx, Throwable pCause) {
		pCause.printStackTrace();
		pCtx.close();
	}

}