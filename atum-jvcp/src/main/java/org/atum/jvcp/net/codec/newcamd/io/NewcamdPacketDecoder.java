package org.atum.jvcp.net.codec.newcamd.io;

import java.util.List;

import org.apache.log4j.Logger;
import org.atum.jvcp.CardServer;
import org.atum.jvcp.crypto.DESUtil;
import org.atum.jvcp.net.NetworkConstants;
import org.atum.jvcp.net.codec.NetUtils;
import org.atum.jvcp.net.codec.PacketState;
import org.atum.jvcp.net.codec.newcamd.NewcamdConstants;
import org.atum.jvcp.net.codec.newcamd.NewcamdPacket;
import org.atum.jvcp.net.codec.newcamd.NewcamdSession;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 22 Nov 2016 22:23:11
 */

public class NewcamdPacketDecoder extends ByteToMessageDecoder {

	private Logger logger = Logger.getLogger(NewcamdPacketDecoder.class);
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		NewcamdSession session = (NewcamdSession) ctx.channel().attr(NetworkConstants.CAM_SESSION).get();
		NewcamdPacket packet = parseBuffer(ctx, session, in);
		if(packet == null){
			return;
		}
		handlePacket(session, packet);
		packet.getPayload().release();
		packet.getHeaders().release();
	}

	private void handlePacket(NewcamdSession session, NewcamdPacket packet) {
		if(packet.isEcm()){
			decodeEcm(session, packet);
			return;
		}
		if(packet.isEmm()){
			decodeEmm(session, packet);
			return;
		}
		switch (packet.getCommand()) {
		case NewcamdConstants.MSG_CARD_DATA_REQ:
			logger.info("newcamd MSG_CARD_DATA_REQ decode: "+session.isReader());
			break;
		case NewcamdConstants.MSG_KEEPALIVE:
			handleKeepalive(session, packet);
			break;
		default:
			logger.info("unhandled packet: " + packet.getCommand() + " " + packet.getSize());
			// payload.readBytes(size);
			break;
		}
	}
	
	private void decodeEmm(NewcamdSession session, NewcamdPacket packet) {
		//logger.info("newcamd emm decode: "+session.isReader());
	}

	private void handleKeepalive(NewcamdSession session, NewcamdPacket packet) {
		//logger.info("newcamd keepalive decode: "+session.isReader());
	}

	private void decodeEcm(NewcamdSession session, NewcamdPacket packet) {
		if(packet.isDcw()){
			logger.info("newcamd ecm dcw decode: "+session.isReader());
			
			byte[] dcw = new byte[16];
			packet.getPayload().readBytes(dcw);
			
			logger.info("dcw dump: "+NetUtils.bytesToString(dcw,0,dcw.length));
			
			if(!CardServer.getInstance().handleEcmAnswer(session, session.getLastRequest().getCspHash(), dcw, -1, -1)){
				//answer was not handled. No entry existed in any cache.
			}
			return;
		}
		logger.info("newcamd ecm decode: "+session.isReader());
		byte[] ecm = new byte[packet.getSize()+3];
		ecm[0] = (byte) packet.getCommand();
		ecm[1] = (byte) (packet.getSize() >> 8);
		ecm[2] = (byte) (packet.getSize() & 0xFF);
		packet.getPayload().readBytes(ecm, 3, packet.getSize());
		
		//long cspHash = EcmRequest.computeEcmHash(ecm);
		CardServer.getInstance().handleClientEcmRequest(session, session.getCardId(), 0, 0, 0, ecm);
	}

	public static NewcamdPacket parseBuffer(ChannelHandlerContext ctx, NewcamdSession session, ByteBuf in) {
		PacketState state = ctx.channel().attr(NetworkConstants.PACKET_STATE).get();
		if (state == null)
			state = PacketState.HEADER;
		switch (state) {
		case HEADER:
			if (in.readableBytes() < 2) {
				return null;
			}
			int size = in.readShort();
			session.setCurrentPacket(-1, size, -1);

			if (in.readableBytes() < size) {
				ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.PAYLOAD);
				return null;
			}
		case PAYLOAD:

			if (in.readableBytes() < session.getPacketSize()) {
				return null;
			}
			ctx.channel().attr(NetworkConstants.PACKET_STATE).set(PacketState.HEADER);
			byte[] payload = new byte[session.getPacketSize()];
			in.readBytes(payload);

			ByteBuf decryptedPayload = DESUtil.desDecrypt(payload, session.getPacketSize(), session.getDesKey());
			if(decryptedPayload == null){
				//invalid DES key.
				return null;
			} 
			return parseDecryptedBuffer(decryptedPayload, decryptedPayload.capacity());
		}
		return null;
	}

	/**
	 * @param decryptedPayload
	 * @param packetSize
	 * @return
	 */
	private static NewcamdPacket parseDecryptedBuffer(ByteBuf decryptedPayload, int packetSize) {
		ByteBuf headers = decryptedPayload.readBytes(10);
		int commandCode = decryptedPayload.readByte() & 0xFF;
		NewcamdPacket packet = new NewcamdPacket(commandCode, headers);
		//int dataLength = decryptedPayload.readShort();
		int dataLength = (decryptedPayload.readByte() & 0x0F) * 256 + (decryptedPayload.readByte() & 0xFF);
		if(dataLength != 0){
			/*if(dataLength != (packetSize-14)){
				loggerA.warn("Invalid packet size: "+dataLength+" "+packetSize+" "+commandCode);
			}*/
			ByteBuf payload = decryptedPayload.readBytes(dataLength);
			packet.setPayload(payload);
		}
		decryptedPayload.release();
		return packet;
	}
}
