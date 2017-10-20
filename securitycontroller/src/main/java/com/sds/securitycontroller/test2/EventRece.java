package com.sds.securitycontroller.test2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.test1.FlowCommandEventArgs;

public class EventRece implements ISecurityControllerModule, IEventListener {
	protected static Logger log = LoggerFactory.getLogger(EventRece.class);
	protected IEventManagerService eventManager;
	
	
	@Override
	public void processEvent(Event e) {
		log.info("com.sds.securitycontroller.test2.EventRece received a '{}' event", e.type);
		log.info("消息的具体内容是："+(String)e.subject);
		if (e.type == EventType.FLOW_COMMAND) {
			log.info("com.sds.securitycontroller.test2.EventRece starts to handle an TEST Event......");
			staticFlowPusher((FlowCommandEventArgs)e.args);
			
			
			
		}

	}

	
	public String staticFlowPusher(FlowCommandEventArgs flowCommandEventArgs){
		String flowCommand = flowCommandEventArgs.getFlowCommand();
		String url1 = flowCommandEventArgs.getUrl();
		
		log.info(flowCommand+"    12345..........................");
		try {
            //创建连接
            URL url = new URL(url1);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.connect();

            //POST请求
            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());

            out.writeBytes(flowCommand);
            out.flush();
            out.close();

            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
            System.out.println(sb);
            log.info(sb+".......................................................12345");
            reader.close();
            // 断开连接
            connection.disconnect();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    
		return null;
	}
	
	
	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processAddListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
		this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
		
		
		log.info("BUPT security controller EventRece initialized.");

	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		eventManager.addEventListener(EventType.FLOW_COMMAND, this);
		
		log.info("BUPT security controller EventRece started");

	}

}
