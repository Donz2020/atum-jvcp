/**
 * 
 */
package org.atum.jvcp.net.codec.newcamd;

import org.atum.jvcp.NewcamdServer;
import org.atum.jvcp.model.CamProtocol;
import org.atum.jvcp.model.CamSession;

import io.netty.channel.ChannelHandlerContext;


/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 13 Dec 2016 23:43:11
 */
public class NewcamdSession extends CamSession {

	private byte[] desKey;
	private byte[] firstDesKey;
	private int cardId;
	private NewcamdServer server;
	
	public NewcamdSession(NewcamdServer server, ChannelHandlerContext context, byte[] desKey){
		super(context, CamProtocol.NEWCAMD);
		this.desKey = desKey;
		this.firstDesKey = desKey;
		this.server = server;
	}

	public byte[] getDesKey() {
		return desKey;
	}
	
	public byte[] getFirstDesKey() {
		return firstDesKey;
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

	/* (non-Javadoc)
	 * @see org.atum.jvcp.model.CamSession#hasCard(int)
	 */
	@Override
	public boolean hasCard(int cardId) {
		if(this.cardId == cardId)
			return true;
		return false;
	}

	@Override
	public void unregister() {
		server.unregister(this);
	}

}
