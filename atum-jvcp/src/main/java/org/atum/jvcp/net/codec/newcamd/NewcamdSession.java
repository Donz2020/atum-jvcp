/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd;

import org.atum.jvcp.model.CamSession;

import io.netty.channel.ChannelHandlerContext;


/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:43:11
 */
public class NewcamdSession extends CamSession {

	private byte[] desKey;
	private int cardId;
	
	public NewcamdSession(ChannelHandlerContext context, byte[] desKey){
		super(context);
		this.desKey = desKey;
	}

	public byte[] getDesKey() {
		return desKey;
	}

	public void setDesKey(byte[] desKey) {
		this.desKey = desKey;
	}

	public int getCardId() {
		return cardId;
	}
	
	public void setCardId(int cardId){
		this.cardId = cardId;
	}

}
