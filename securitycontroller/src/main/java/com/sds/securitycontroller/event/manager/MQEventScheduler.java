/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.event.manager;

//import java.awt.SystemTray;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.sds.securitycontroller.command.ICommandPushService;
import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
//import java.util.Date;
//import com.sds.securitycontroller.core.internal.Config;
//import com.sds.securitycontroller.knowledge.KnowledgeEntityAttribute;
//import com.sds.securitycontroller.knowledge.KnowledgeType;
//import com.sds.securitycontroller.event.*;
import com.sds.securitycontroller.log.manager.ILogManagementService;
import com.sds.securitycontroller.module.ISecurityControllerModuleContext;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;

public class MQEventScheduler extends EventManager /*,Serializable*/{
	private static String TOPIC_EXCHANGE_NAME = "topic_securitycontroller";
	private static String FANOUT_EXCHANGE_NAME = "fanout_securitycontroller";
//	private static String ampqHost = "controller.research.intra.sds.com";
	private static String ampqHost = "127.0.0.1";
	private static int ampqPort = 5672;
	private static String ampqUserName = "guest";
	private static String ampqPassword = "guest";
//	private static String ampqHost = "10.103.25.248";

	//private static final String RPC_EXCHANGE_NAME = "rpc_securitycontroller";
	private static final String TOPIC_HEADER = "EVENT_TYPE.";
	private  SecurityControllerModuleContext context=null;
    protected Logger logger = LoggerFactory.getLogger(EventManager.class);
    
	Channel channel = null;
	QueueingConsumer consumer = null;
	QueueingConsumer.Delivery delivery =null;
	protected Map<EventType, Set<IEventListener>> eventListeners;
	private String topicQueueName;
	private String fanoutQueueName;
	
	static DateFormat dateFormat= new SimpleDateFormat("HH:mm:ss.SSS");

	    
    public MQEventScheduler(){
    	
    	logger.info("MQ constructing...");
    	ConnectionFactory factory = new ConnectionFactory();
        
        //producer has 2 types of exchanges,topic for event message and fanout for subscription message
        //consumer is binded to both types of messages. Messages if sorted by Properties.
        
     // load properties
        GlobalConfig config = GlobalConfig.getInstance();

        TOPIC_EXCHANGE_NAME = config.topicExchangeName;
    	FANOUT_EXCHANGE_NAME = config.fanoutExchangeName;
    	ampqHost = config.ampqHost;
    	ampqPort = Integer.parseInt(config.ampqPort);
    	ampqUserName=config.ampqUserName;
    	ampqPassword=config.ampqPassword;
    	factory.setHost(ampqHost);
    	factory.setPort(ampqPort);
    	factory.setUsername(ampqUserName);
    	factory.setPassword(ampqPassword);
        
        try{
	        Connection connection = factory.newConnection();
	        channel = connection.createChannel();	
	        channel.exchangeDeclare(TOPIC_EXCHANGE_NAME, "topic");
	        channel.exchangeDeclare(FANOUT_EXCHANGE_NAME, "fanout");
	        consumer = new QueueingConsumer(channel);
	        topicQueueName = channel.queueDeclare().getQueue();
	        fanoutQueueName = channel.queueDeclare().getQueue();
	        channel.basicConsume(topicQueueName, true, consumer);
	        channel.basicConsume(fanoutQueueName, true, consumer);
			channel.queueBind(fanoutQueueName, FANOUT_EXCHANGE_NAME,"");
			
			//start RPC listener
			logger.info("initilizing RPC listener...");
			new Thread(new RPCListenerThread()).start();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        this.eventListeners = new HashMap<EventType, Set<IEventListener>>();
    }
    

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
        Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        // We are the class that implements the service
        m.put(IEventManagerService.class, this);
        return m;
	}
	
    
	@Override
	public void addEventListener(EventType type, IEventListener listener) {
    	if(!this.eventListeners.containsKey(type)){
    		this.eventListeners.put(type, new HashSet<IEventListener>());
    		//PC_Chen
    		//add this type to routingKey to receive this type message
    		try {
				channel.queueBind(topicQueueName, TOPIC_EXCHANGE_NAME,"*."+type.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	this.eventListeners.get(type).add(listener);
    	logger.info("added listener '{}' for event type '{}'.",listener.toString(),type);
	}
	
	@Override
	public void removeEventListener(EventType type, IEventListener listener) {
    	if(this.eventListeners.containsKey(type)){
    		if(this.eventListeners.get(type).contains(listener))
    	    	this.eventListeners.get(type).remove(listener);
    		if(this.eventListeners.get(type).isEmpty())
    			this.eventListeners.remove(type);
    		//PC_Chen
    		//if no more listener interested in this type , unbind it from exchange
    		if(!this.eventListeners.containsKey(type)){
        		try {
    				channel.queueUnbind(topicQueueName, TOPIC_EXCHANGE_NAME,"*."+type.toString());
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		
    	}
	}
	
	public byte[] objTobytes(Object obj) throws IOException{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(obj);
		return byteStream.toByteArray();
	}
	
	public Object bytesToObj(byte[] bytes) throws IOException, ClassNotFoundException{
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		ObjectInputStream objStream = new ObjectInputStream(byteStream);
		return objStream.readObject();
	}
	
	@Override
	public void addConditionToListener(EventSubscriptionInfo condition){
		BasicProperties props=new BasicProperties.Builder().contentType("SUBSCRIPTION_INFO").build();
		try {
			channel.basicPublish(FANOUT_EXCHANGE_NAME, "", props,objTobytes(condition));
		} catch (IOException e1) {
			e1.printStackTrace();
			logger.error(e1.getMessage());
		}
	}

	@Override
	public void addEvent(Event e) {
		// PC_Chen
		//adding an event means publishing a message with routingKey=type to exchange
		BasicProperties props=new BasicProperties.Builder().contentType("EVENT").build();
		try {
			channel.basicPublish(TOPIC_EXCHANGE_NAME, TOPIC_HEADER+e.type.toString(), props,objTobytes(e));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void addBroadcastEvent(Event e) {
		// PC_Chen
		//adding a broadcast event == publish an event to a fanout exchange so that every distributed node can receive it.
		BasicProperties props=new BasicProperties.Builder().contentType("BROADCAST_EVENT").build();
		try {
			channel.basicPublish(FANOUT_EXCHANGE_NAME,TOPIC_HEADER+e.type.toString(),props,objTobytes(e));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	@Override
	public void initBuiltinListeners(ISecurityControllerModuleContext moduleContext) {
    	
        ICommandPushService commandPusher = moduleContext.getServiceImpl(ICommandPushService.class);
        if(commandPusher instanceof IEventListener)
        	addEventListener(EventType.PUSH_FLOW, (IEventListener)commandPusher);
        else
        	logger.error("{} does not implements IEventListener interface.",commandPusher);
        
        ILogManagementService loggerManager = moduleContext.getServiceImpl(ILogManagementService.class);
        if(loggerManager == null)
        	logger.error("loggerManager is not enabled");
        else if(!(loggerManager instanceof IEventListener))
        	logger.error(loggerManager.toString()+" does not implements IEventListener interface.");
        else 
        	addEventListener(EventType.RECEIVED_LOG,(IEventListener)loggerManager);
	}

	@Override
	public void start() {
		logger.info("MQ eventmanager starting...");
		while(true){
			try {
				delivery=consumer.nextDelivery();
				byte[] eventBytes = delivery.getBody();
				BasicProperties props=delivery.getProperties();
				String messageType=props.getContentType();

				if(messageType.equals("EVENT") || messageType.equals("BROADCAST_EVENT") ){
					Event event=(Event) bytesToObj(eventBytes);
					processEvent(event);
				}else if(messageType.equals("SUBSCRIPTION_INFO")){
	        		EventSubscriptionInfo subinfo=(EventSubscriptionInfo)bytesToObj(eventBytes);
	        		Set<IEventListener> subscberSet = eventListeners.get(EventType.ADD_SUBSCRIPTION);
	        		if(subscberSet!=null){
	        			
	        			logger.info(" DISPATCHING SUB ADD EVENT, eventListener count is: {}",(eventListeners.get(EventType.ADD_SUBSCRIPTION)).size());
		        		for( IEventListener listener:eventListeners.get(EventType.ADD_SUBSCRIPTION)){
		        			
		        			logger.info(" SUBSCRIPTION ADDING DISPATCHED TO LISTENER '{}'",listener.toString());
		        			listener.processAddListenEventCondition(subinfo.getEventype(), subinfo);
		        		}
	        		}
	        		else {
						logger.info(" Not to handle with subscription adding. ");
					}
		        	
				}
			} catch (IOException | ShutdownSignalException | ConsumerCancelledException | InterruptedException | ClassNotFoundException e1) {
				logger.error(e1.getMessage());
				e1.printStackTrace();
			} 
		}
	}
	private void processEvent(Event e) {
    	EventType type = e.type;
    	if(!this.eventListeners.containsKey(type))
    		return;
//    	Date now=new Date();
//    	long delay=now.getTime()-e.args.getTime().getTime();
    	Set<IEventListener> listeners = this.eventListeners.get(type);
    	if(type==EventType.RECEIVED_POLICY){
    		logger.debug("received policy! listener:"+eventListeners.get(type));
    	}
    	for(IEventListener listener : listeners){
//        	logger.info("Dispatching a {} event {} ms after its generation, to {}.",e.type.toString(),delay,listener.getClass().toString());
        	listener.processEvent(e);
    	}
	}
	
	@Override
	public void accessContext(SecurityControllerModuleContext context){
		this.context=context;
	}
	
	String RPCRequestQueueName = "rpc_queue";
//	String RPCReplyQueueName;
	Object rpcCallResultObject=new Object();//null;
	byte[] lock = new byte[0];
	boolean rpcCallOver = false;
	
	
	@Override
	public Object makeRPCCall(Class<?> serviceClass,String methodName,Object[] Args){

        rpcCallOver = false;
        rpcCallResultObject = null;
		//transform args
		Object[] args = null;
		try {
			if(Args==null || Args.length==0){
				logger.error("illegal args!");
				return null;
			}
			args = new Object[Args.length+2];
			args[0] = serviceClass;
			args[1] = methodName;
			for(int i=2;i<args.length;i++){
				args[i] = Args[i-2];
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		while(!rpcCallOver){
			try {
		        rpcCallOver = false;
		        Thread callThread = new Thread(new RPCClientThread(args,this));
		        callThread.start();
		        logger.info(" [RPC CLIENT] Making RPC request to service [{}], method name:[{}], args: {}",serviceClass,methodName,args);
		        logger.info(" [RPC CLIENT] waiting for response...");
				callThread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info(" [RPC CLIENT] received response.");
		return rpcCallResultObject;
	}
	
	class RPCClientThread implements Runnable{
		Object[] args;
		MQEventScheduler scheduler;
		public RPCClientThread(Object[] args,MQEventScheduler scheduler){
			this.args=args;
			this.scheduler = scheduler;
		}
		@Override
		public void run() {
//			System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[[[[RPC THREAD CALLING!!!!!!!!!!!!!!");
			rpcCallResultObject = RPCCall(args);
			return;
//			rpcCallResultObject.notify();
		}
		
		Object RPCCall(Object[] args) {
			// Make RPC Call using MQ 
			Connection connection = null;  
		    Channel channel= null;  
		    String requestQueueName = RPCRequestQueueName;  
		    String replyQueueName;  
		    QueueingConsumer consumer;  
	        Object response = null;
		    try{
		    	// establish RPC Client
		    	logger.info("establishing RPC connection...");
		    	ConnectionFactory factory = new ConnectionFactory();  
		        factory.setHost(ampqHost); 
		        factory.setPort(ampqPort);
		    	factory.setUsername(ampqUserName);
		    	factory.setPassword(ampqPassword);
		        connection = factory.newConnection();  
		        channel = connection.createChannel();  
		        replyQueueName = channel.queueDeclare().getQueue();  
		        consumer = new QueueingConsumer(channel);  
		        channel.basicConsume(replyQueueName, true, consumer);
		    }
		    catch (Exception e){
		    	//close connection & return
	    		logger.error(e.getLocalizedMessage());
		    	e.printStackTrace();

		        rpcCallOver = true;
		    	return null;
		    }
		    try{
		        // make the RPC call
		        String corrId = UUID.randomUUID().toString();  
//		        BasicProperties props = new BasicProperties();  
//		        props.setReplyTo(replyQueueName);  
//		        props.setCorrelationId(corrId);  
			    BasicProperties props = new BasicProperties
	                    .Builder()
                		.replyTo(replyQueueName)
	                    .correlationId(corrId)
	                    .build();
		        
		        byte[] reqBytes = objTobytes(args); 
		        channel.basicPublish("", requestQueueName, props, reqBytes);  
		        //wait for response
		        while (true) {  
		            QueueingConsumer.Delivery delivery = consumer.nextDelivery();  
		            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
		                response = bytesToObj( delivery.getBody() ); //new String(delivery.getBody(), "UTF-8");  
		                break;  
		            }
		        }
//		        rpcCallResultObject = response;
		        // close connection
		        connection.close();
		        rpcCallOver = true;
		        return response;
		    }
		    catch (Exception e){
		    	logger.error(e.getLocalizedMessage());
		    	e.printStackTrace();

		        rpcCallOver = true;
		    	return null;
		    }
		}
		
	}
	
	
	class RPCListenerThread implements Runnable{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run() {
			final String RPC_QUEUE_NAME = RPCRequestQueueName;
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(ampqHost); 
	        factory.setPort(ampqPort);
	    	factory.setUsername(ampqUserName);
	    	factory.setPassword(ampqPassword);
			Connection connection;
			QueueingConsumer consumer = null;
			Channel channel = null;
			try {
				logger.info("[RPC REQUEST LISTENER] establishing connection...");
				connection = factory.newConnection();
				channel = connection.createChannel();
				channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
				channel.basicQos(1);
				consumer = new QueueingConsumer(channel);
				channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
				//connection = factory.newConnection();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
			logger.info("[RPC REQUEST LISTENER] waiting for RPC request...");

			while (true) {
			    QueueingConsumer.Delivery delivery;
				try {
					delivery = consumer.nextDelivery();
					BasicProperties props = delivery.getProperties();

//	                BasicProperties replyProps = new BasicProperties();  
//	                replyProps.setCorrelationId(props.getCorrelationId()); 
					
				    BasicProperties replyProps = new BasicProperties
				                                     .Builder()
				                                     .correlationId(props.getCorrelationId())
				                                     .build();
				    
	                logger.info("[RPC REQUEST LISTENER] received a request, start parsing");
				    Object request = null;
				    try {
				    	request = bytesToObj( delivery.getBody());
					} catch (Exception e) {
						logger.error("received invalid data ");
						continue;
					}
				    Object[] oriArgs = (Object[])request;
				    Object output = "null";
				    if(oriArgs.length<2){
				    	logger.error("illegal args length");
						continue;
				    }
				    Object[] args = new Object[oriArgs.length-2];
					//check args format : args[0] = serviceName, args[1] = methodName
				    Class serviceClass = null;
				    String methodName = "";
				    try{
				    	serviceClass = (Class)oriArgs[0];
				    	methodName =  ((String)oriArgs[1]).toLowerCase();
				    	for(int i=0;i<args.length;i++){
				    		args[i] = oriArgs[i+2];
				    	}
				    	logger.info("[RPC REQUEST LISTENER] received a [{}] request ",methodName);
					    
					    
					    IRPCHandler rpcHandler = (IRPCHandler) context.getServiceImpl(serviceClass);
					    if(rpcHandler == null){
					    	logger.warn("rpcHandler "+serviceClass +" null, skip call");
					    }
					    else					    	
					    	output = rpcHandler.handleRPCRequest(methodName, args);
				    }
				    catch (Exception e){
				    	logger.error("invalid args");
						e.printStackTrace();
						continue;
					}
				    try {
						byte[] bytes = objTobytes(output);
						channel.basicPublish("", props.getReplyTo(), replyProps, bytes);
					    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		
		byte[] objTobytes(Object obj) throws IOException{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
			objStream.writeObject(obj);
			return byteStream.toByteArray();
		}
		
	    Object bytesToObj(byte[] bytes) throws IOException, ClassNotFoundException{
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			ObjectInputStream objStream = new ObjectInputStream(byteStream);
			return objStream.readObject();
		}
	}



	public static void main(String[] args){
		 // load properties
        try {
        	Properties prop= new Properties();
        	InputStream in = new BufferedInputStream (new FileInputStream("config/rabbitmq.properties"));
            prop.load(in);
            System.out.println(prop);
        	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}	
}
