package edu.wildlifesecurity.trapdevice.communicatorclient.impl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Map;

import android.util.Base64;
import edu.wildlifesecurity.framework.AbstractComponent;
import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.Message;
import edu.wildlifesecurity.framework.MessageEvent;
import edu.wildlifesecurity.framework.communicatorclient.ICommunicatorClient;

public class Communicator extends AbstractComponent implements
		ICommunicatorClient {
	
	private IChannel channel;

	@Override
	public void init(){
		
		try {
			
			// Read from configuration which channels to use
			channel = (IChannel) Class.forName(configuration.get("CommunicatorServer_Channel").toString()).newInstance();
		
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
						
					}catch(Exception ex){
						ex.printStackTrace();
						log.error("Error in CommunicatorClient. Couldn't serialize configuration: " + ex.getMessage());
					}
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ISubscription addEventHandler(EventType type, IEventHandler<MessageEvent> handler) {
		return channel.addEventHandler(type, handler);
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

}
