package com.relayrides.pushy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class ApnsClientInitializer<T extends ApnsPushNotification> extends ChannelInitializer<SocketChannel> {
	
	private final PushManager<T> pushManager;
	private final ApnsClientThread<T> clientThread;
	
	public ApnsClientInitializer(final PushManager<T> pushManager, final ApnsClientThread<T> clientThread) {
		this.pushManager = pushManager;
		this.clientThread = clientThread;
	}
	
	@Override
	protected void initChannel(final SocketChannel channel) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
		
		final ChannelPipeline pipeline = channel.pipeline();
		
		if (this.pushManager.getEnvironment().isTlsRequired()) {
			pipeline.addLast("ssl", SslHandlerFactory.getSslHandler(
					this.pushManager.getKeyStore(), this.pushManager.getKeyStorePassword()));
		}
		
		pipeline.addLast("decoder", new ApnsErrorDecoder());
		pipeline.addLast("encoder", new PushNotificationEncoder<T>());
		pipeline.addLast("handler", new ApnsErrorHandler<T>(this.pushManager, this.clientThread));
	}
}
