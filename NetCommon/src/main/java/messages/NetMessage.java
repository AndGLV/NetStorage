package messages;

import messages.enums.NetMessageType;

import java.io.Serializable;

public class NetMessage implements Serializable {
	private NetMessageType messageType;
	private String msg;
	private Object objMsg;

	public NetMessage(NetMessageType messageType, String msg, Object objMsg) {
		this.messageType = messageType;
		this.msg = msg;
		this.objMsg = objMsg;
	}

	public NetMessage(NetMessageType messageType, Object objMsg) {
		this.messageType = messageType;
		this.objMsg = objMsg;
		this.msg = null;
	}

	public NetMessage(NetMessageType messageType) {
		this.messageType = messageType;
		this.msg = null;
		this.objMsg = null;
	}

	public NetMessageType getMessageType() {
		return messageType;
	}

	public String getMsg() {
		return msg;
	}

	public Object getObjMsg() {
		return objMsg;
	}

	public void setMessageType(NetMessageType messageType) {
		this.messageType = messageType;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setObjMsg(Object objMsg) {
		this.objMsg = objMsg;
	}
}
