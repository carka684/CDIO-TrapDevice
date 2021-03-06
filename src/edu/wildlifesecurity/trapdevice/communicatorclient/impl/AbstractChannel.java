package edu.wildlifesecurity.trapdevice.communicatorclient.impl;

import java.util.Map;

import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ILogger;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.Message;
import edu.wildlifesecurity.framework.MessageEvent;

/**
 * Represents a communication channel. A channel could be for example sms or internet.
 * TODO: Handle lost connection
 */
public abstract class AbstractChannel {
	
	protected Map<String, Object> configuration;
	protected ILogger log;
	
	protected AbstractChannel(Map<String, Object> config, ILogger logger){
		this.configuration = config;
		this.log = logger;
	}
	
	/**
	 * Start try to connect to server
	 */
	abstract void connect();
	
	/**
	 * Adds support for receiving events when messages arrives from TrapDevices
	 */
	abstract ISubscription addEventHandler(EventType type, IEventHandler<MessageEvent> handler);
	
	/**
	 * Sends a string message through the channel to the TrapDevice that is contained in the Message instance.
	 */
	abstract void sendMessage(Message message);
	
	/**
	 * Disposes the channel
	 */
	abstract void dispose();

}