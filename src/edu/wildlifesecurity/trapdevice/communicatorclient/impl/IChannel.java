package edu.wildlifesecurity.trapdevice.communicatorclient.impl;

import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.Message;
import edu.wildlifesecurity.framework.MessageEvent;

/**
 * Represents a communication channel. A channel could be for example sms or internet.
 */
public interface IChannel {
	
	/**
	 * Starts to try to connect to server asynchronously
	 */
	void connect();
	
	/**
	 * Adds support for receiving events when messages arrives from TrapDevices
	 */
	ISubscription addEventHandler(EventType type, IEventHandler<MessageEvent> handler);
	
	/**
	 * Sends a string message through the channel to the TrapDevice that is contained in the Message instance.
	 */
	void sendMessage(Message message);

}
