/** 
*    Copyright 2014 BUPT. 
**/ 
/**
 *    Copyright 2011, Big Switch Networks, Inc.
 *    Originally created by David Erickson, Stanford University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package com.sds.securitycontroller.core.internal;

//import java.io.File;
//import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.core.IDeviceMessageListener;
import com.sds.securitycontroller.core.IInfoProvider;
import com.sds.securitycontroller.core.ISecurityControllerProviderService;
import com.sds.securitycontroller.core.message.BasicFactory;
import com.sds.securitycontroller.core.message.DeviceMessage;
import com.sds.securitycontroller.core.message.MessageParseException;
import com.sds.securitycontroller.core.web.CoreWebRoutable;
import com.sds.securitycontroller.device.Device;
import com.sds.securitycontroller.device.DeviceFactory.DeviceCategory;
import com.sds.securitycontroller.device.DeviceFactory.DeviceType;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.protocol.DeviceMessageType;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.threadpool.IThreadPoolService;
import com.sds.securitycontroller.utils.CmdLineSettings;
import com.sds.securitycontroller.utils.LoadMonitor;

/**
 * The main controller class.  Handles all setup and network listeners
 */
public class Controller implements ISecurityControllerProviderService /*,Serializable*/{

	protected static Logger log = LoggerFactory.getLogger(Controller.class);

    protected BasicFactory factory = new BasicFactory();
    protected List<String> deviceDescSortedList;
    protected IEventManagerService eventManager;


    // The controllerNodeIPsCache maps Controller IDs to their IP address.
    // It's only used by handleControllerNodeIPsChanged
    protected HashMap<String, String> controllerNodeIPsCache;

    
    // Module dependencies
    protected IRestApiService restApi;
    protected IThreadPoolService threadPool;

    // Configuration options
    protected int commPort = 7777;
    protected int workerThreads = 0;
    // The id for this controller node. Should be unique for each controller
    // node in a controller cluster.
    protected String controllerId = "localhost";
    


    // Start time of the controller
    protected long systemStartTime;
    // Storage table names
    protected static final String CONTROLLER_TABLE_NAME = "controller_controller";
    protected static final String CONTROLLER_ID = "id";

    protected static final String CONTROLLER_INTERFACE_TABLE_NAME = "controller_controllerinterface";
    protected static final String CONTROLLER_INTERFACE_ID = "id";
    protected static final String CONTROLLER_INTERFACE_CONTROLLER_ID = "controller_id";
    protected static final String CONTROLLER_INTERFACE_TYPE = "type";
    protected static final String CONTROLLER_INTERFACE_NUMBER = "number";
    protected static final String CONTROLLER_INTERFACE_DISCOVERED_IP = "discovered_ip";

    // Perf. related configuration
    protected static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;
    public static final int BATCH_MAX_SIZE = 100;
    protected static final boolean ALWAYS_DECODE_ETH = true;

    // Load monitor for overload protection
    protected final boolean overload_drop =
        Boolean.parseBoolean(System.getProperty("overload_drop", "false"));
    protected final LoadMonitor loadmonitor = new LoadMonitor(log);

    protected Map<String, List<IInfoProvider>> providerMap;
   
    
    // ***************
    // Getters/Setters
    // ***************

    public void setRestApiService(IRestApiService restApi) {
        this.restApi = restApi;
    }

    public void setThreadPoolService(IThreadPoolService tp) {
        this.threadPool = tp;
    }

	@Override
	public void setScheduler(IEventManagerService scheduler) {
		this.eventManager = scheduler;
	}
	
    @Override
	public synchronized void terminate() {
        log.info("Calling System.exit");
        System.exit(1);
    }


    @Override
    public String getControllerId() {
        return controllerId;
    }

    // **************
    // Initialization
    // **************


    /**
     * Sets the initial role based on properties in the config params.
     * It looks for two different properties.
     * If the "role" property is specified then the value should be
     * either "EQUAL", "MASTER", or "SLAVE" and the role of the
     * controller is set to the specified value. If the "role" property
     * is not specified then it looks next for the "role.path" property.
     * In this case the value should be the path to a property file in
     * the file system that contains a property called "SecurityController.role"
     * which can be one of the values listed above for the "role" property.
     * The idea behind the "role.path" mechanism is that you have some
     * separate heartbeat and master controller election algorithm that
     * determines the role of the controller. When a role transition happens,
     * it updates the current role in the file specified by the "role.path"
     * file. Then if SecurityController restarts for some reason it can get the
     * correct current role of the controller from the file.
     * @param configParams The config params for the SecurityControllerProvider service
     * @return A valid role if role information is specified in the
     *         config params, otherwise null
     */

    /**
     * Tell controller that we're ready to accept devices loop
     * @throws IOException
     */
    @Override
    public void run() {
    	if (log.isDebugEnabled()) {
			//logListeners();
		}

		try {
			final ServerBootstrap bootstrap = createServerBootStrap();

			bootstrap.setOption("reuseAddr", true);
			bootstrap.setOption("child.keepAlive", true);
			bootstrap.setOption("child.tcpNoDelay", true);
			bootstrap.setOption("child.sendBufferSize", Controller.SEND_BUFFER_SIZE);
			//
			//            ChannelPipelineFactory pfact =
			//                    new OpenflowPipelineFactory(this, null);
			//            bootstrap.setPipelineFactory(pfact);
			final Timer timer = new HashedWheelTimer();
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					return Channels.pipeline(
							new FrameDecoder(){

								@Override
								protected Object decode(ChannelHandlerContext arg0,
										Channel arg1, ChannelBuffer arg2)
												throws Exception {
									byte[] b = new byte[arg2.capacity()];
									arg2.readBytes(b);
									if((new String(b)).startsWith("ECHO"))
										return arg2;
									else{
										log.info(new String(b));
										return null;
									}
								}

							},
							new SimpleChannelUpstreamHandler(){
								@Override
								public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
									log.warn(
											"Unexpected exception from downstream.{}",
											e.getCause());
									e.getChannel().close();
								}
								@Override
								public void channelConnected(
										ChannelHandlerContext ctx, ChannelStateEvent e) {
									// Send the first message.  Server will not send anything here
									// because the firstMessage's capacity is 0.
									log.info("______Security Device connected.");
								}
								@Override
								public void messageReceived(
										ChannelHandlerContext ctx, MessageEvent e) {
									// Send back the received message to the remote peer.
									log.debug("_________"+e.getMessage().toString());
									final ChannelBuffer firstMessage = ChannelBuffers.buffer("ECHO REPLY".length());
									for (int i : "ECHO REPLY".getBytes()) {
										firstMessage.writeByte((byte) i);
									}
									final Channel c = e.getChannel();
									timer.newTimeout( new TimerTask() {
										@Override
										public void run(Timeout timeout) throws Exception {
											log.debug("Sending reply .... " );
											c.write(firstMessage);
										}
									}, 4, TimeUnit.SECONDS);
								}
							});
				}
			});
			InetSocketAddress sa = new InetSocketAddress(commPort);
			final ChannelGroup cg = new DefaultChannelGroup();
			cg.add(bootstrap.bind(sa));

			log.info("Listening for device connections on {}", sa);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// main loop
		this.eventManager.start();
		this.eventManager.setStartedStatus(true);
		
	}

	private ServerBootstrap createServerBootStrap() {
		if (workerThreads == 0) {
			return new ServerBootstrap(
					new NioServerSocketChannelFactory(
							Executors.newCachedThreadPool(),
							Executors.newCachedThreadPool()));
		} else {
			return new ServerBootstrap(
					new NioServerSocketChannelFactory(
							Executors.newCachedThreadPool(),
							Executors.newCachedThreadPool(), workerThreads));
		}
	}

	public void setConfigParams(Map<String, String> configParams) {
		String scPort = configParams.get("scport");
		if (scPort != null) {
			this.commPort = Integer.parseInt(scPort);
		}
		log.debug("Security controller port set to {}", this.commPort);
		String threads = configParams.get("workerthreads");
		if (threads != null) {
			this.workerThreads = Integer.parseInt(threads);
		}
		log.debug("Number of worker threads set to {}", this.workerThreads);
		String controllerId = configParams.get("controllerid");
		if (controllerId != null) {
			this.controllerId = controllerId;
		}
		log.debug("ControllerId set to {}", this.controllerId);
	}


	/**
	 * Initialize internal data structures
	 */
	public void init(Map<String, String> configParams) {
		// These data structures are initialized here because other
		// module's startUp() might be called before ours
		this.providerMap = new HashMap<String, List<IInfoProvider>>();
		setConfigParams(configParams);
		this.systemStartTime = System.currentTimeMillis();
		Properties prop = new Properties();  
		InputStream is = this.getClass().getClassLoader().
				getResourceAsStream(CmdLineSettings.DEFAULT_CONFIG_FILE);  

		try {
			prop.load(is);
		} catch (Exception e) {
			log.error("Could not load module configuration file", e);
			e.printStackTrace();
			System.exit(1);
		}
		//        commPort= Integer.parseInt(prop.getProperty("com.sds.securitycontroller.core.internal.Controller.openflowport"));
	}

	/**
	 * Startup all of the controller's components
	 */
//	@LogMessageDoc(message="Waiting for storage source",
//			explanation="The system database is not yet ready",
//			recommendation="If this message persists, this indicates " +
//					"that the system database has failed to start. " +
//					LogMessageDoc.CHECK_CONTROLLER)
	public void startupComponents() {
		// Create the table names we use


		// Startup load monitoring
		if (overload_drop) {
			this.loadmonitor.startMonitoring(
					this.threadPool.getScheduledExecutor());
		}

		// Add our REST API
		restApi.addRestletRoutable(new CoreWebRoutable());
	}


	@Override
	public long getSystemStartTime() {
		return (this.systemStartTime);
	}


	@Override
	public Map<DeviceMessageType, List<IDeviceMessageListener>> getListeners() {
		return null;
	}





	@Override
	public void addInfoProvider(String type, IInfoProvider provider) {
		if (!providerMap.containsKey(type)) {
			providerMap.put(type, new ArrayList<IInfoProvider>());
		}
		providerMap.get(type).add(provider);
	}

	@Override
	public void removeInfoProvider(String type, IInfoProvider provider) {
		if (!providerMap.containsKey(type)) {
			log.debug("Provider type {} doesn't exist.", type);
			return;
		}

		providerMap.get(type).remove(provider);
	}


	@Override
	public Map<String, Object> getControllerInfo(String type) {
		if (!providerMap.containsKey(type)) return null;

		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (IInfoProvider provider : providerMap.get(type)) {
			result.putAll(provider.getInfo(type));
		}

		return result;
	}


	protected ChannelUpstreamHandler getChannelHandler(MessageChannelState state) {
		return new CommunicationChannelHandler(state);
	}


	protected class CommunicationChannelHandler
	extends IdleStateAwareChannelUpstreamHandler {
		protected Device sd;
		protected Channel channel;
		protected MessageChannelState state;
		protected DeviceCategory category;
		protected String name;
		protected DeviceType type;
		protected String id;
		protected String ipaddr;

		public CommunicationChannelHandler(MessageChannelState state) {
			this.state = state;
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			channel = e.getChannel();
			log.info("New device connection from {}",
					channel.getRemoteAddress());
			sendMessage(DeviceMessageType.HELLO);
		}

		@Override
		public void channelDisconnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			log.info("Disconnected device {}", sd);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			if (e.getCause() instanceof ReadTimeoutException) {
				// device timeout
				log.error("Disconnecting device {} due to read timeout", sd);
				ctx.getChannel().close();
			} else if (e.getCause() instanceof HandshakeTimeoutException) {
				log.error("Disconnecting device {}: failed to complete handshake",
						sd);
				ctx.getChannel().close();
			} else if (e.getCause() instanceof ClosedChannelException) {
			} else if (e.getCause() instanceof IOException) {
				log.error("Disconnecting device {} due to IO Error: {}",
						sd, e.getCause().getMessage());
				ctx.getChannel().close();
			} else if (e.getCause() instanceof MessageParseException) {
				log.error("Disconnecting device " + sd +
						" due to message parse failure",
						e.getCause());
				ctx.getChannel().close();
			} else if (e.getCause() instanceof RejectedExecutionException) {
				log.warn("Could not process message: queue full");
			} else {
				log.error("Error while processing message from security device " + sd,
						e.getCause());
				ctx.getChannel().close();
			}
		}

		@Override
		public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
				throws Exception {
			sendMessage(DeviceMessageType.ECHO_REQUEST);
		}



		protected void addDeviceImpl() throws Exception {
			if (!state.hasGetConfigReply) {
				log.debug("Waiting for config reply from device {}",
						channel.getRemoteAddress());
				return;
			}
			if (!state.hasDescription) {
				log.debug("Waiting for description from device {}",
						channel.getRemoteAddress());
				return;
			}

			log.info("Device {} bound to class {}",
					sd.getId(), sd.getClass().getName());
			state.deviceBindingDone = true;
			state.hsState = MessageChannelState.HandshakeState.READY;
		}


		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			if (e.getMessage() instanceof List) {
				@SuppressWarnings("unchecked")
				List<DeviceMessage> msglist = (List<DeviceMessage>)e.getMessage();

				LoadMonitor.LoadLevel loadlevel;
				if (overload_drop) {
					loadlevel = loadmonitor.getLoadLevel();
				}
				else {
					loadlevel = LoadMonitor.LoadLevel.OK;
				}

				for (DeviceMessage msg : msglist) {
					try {
						if (overload_drop &&
								!loadlevel.equals(LoadMonitor.LoadLevel.OK)) {
							switch (msg.getType()) {
							default:
								break;
							}
						}

						// Do the actual packet processing
						processDeviceMessage(msg);

					}
					catch (Exception ex) {
						// We are the last handler in the stream, so run the
						// exception through the channel again by passing in
						// ctx.getChannel().
						Channels.fireExceptionCaught(ctx.getChannel(), ex);
					}
				}
			}
		}

		/**
		 * Send information to the device
		 * @throws IOException
		 */
		private void sendMessage(DeviceMessageType type) throws IOException {
			List<DeviceMessage> msglist = new ArrayList<DeviceMessage>(1);
			msglist.add(factory.getMessage(type));
			//        	List<String> msglist = new ArrayList<String>(1);
			//        	msglist.add("abc");
			channel.write(msglist);
		}

		/*
        private void sendMessage(DeviceMessage msg) throws IOException {
            List<DeviceMessage> msglist = new ArrayList<DeviceMessage>(1);
            msglist.add(msg);
            channel.write(msglist);
        }


        private void sendMessage(List<DeviceMessage> msglist) throws IOException {
            channel.write(msglist);
        }*/



		/**
		 * Dispatch an incoming message from a device to the appropriate
		 * handler.
		 * @param m The message to process
		 * @throws IOException
		 * @throws SwitchStateException
		 */
		protected void processDeviceMessage(DeviceMessage m)
				throws IOException {

			switch (m.getType()) {

			default:
				break;
			}

		}


	}



}
