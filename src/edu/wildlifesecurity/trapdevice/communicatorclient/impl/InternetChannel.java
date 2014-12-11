package edu.wildlifesecurity.trapdevice.communicatorclient.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.Toast;
import edu.wildlifesecurity.framework.EventDispatcher;
import edu.wildlifesecurity.framework.EventType;
import edu.wildlifesecurity.framework.IEventHandler;
import edu.wildlifesecurity.framework.ILogger;
import edu.wildlifesecurity.framework.ISubscription;
import edu.wildlifesecurity.framework.Message;
import edu.wildlifesecurity.framework.MessageEvent;

/**
 * Connects this trap device with the server using the internet
 * @author Tobias
 *
 */
public class InternetChannel extends AbstractChannel {

	public InternetChannel(Map<String, Object> config, ILogger logger) {
		super(config, logger);
	}

	private EventDispatcher<MessageEvent> eventDispatcher = new EventDispatcher<MessageEvent>();
	
	private Thread connectingThread;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	@Override
	public void connect() {
		
		// Do the connecting in another thread
		connectingThread = new Thread(new Runnable(){

			@Override
			public void run() {

				Pattern pattern = Pattern.compile("([^:^/]*):(\\d*)?(.*)?");
				Matcher matcher = pattern.matcher(configuration.get("CommunicatorClient_ServerIdentifier").toString());
				matcher.find();
				
				// Try to connect until success
				while(socket == null){
				
					try {
						
						// Try connect
						socket = new Socket(matcher.group(1), Integer.parseInt(matcher.group(2)));
						
					} catch (IOException e) {
						try {
							if(connectingThread.isInterrupted())
								return;
							
							// If it failed, sleep for a while and try again
							log.warn("Connection failed, sleeps for 20 sec and tries again...");
							Thread.sleep(20000);
							
						} catch (InterruptedException e1) {
							// Do nothing
							return;
						}
					} catch (IllegalStateException e){
						log.error("Connection failed, the ip address needs to be of the format x.x.x.x:xxxx");
						return;
					}
				}
				
				try{
					
					// Connection established!
					System.out.println("Server connection established!");

					writer = new PrintWriter(socket.getOutputStream(), true);
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					// Send HANDSHAKE_REQ
					sendMessage(new Message(0, Message.Commands.HANDSHAKE_REQ.toString()));
					
					String message;
					while ((message = reader.readLine()) != null) {
						
						// New message arrived, tell listeners!
						eventDispatcher.dispatch(
			            		new MessageEvent(MessageEvent.getEventType(message.split(",")[0]), 
			            				new Message(message, 0)));
					}
					
				}catch (IOException ex){
					log.error("Error in InternetChannel: " + ex.getMessage());
				}	
				
			}
			
		});
		
		connectingThread.start();
	}

	@Override
	public ISubscription addEventHandler(EventType type,
			IEventHandler<MessageEvent> handler) {
		return eventDispatcher.addEventHandler(type, handler);
	}

	@Override
	public void sendMessage(Message message) {
		writer.write(message.getMessage() + "\n");
		writer.flush();
	}
	
	@Override
	public void dispose(){
		if(socket != null && (socket.isBound() || socket.isConnected()))
			try {
				socket.close();
			} catch (IOException e) {
				// Error when closing the socket. Do nothing
			}
		connectingThread.interrupt();
	}

}
