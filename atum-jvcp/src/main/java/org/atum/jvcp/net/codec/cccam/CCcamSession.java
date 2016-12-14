package org.atum.jvcp.net.codec.cccam;

import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;
import org.atum.jvcp.model.EcmRequest;
import org.atum.jvcp.net.CamSession;
import org.atum.jvcp.net.codec.cccam.io.CCcamPacketSender;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:16:25
 */

public class CCcamSession extends CamSession {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(CCcamSession.class);
	
	private ChannelHandlerContext context;
	private CCcamCipher encrypter;
	private CCcamCipher decrypter;

	private String username;
	private CCcamPacketSender packetSender;

	private long lastPing = System.currentTimeMillis();

	private int packetCommandCode;
	private int packetSize;
	
	private EcmRequest lastRequest = null;
	
	public CCcamSession(ChannelHandlerContext context, CCcamCipher encrypter, CCcamCipher decrypter) {
		this.context = context;
		this.encrypter = encrypter;
		this.decrypter = decrypter;
	}

	public CCcamCipher getDecrypter() {
		return decrypter;
	}
	
	public CCcamCipher getEncrypter() {
		return encrypter;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
	
	public void write(CCcamPacket cCcamPacket){
		context.channel().writeAndFlush(cCcamPacket);
	}
	

	public ChannelHandlerContext getCtx() {
		return context;
	}

	public void setPacketSender(CCcamPacketSender packetSender) {
		this.packetSender = packetSender;
	}
	

	public CCcamPacketSender getPacketSender() {
		return this.packetSender;
	}
	
	public long getLastKeepalive(){
		return System.currentTimeMillis() - lastPing ;
	}
	
	public void keepAlive(){
		packetSender.writeKeepAlive();
		lastPing = System.currentTimeMillis();
	}

	public void setCurrentPacket(int cmdCode, int size) {
		this.packetCommandCode = cmdCode;
		this.packetSize = size;
	}
	
	public int getPacketCode(){
		return packetCommandCode;
	}
	
	public int getPacketSize(){
		return packetSize;
	}

	public void setLastKeepAlive(long currentTimeMillis) {
		lastPing = currentTimeMillis;
	}

	public boolean isReader() {
		return false;
	}

	public EcmRequest getLastRequest() {
		return lastRequest;
	}

	public void setLastRequest(EcmRequest lastRequest) {
		this.lastRequest = lastRequest;
	}
}
