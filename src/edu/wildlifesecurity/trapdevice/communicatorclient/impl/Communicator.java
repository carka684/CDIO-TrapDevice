package edu.wildlifesecurity.trapdevice.communicatorclient.impl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.util.Map;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import edu.wildlifesecurity.framework.AbstractComponent;
import edu.wildlifesecurity.framework.EventDispatcher;
import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ILogger;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.Message;
import edu.wildlifesecurity.framework.MessageEvent;
import edu.wildlifesecurity.framework.communicatorclient.ConnectEvent;
import edu.wildlifesecurity.framework.communicatorclient.ICommunicatorClient;

public class Communicator extends AbstractComponent implements
		ICommunicatorClient {
	
	private AbstractChannel channel;
	private boolean isConnected = false; 
	private EventDispatcher<ConnectEvent> connectionEventDispatcher = new EventDispatcher<ConnectEvent>();
	
	@Override
	public void init(){
		
		try {
			
			// Read from android configuration which channel to use
			Class<?> cl = Class.forName(configuration.get("CommunicatorClient_Channel").toString());
			Constructor<?> cons = cl.getConstructor(Map.class, ILogger.class);
			channel = (AbstractChannel) cons.newInstance(configuration, log);
		
			// Start try to connect to server
			channel.connect();
			
			// Start listen for successful connection to server
			channel.addEventHandler(MessageEvent.getEventType(Message.Commands.HANDSHAKE_ACK), new IEventHandler<MessageEvent>(){
				@SuppressWarnings("unchecked")
				@Override
				public void handle(MessageEvent event) {
					// Connected to server! 

					try{
						// Parse configuration
						String configEncoded = event.getMessage().getMessage().split(",")[1];
						byte[] bytes = Base64.decode(configEncoded, Base64.DEFAULT);
						ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
						ObjectInput ois = new ObjectInputStream(bis);
						Map<String,Object> config = (Map<String,Object>) ois.readObject();
						
						loadConfiguration(config);
						
						isConnected = true;
						connectionEventDispatcher.dispatch(new ConnectEvent(ConnectEvent.CONNECTED, true));
						
					}catch(Exception ex){
						ex.printStackTrace();
						log.error("Error in CommunicatorClient. Couldn't serialize configuration: " + ex.getMessage());
					}
				}
			});
			
		} catch (Exception e) {
			log.error("Unexpected error in CommunicatorClient: " + e.getMessage());
		}
	}
	
	@Override
	public ISubscription addEventHandler(EventType type, IEventHandler<MessageEvent> handler) {
		return channel.addEventHandler(type, handler);
	}
	
	@Override
	public ISubscription addConnectionEventHandler(EventType type, IEventHandler<ConnectEvent> handler){
		return connectionEventDispatcher.addEventHandler(type, handler);
	}

	@Override
	public void sendMessage(Message message) {
		channel.sendMessage(message);
	}
	
	@Override
	public void info(String message) {
		sendLogMessage("INFO", message);
	}

	@Override
	public void warn(String message) {
		sendLogMessage("WARN", message);
	}

	@Override
	public void error(String message) {
		sendLogMessage("ERROR", message);
	}
	
	private void sendLogMessage(String prio, String message){
		sendMessage(new Message(0, Message.Commands.LOG + "," + prio + "," + message));
	}
	
	@Override
	public boolean isConnected(){
		return isConnected;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		return configuration;
	}

	@Override
	public void dispose(){
		if(channel != null)
			channel.dispose();
	}
	
}
