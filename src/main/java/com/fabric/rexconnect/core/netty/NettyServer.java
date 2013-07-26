package com.fabric.rexconnect.core.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/*================================================================================================*/
public class NettyServer implements Runnable {
	
	private static final Logger vLog = Logger.getLogger(NettyServer.class);
	
	private int vPort;


	////////////////////////////////////////////////////////////////////////////////////////////////
	/*--------------------------------------------------------------------------------------------*/
	public NettyServer(int pPort) {
		vLog.setLevel(Level.ALL);
		vPort = pPort;
	}

	/*--------------------------------------------------------------------------------------------*/
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ChannelInitializer<SocketChannel> handler = new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel pChan) throws Exception {
					pChan.pipeline()
						.addLast(new DelimiterBasedFrameDecoder(
							Integer.MAX_VALUE/2, Delimiters.nulDelimiter())
						)
						.addLast(new NettyRequestHandler());
				}
			};
			
			ServerBootstrap b = new ServerBootstrap()
				.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 100)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(handler);

			ChannelFuture f = b.bind(vPort).sync();
			f.channel().closeFuture().sync();
		}
		catch ( Exception e ) {
			vLog.fatal(e.getMessage(), e);
		}
		finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}