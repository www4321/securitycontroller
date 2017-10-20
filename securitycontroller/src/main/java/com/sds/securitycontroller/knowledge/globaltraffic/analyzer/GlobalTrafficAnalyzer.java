
package com.sds.securitycontroller.knowledge.globaltraffic.analyzer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;  
import java.util.Set;
import java.util.Iterator;
import java.text.ParseException;  
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.event.Event;
import com.sds.securitycontroller.event.EventSubscriptionInfo;
import com.sds.securitycontroller.event.EventType;
import com.sds.securitycontroller.event.IEventListener;
import com.sds.securitycontroller.event.manager.IEventManagerService;
import com.sds.securitycontroller.flow.FlowEventArgs;
import com.sds.securitycontroller.flow.FlowGlobalRecord;
import com.sds.securitycontroller.flow.FlowInfo;
import com.sds.securitycontroller.flow.FlowMatch;
import com.sds.securitycontroller.flow.FlowTrafficStats;
import com.sds.securitycontroller.knowledge.KnowledgeType;
import com.sds.securitycontroller.knowledge.globaltraffic.MatchPath;
import com.sds.securitycontroller.knowledge.globaltraffic.analyzer.current_Globle_flowInfo;
import com.sds.securitycontroller.knowledge.networkcontroller.Topology;
import com.sds.securitycontroller.module.ISecurityControllerModule;
import com.sds.securitycontroller.module.ISecurityControllerService;
import com.sds.securitycontroller.module.SecurityControllerModuleContext;
import com.sds.securitycontroller.module.SecurityControllerModuleException;
import com.sds.securitycontroller.restserver.IRestApiService;
import com.sds.securitycontroller.storage.IStorageSourceService;
import com.sds.securitycontroller.storage.QueryClause;
import com.sds.securitycontroller.storage.QueryClauseItem;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;


public class GlobalTrafficAnalyzer implements IGlobalTrafficAnalyzeService,
		ISecurityControllerModule, IEventListener {

	private int allCount=0;
	@Override
	public int getAllCount() {
		return allCount;
	}

	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}

	private static  HashMap<String, MatchPath>  FlowinfoList=new  HashMap<String, MatchPath> ();
	protected IEventManagerService eventManager;
	protected static Logger log = LoggerFactory.getLogger(GlobalTrafficAnalyzer.class);
	protected IStorageSourceService storageSource;
	String ncHost;
	private static final String tableName = "globalrecord";
	public static Map<String, Integer> V_Source= new HashMap<String,Integer>();
	public static Map<String, Integer> V_Destination= new HashMap<String,Integer>();
	
	public static Map<String, Double> Thita= new HashMap<String,Double>();
	public static Map<String, Integer> suspiciousMITM_Node= new HashMap<String,Integer>();
	public static Map<String, Map<String, Integer>> Destination_List=new HashMap<String, Map<String, Integer>>();
	public static Map<String, Map<String, Long>> L_time=new HashMap<String, Map<String, Long>>();
	public static Map<String, List<String>> Source_List= new HashMap<String, List<String>>();
	
	private static int recordTimeInterval=3;//MINUTES
	private static final String tablenameGlobalRecord = "globalrecord";
	private volatile Boolean indexExsit = false;
	
	protected IRestApiService restApi;
	
	Topology topology = null;
	HashMap<String, MatchPath> globalFlowMap;

	
	protected Map<String, Long[]> flowTrafficStatMap = new HashMap<String, Long[]>();
	
	/**
	 * get topology status by RPC
	 */
	@SuppressWarnings("rawtypes")
	Topology getTopology(){
		try {
			Object[] rqArgs = {KnowledgeType.NETWORK_TOPOLOGY,null,null};
			Class serviceClass=com.sds.securitycontroller.knowledge.manager.IKnowledgeBaseService.class;
			String methodName="queryEntity";
			
//			Thread.sleep(10000);
			Topology result = (Topology)eventManager.makeRPCCall(serviceClass, methodName, rqArgs);
			System.err.println(result);
			return  result;
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return null;
		}
	}
	public current_Globle_flowInfo flowfinishgenerating(HashMap<String, MatchPath>FlowinfoList,HashMap<String, MatchPath>flowinfolist)
	{   
		HashMap<String, MatchPath>  Flowfinish=new HashMap<String, MatchPath>() ;//存储结束的流
		HashMap<String, MatchPath>  Flowlast=new HashMap<String, MatchPath>() ;//存储还存在的流
		if(FlowinfoList == null && flowinfolist != null){
			Flowlast = flowinfolist;
			Set<String> set1 = flowinfolist.keySet();
			Iterator<String> it1 = set1.iterator();
			while (it1.hasNext()){
				String key = it1.next();
				String createTime=DateTime.now().toString();
				Flowlast.get(key).createTime = createTime;
			}
		}
			
		if (flowinfolist != null && FlowinfoList != null ) 
		{
			Set<String> set1 = flowinfolist.keySet();
			Iterator<String> it1 = set1.iterator();
			while (it1.hasNext()){
				String key = it1.next();
				if (FlowinfoList.containsKey(key)!=true){
					String createTime=DateTime.now().toString();
					flowinfolist.get(key).createTime = createTime;
					Flowlast.put(key, flowinfolist.get(key));
				}
			}
			Set<String> set2 = FlowinfoList.keySet();
			Iterator<String> it2 = set2.iterator();
			while (it2.hasNext())
			 {
				String key = it2.next();
				 if(flowinfolist.containsKey(key)!=true)//FlowEntry:1-->0
				 {
					 String finishTime=DateTime.now().toString();
					 FlowinfoList.get(key).finishTime = finishTime;
					 Flowfinish.put(key, FlowinfoList.get(key));
					//test below:
				//	 System.out.println("finished_flow_ID is "+key+"  finish_time= "+finishTime);
				 }
				 else   //网络中仍然存在这个流表项，那么执行如下操作：
				 {
					 //if((flowinfolist.get(key).packetCount)==(FlowinfoList.get(key).packetCount))
					 //{
						 //String finishTime=DateTime.now().toString();
						 //Flowfinish.put(key, finishTime);
						 //test below:
			//			 System.out.println("finished_flow_ID is"+key+" finish_time="+finishTime);
						 
					 //}
					 //else
					 //{
						 String lastTime=DateTime.now().toString();
						 FlowinfoList.get(key).lastTime = lastTime;
						 Flowlast.put(key, FlowinfoList.get(key));
						//test below:
					// System.out.println("last_flow_ID is "+key+"  last_time= "+lastTime);
					 //}
				 }
			}
		}
		if (flowinfolist ==null && FlowinfoList !=null){
			Set<String> set2 = FlowinfoList.keySet();
			Iterator<String> it2 = set2.iterator();
			while (it2.hasNext()){
				String key = it2.next();
				String finishTime=DateTime.now().toString();
				FlowinfoList.get(key).finishTime = finishTime;
				Flowfinish.put(key, FlowinfoList.get(key));
			}
		}
		return  new  current_Globle_flowInfo(Flowfinish,Flowlast);
	}
	 public static Date toDate(String dateStr) {  
	        Date date = null;  
	        SimpleDateFormat formater = new SimpleDateFormat();  
	         formater.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");  
	       try {  
	           date = formater.parse(dateStr);  
	        } catch (ParseException e) {  
	             e.printStackTrace();  
	       }  
	      return date;  
	   }  
	@Override
	public void processEvent(Event e) {
	//	System.out.println(DateTime.now());
		FlowEventArgs fArgs = (FlowEventArgs)e.args;
		List<FlowInfo> fList = fArgs.flowList;
		System.out.println("the total number of SDN flow table is:  "+fList.size());
		//globalFlowMap = 
		calculateGloableTrafficMapping(fList);
		//recordTrafficStatus(this.globalFlowMap);
		recordGlobalFlow2DB(this.globalFlowMap);
		System.out.println("the total number of GFT is: "+globalFlowMap.size());
		HashMap<String, MatchPath> flowinfolist=this.globalFlowMap;
	//	System.out.println(flowinfolist.toString());
		
	flowFinishEventArgs Args = new flowFinishEventArgs();
	current_Globle_flowInfo Current_Globle_flowInfo=flowfinishgenerating(FlowinfoList,flowinfolist);
	 Args.finished_flow=Current_Globle_flowInfo.Flowfinish;
	 Args.last_flow=Current_Globle_flowInfo.Flowlast;
	this.eventManager.addEvent(new Event(
				EventType.FLOW_FINISH, null, this,
				Args));
	//recordGlobalFlow2ES(Args.finished_flow);
    FlowinfoList.clear();
	
//System.out.println("fList is : "+fList+"FlowinfoList is : "+FlowinfoList+"flowinfoList is : "+flowinfolist);
//     Map<String, Map<String, Long>> srcList=new HashMap<String, Map<String, Long>>();
//	 Map<String, Map<String, Long>> dstList=new HashMap<String, Map<String, Long>>();
    long time_start=System.nanoTime();
    Map<String, Map<String, Map<Long,Long>>> GFT_list=new HashMap<String,Map<String, Map<Long,Long>>>();
	 for(Iterator it =  flowinfolist.keySet().iterator();it.hasNext();)
 {   
	Map<Long, Long> Traffic= new HashMap<Long, Long>();
	Map<String, Map<Long, Long>> Node_Info=new HashMap<String,Map<Long, Long>>();
//    Map<String, Integer> destination_single= new HashMap<String, Integer>();
//    Map<String, Long> time_single= new HashMap<String, Long>();
     String key = it.next().toString();
//     Map<String, Long> cache= new HashMap<String, Long>();
//     Map<String, Long> cache1= new HashMap<String, Long>();
     String v_Destination=flowinfolist.get(key).getmatchlist().getFirst().getDataLayerDestination();
     String v_Source=flowinfolist.get(key).getmatchlist().getFirst().getDataLayerSource();
     String createTime=flowinfolist.get(key).createTime;
     long byteCount=flowinfolist.get(key).byteCount;
     long packetCount= flowinfolist.get(key).packetCount;
     long plus=byteCount+packetCount;
     long sport=flowinfolist.get(key).getmatchlist().getFirst().getTransportSource();
     long dport=flowinfolist.get(key).getmatchlist().getFirst().getTransportDestination();
     long plus_port=sport+dport;
     System.out.println("GFT_ID is: "+key+",byteCount is:"+byteCount+",packetCount is:"+packetCount);
     System.out.println("createTime is:"+createTime+",matchlist"+flowinfolist.get(key).getmatchlist().toString());
     System.out.println("pathlink: "+flowinfolist.get(key).getpathlink().toString());
     Traffic.put(plus, plus_port);
     Node_Info.put(v_Destination, Traffic);
     if(GFT_list.containsKey(v_Source))
     {
    	
    		 GFT_list.get(v_Source).put(v_Destination,Traffic);
    	
     }
     else
    	 GFT_list.put(v_Source, Node_Info);
   // System.out.println("the packetCount of "+key+" is: "+packetCount);
   // System.out.println("the byteCount of "+key+" is: "+byteCount);
     Date date = toDate(createTime);
     long CreateTime=date.getTime();
//     cache.put(v_Source, packetCount);
//     cache1.put(v_Destination, packetCount);
//     if(!(srcList.containsKey(v_Destination)))
//     {
//    	 srcList.put(v_Destination, cache);
//     }
//     else
//     {
//    	 srcList.get(v_Destination).put(v_Source,packetCount);
//     }
//     if(!(dstList.containsKey(v_Source)))
//     {
//    	 dstList.put(v_Source, cache1);
//     }
//     else
//     {
//    	 dstList.get(v_Source).put(v_Destination,packetCount);
//     }
 }
     for(Iterator it=GFT_list.keySet().iterator();it.hasNext();)
     {

    	 String smac=it.next().toString();
    	 Map<String, Map<Long,Long>> nodeInfo=GFT_list.get(smac);
    	 Set<String> Dmac=nodeInfo.keySet();
    	 Iterator Dmac_interator=Dmac.iterator();
    	 while(Dmac_interator.hasNext())
    	 {
    		 Map<Long, Long> traffic= new HashMap<Long, Long>();
    		 String dmac=Dmac_interator.next().toString();
             
    		 if(GFT_list.containsKey(dmac))
    		 {
    			 Iterator M=GFT_list.get(dmac).keySet().iterator();
    			 while(M.hasNext())
    			 {
    				 String ddmac=M.next().toString();
    				 if(ddmac==smac)
    					 continue;
    				 Iterator tfc=GFT_list.get(dmac).get(ddmac).keySet().iterator();
    				 while(tfc.hasNext())
    				 {
    					 String s=tfc.next().toString();
    					
    					 long btct=new Long(s);
    					 if(!(btct>0))
    						 continue;
    					// System.out.println("s= "+s+"btct"+btct);
    					 long pt=GFT_list.get(dmac).get(ddmac).get(btct);
    					 if(GFT_list.get(smac).get(dmac).containsKey(btct))
    					 {
    						 if(GFT_list.get(smac).get(dmac).get(btct).equals(pt))
    						 {
    						//	 System.out.println("the attacker's MAC address is: "+dmac);
    						 }
    					 }
    				 }
    				 
    			 }
    		 }
    	 }
     }
     long time_end=System.nanoTime();
  //   System.out.println("The culculating time of the method is(nanosecond):"+(time_end-time_start));
//    for(Iterator it= srcList.keySet().iterator();it.hasNext();)
//     {
//  	   String dst=it.next().toString();
//  	   Map<String, Long> src_traffic=srcList.get(dst);
//  	   Set<String> SRC=src_traffic.keySet();
//  	   Iterator src_interator=SRC.iterator();
//  	   while(src_interator.hasNext())
//  	   {
//  		  String src= src_interator.next().toString();
//  		  double pkt_in=src_traffic.get(src);
//  		  if(dstList.containsKey(dst))
//  		  {
//  			Map<String, Long> dst_traffic=dstList.get(dst);
//  			Set<String> DST=dst_traffic.keySet();
//  	  	   Iterator DST_traffic=DST.iterator();
//  	  	   while(DST_traffic.hasNext())
//  	  	   {
//  	  		   String dstN=DST_traffic.next().toString();
//  	  		   double dst_pkt=dst_traffic.get(dstN);
//  	  		  if(dst_pkt>1)
//				  {
//					  double ratio=pkt_in/dst_pkt;
//					  if(ratio>0.95 && ratio<1.05 && dstN!=src && !(dst.equals("38:97:d6:ad:c3:da")))
//					  {
//						  System.out.println("The MITM attacker's MAC address is: "+dst);
//						  break;
//					  }
//				  }
//  	  	   }
//  			  
//  				
//  			  }
//  		  }
//  	   }
     
     
  //  long time_end=System.nanoTime();
  //  System.out.println("The culculating time of the method is(nanosecond):"+(time_end-time_start));
     
     
//     
//     if(L_time.containsKey(v_Source))
//     {
//    	 if(!(L_time.get(v_Source).containsKey(v_Destination)))
//    	 {
//    		 Long time=System.nanoTime();
//    		 L_time.get(v_Source).put(v_Destination, CreateTime);
//    	 }
//     }
//     else
//     {   
//    	 Long time=System.nanoTime();
//    	 time_single.put(v_Destination, CreateTime);
//    	 L_time.put(v_Source, time_single);
//     }
//     
//     
     //set up a list Destination_List ==>HashMap<Destination,List<Source>>
//    if(!(Source_List.containsKey(v_Destination)))
//    	{ 
//    	//V_source.add(v_Source);
//    	Source_List.put(v_Destination, new ArrayList<String>());}
//     Source_List.get(v_Destination).add(v_Source);
//    if(!(Destination_List.containsKey(v_Source)))
//    		 {
//    	 destination_single.put(v_Destination, 1);
//    	 Destination_List.put(v_Source, destination_single);
//    	
//    		 }
//     else
//     {   if(!(Destination_List.get(v_Source).containsKey(v_Destination)))
//                {
//    	 Destination_List.get(v_Source).put(v_Destination, 1);
//                }
//        else
//         {
//    	 int q=Destination_List.get(v_Source).get(v_Destination);
//    	 destination_single.put(v_Destination,q+1);
//    	 Destination_List.get(v_Source).put(v_Destination, q+1);
//         }
//       
//     }
//    
    
    
    
    
//     if(!(V_Source.containsKey(v_Source)))
//     {
//    	 
//    	 V_Source.put(v_Source, 1);
//     }
//     else
//     {
//    	 int i=V_Source.get(v_Source).intValue()+1;
//    	 V_Source.put(v_Source, i);
//     }
//     if(!(V_Destination.containsKey(v_Destination)))
//     {
//    	 V_Destination.put(v_Destination, 1);
//     }
//     else
//     {
//    	 int j=V_Destination.get(v_Destination).intValue()+1;
//    	 V_Destination.put(v_Destination, j);
//     }
     
 	//	System.out.println("v_Source: "+v_Source+" speed: "+V_Source.get(v_Source).toString());

     
     
     
     
  //  System.out.println("V_Source: "+V_Source+" V_Destination "+V_Destination);
    
     
     
     
     /*  flowfinish
     if (Args.last_flow.containsKey(key)){
    	 FlowinfoList.put(key, Args.last_flow.get(key));
     }
     else{
    	 String createTime=DateTime.now().toString();
    	 flowinfolist.get(key).createTime = createTime;
    	 FlowinfoList.put(key, flowinfolist.get(key));
     }*/
//     }
    
    
//   long start_time=System.nanoTime();
//   for(Iterator it= L_time.keySet().iterator();it.hasNext();)
//   {
//	   String source=it.next().toString();
//	   Map<String, Long> Dest_time=L_time.get(source);
//	   Set<String> dest=Dest_time.keySet();
//	   Iterator dest_interator=dest.iterator();
//	   while(dest_interator.hasNext())
//	   {
//		   String dest1=dest_interator.next().toString();
//		   long time1=Dest_time.get(dest1);
//		   if(L_time.containsKey(dest1))
//		   {
//			   if(L_time.get(dest1).containsKey(source))
//			   {
//		   long time2=L_time.get(dest1).get(source);
//		   if(time1>time2)
//			   continue;	
//			   }
//		   }
//		   if(!(source.equals("38:97:d6:ad:c3:da")))
//		   { System.out.println("the MITM attacker detected by method 5 is : "+source);
//		   break;
//		   }
//	   }
//   }
//   long end_time=System.nanoTime();
//   long using_time=end_time-start_time;
//   System.out.println("the time using by method 5 is : "+using_time);
   
  //detection method 4:
    
    
  /*
    long starTime=System.nanoTime();
    
    for(Iterator it = Destination_List.keySet().iterator();it.hasNext();)
    {
//    	 Map<String, Integer> destination_single= new HashMap<String, Integer>();    	 
    	int M=0,N=0,Q=0;
    	String key = it.next().toString();
    	 Map<String,Integer> H=Destination_List.get(key);
    	Set<String> Dest=H.keySet();
    	Q=Dest.size();
    	Iterator ii=Dest.iterator();
    	 while(ii.hasNext())
    	 {
    		 String dest=ii.next().toString();
    		 int w=H.get(dest);
    		 N=N+w;
    		 if(M<w)
             { M=w;}
              

    	 
    	 	 ////////////////detection method 5: abandoned method 
    /*		 if(Source_List.containsKey(key)){
    	 if(!(Source_List.get(key).contains(dest)))
    	 {
    		 if(!(suspiciousMITM_Node.containsKey(key)))
  			{
  				suspiciousMITM_Node.put(key,1);
  			}
  			else
  			{
  				int n=suspiciousMITM_Node.get(key).intValue()+1;
  				suspiciousMITM_Node.put(key, n);
    	 System.out.println("The MITM attacker's "+"MAC is "+key+" (detected by method 5)");
    	 }
    	 }
    
        
    	 } */
    	  
    /* 
    	if(N>5)
    	{
         long W=M/N;
    	 if(W>0.5)
    	 {
    		 if(!(suspiciousMITM_Node.containsKey(key)))
 			{
 				suspiciousMITM_Node.put(key,1);
 			}
 			else
 			{
 				int n=suspiciousMITM_Node.get(key).intValue()+1;
 				suspiciousMITM_Node.put(key, n);
 			}
    System.out.println("The MITM attacker's "+"MAC is "+key+" (detected by method 4)");
    }
    	}	 
   	 }
    }  
  long endTime=System.nanoTime();
 long Time=endTime-starTime; 
 System.out.println("culculating time of method 4: "+Time);
    */

    /*
    long start_time=System.nanoTime();
    for(Iterator it =  V_Destination.keySet().iterator();it.hasNext();)
    {
    	String key = it.next().toString();
    	Double speed_destination=Double.valueOf(V_Destination.get(key));
    	if(V_Source.containsKey(key) && V_Source.get(key)!=0)
    	{
    		Integer speed_source=V_Source.get(key);            
    	/*	/////////////////////////detection method 1:
    		if((speed_destination>50) && (speed_source>50))
    		{   if(!(suspiciousMITM_Node.containsKey(key)))
    		         {
    			suspiciousMITM_Node.put(key, 1);
    		         }
    		    else
    		         {
    			int q=suspiciousMITM_Node.get(key).intValue()+1;
    			suspiciousMITM_Node.put(key, q);
    			if(!key.equals("38:97:d6:ad:c3:da"))
   	System.out.println("The MITM attacker's "+"MAC is "+key+" (detected by method 1)");
    			
    		         }
    		} */
    		
    		
    		
    		
    	//detection method 2:
    	/*	
    		Double thita = speed_destination/speed_source;
    		Thita.put(key, thita);
    		
    		if((0.95<thita) && (thita<1.05))
    		{
    			if(!(suspiciousMITM_Node.containsKey(key)))
    			{
    				suspiciousMITM_Node.put(key,1);
    			}
    			else
    			{
    				int n=suspiciousMITM_Node.get(key).intValue()+1;
    				suspiciousMITM_Node.put(key, n);
    			}
    			if(!key.equals("38:97:d6:ad:c3:da"))
    			{
    			System.out.println("The MITM attacker's "+"MAC is "+key+" (detected by method 2)"+"thita: "+thita);
    			}
    			}
    			*/
    		
    	
    	
	
    	
    
               
//    long endTime=System.currentTimeMillis();
//   long Time=endTime-starTime;
 //System.out.println("the detection time of method 1 is : "+Time);
  /*  for(Iterator it =  suspiciousMITM_Node.keySet().iterator();it.hasNext();)
    {
    	String key = it.next().toString();
    	int i=suspiciousMITM_Node.get(key).intValue();
    	if(i>1)
    	{
    		System.out.println("The MITM attacker's MAC address is "+key);
    	}
   }
    */
	
    
    
//  long endTime=System.nanoTime();
//	 long Time=endTime-start_time; 
//	 System.out.println("culculating time of method 1: "+Time);
	}
	


	@Override
	public void addListenEventCondition(EventType type,
			EventSubscriptionInfo condition) {
	}

	@Override
	public void processAddListenEventCondition(EventType type, EventSubscriptionInfo condition) {

	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleServices() {
		Collection<Class<? extends ISecurityControllerService>> services =
                new ArrayList<Class<? extends ISecurityControllerService>>(1);
        services.add(IGlobalTrafficAnalyzeService.class);
        return services;
	}

	@Override
	public Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> getServiceImpls() {
		Map<Class<? extends ISecurityControllerService>, ISecurityControllerService> m = new HashMap<Class<? extends ISecurityControllerService>, ISecurityControllerService>();
        // We are the class that implements the service
        m.put(IGlobalTrafficAnalyzeService.class, this);
        return m;
	}

	@Override
	public Collection<Class<? extends ISecurityControllerService>> getModuleDependencies() {
		Collection<Class<? extends ISecurityControllerService>> l = new ArrayList<Class<? extends ISecurityControllerService>>();
        //l.add(IStorageSourceService.class);
        l.add(IRestApiService.class);
        l.add(IStorageSourceService.class);
        return l;
	}

	@Override
	public void init(SecurityControllerModuleContext context)
			throws SecurityControllerModuleException {
    	this.eventManager = context.getServiceImpl(IEventManagerService.class, this);
    	this.storageSource = context.getServiceImpl(IStorageSourceService.class, this);
    	if(context.getConfigParams(this).get("recordInterval")!=null)
    		recordTimeInterval = Integer.valueOf( context.getConfigParams(this).get("recordInterval"));
    	
    	// listening to RETRIEVED FLOW LIST
    	
    	
    	// eventManager.addEventListener(EventType.RETRIEVED_FLOWLIST, this);
    	
    	if(context != null)
        	this.restApi = context.getServiceImpl(IRestApiService.class);

//    	Map<String, String> configOptions = context.getConfigParams(this);
    	String _ncHost = (context.getGeneralConfigParams("securitycontroller.generalparam")).get("nchost");
    	if(_ncHost!=null)
    		this.ncHost = _ncHost;
    //	TopologyInstance.setNcHost(ncHost);//adding linking relations between hosts.
    //	NetworkDeviceEntity.setNcHost(ncHost); 
//        if((configOptions.get("nchost"))!=null)
//        	this.ncHost = configOptions.get("nchost").replace("[nchost]", _ncHost);
    	log.info(" Global traffic com.sds.securitycontroller.knowledge.globaltraffic.analyzer initializing...");
	}

	@Override
	public void startUp(SecurityControllerModuleContext context) {
		 restApi.addRestletRoutable(new GlobalTrafficAnalyzerRoutable());
		 cf = new CaculateFlowPath();
		 fg=new FlowIngeration();
	}

	
	CaculateFlowPath cf;
	FlowIngeration fg=null;
	protected HashMap<String,MatchPath> path;
	protected HashMap<String,MatchPath> path1;
//	protected static Logger log = LoggerFactory.getLogger(FindPath.class);
	
	@Override
	public void calculateGloableTrafficMapping(List<FlowInfo> flist)
	{
		Date getDataTime1 = new Date();
		 fg.flowingeration(flist);
		 Date getDataTime2 = new Date();
		 //时间转换
		 /*System.out.println(getDataTime1.getTime());
		 String string=String.valueOf(getDataTime1.getTime());
		 Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(Long.parseLong(string));
         Formatter ft=new Formatter(Locale.CHINA);
         System.out.println(ft.format("%1$tY年%1$tm月%1$td日%1$tA，%1$tT %1$tp", cal).toString());*/
		 
		 
		 //globalFlowMap.clear();
		 
		  cf.sort();
		//  cf.getvalue();
		  Date getDataTime3 = new Date();

		   path=cf.findmod();
		  cf.getvalue2();
		   Date getDataTime4 = new Date();
		   
		    
		   log.debug(" Time consumed to flow ingeration: {}ms", getDataTime2.getTime()-getDataTime1.getTime());
		   log.debug(" Time consumed to flow sort: {}ms", getDataTime3.getTime()-getDataTime2.getTime());  
		   log.debug(" consumed to find all the flow path : {}ms, json size: {}", getDataTime4.getTime()-getDataTime1.getTime(),flist.toString().length());
		   globalFlowMap = path;
		   //System.out.println(path.size());
		   //System.out.println(globalFlowMap.size());
		   //store globalFlowMap in file
		   
		  /* try {
			   DateTime dt =DateTime.now();
				int name=dt.getYear()+dt.getMonthOfYear()+dt.getDayOfMonth()+dt.getHourOfDay()+dt.getMinuteOfHour();
				System.out.println(name);
				File file = new File("d:/SDN/flowData/"+name+"flow.txt");
				if (!file.exists()) {
					file.createNewFile();
				}
			FileWriter writer=new FileWriter(file);
			StringBuffer sb= new StringBuffer("");
			//sb.append(str+"/n")
			for (MatchPath mp : globalFlowMap.values()) {
				List<FlowMatch> listFlowMatchs=mp.getmatchlist();
				List<NodePortTuple> listNodePortTuples = mp.getpathlink();
				for (FlowMatch flowMatch : listFlowMatchs) {
					String str=flowMatch.getDataLayerSource()+","+flowMatch.getDataLayerDestination()+","+flowMatch.getNetworkSource()+
							","+flowMatch.getNetworkDestination()+","+flowMatch.getTransportSource()+","+flowMatch.getTransportDestination()+","+flowMatch.getNetworkProtocol();
					
					int len=listNodePortTuples.size();
					for(int i=0;i<len;i++){
						if(len==1){
							str+=","+listNodePortTuples.get(i).getNodeId()+":"+listNodePortTuples.get(i).getPortId()+"\n";
						}else {
							if(i==0){
								str+=","+listNodePortTuples.get(i).getNodeId()+":"+listNodePortTuples.get(i).getPortId()+"->";
							}else if (i==len-1) {
								str+=listNodePortTuples.get(i).getNodeId()+":"+listNodePortTuples.get(i).getPortId()+"\n";
							}else {
								str+=listNodePortTuples.get(i).getNodeId()+":"+listNodePortTuples.get(i).getPortId()+"->";
							}
						}
						
					}
					//System.out.print(str);
					sb.append(str);
					
				}
				
			}
			BufferedWriter bw = new BufferedWriter(writer);
			
            bw.write(sb.toString());
           
            bw.close();
            writer.close();
            log.info("data is saved in {}",file);
		   }catch (Exception e) {
			// TODO: handle exception
			   e.printStackTrace();
		}*/
		   /*System.out.println("test begin");
		   for (String key : globalFlowMap.keySet()) {
				for (FlowMatch fm : globalFlowMap.get(key).getmatchlist()) {
					System.out.println(fm.getInputPort());
				}
			}
		   System.out.println("test over");*/
	}

	public Map<String, MatchPath> getGlobalFlowMap() {
		return globalFlowMap;
	}

	@Override
	public Map<String, MatchPath> getGloableTrafficMapping() {
		return globalFlowMap;
	}

	@Override
	public List<MatchPath> queryFlowMatch(FlowMatch objMatch,boolean getOne) {
		if(globalFlowMap==null || objMatch==null)
			return null;
		/*
		Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "yuyuan").build();
	 	    TransportClient client =  new TransportClient(settings);
		    client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	    try{
		    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("current");
		    deleteIndexRequest.listenerThreaded(false);
		    client.admin().indices().delete(deleteIndexRequest);
		    }
        finally{
		for(String key :globalFlowMap.keySet()){
			MatchPath mp=globalFlowMap.get(key);
			LinkedList<FlowMatch> matchList = mp.getmatchlist();	
			for (FlowMatch flowMatch : matchList) {	
				String flowId=flowMatch.getId();
				this.storageSource.setTablePrimaryKeyName(tablenameGlobalRecord, "flowId");
				FlowGlobalRecord search=(FlowGlobalRecord) this.storageSource.getEntity(tablenameGlobalRecord, flowId, FlowGlobalRecord.class);
		            client.prepareIndex("current", "globalflow")
				        .setSource(search.getDBElements())
				        .execute()
				        .actionGet();		
			    }
		    }
		client.close();
        }
        */
		List<MatchPath> result=new ArrayList<MatchPath>();
		if(getOne){
			MatchPath matchPath=globalFlowMap.get( objMatch.getMatch() );
			if(matchPath!=null)
				result.add(matchPath);
			return result;
		}
		Collection<MatchPath> copyGlobalFlowMap=new ArrayList<MatchPath>(globalFlowMap.values());
		//实现分页查询
		for(MatchPath matchPath : copyGlobalFlowMap){
			for(FlowMatch refMatch: matchPath.getmatchlist()){
				boolean compareResult= FlowMatch.isFlowMatchesRelated(refMatch, objMatch);
				if(compareResult){
					result.add(matchPath);
				}
			}
		}
		int curPage=objMatch.getQueryPage();
		int size=objMatch.getQuerySize();
		
		int begin=(curPage-1)*size;
		int end=curPage*size;
		int count=result.size();
		if(begin<0){
			begin=0;
		}
		if(end>=count){
			end=count-1;
		}
		if(begin>=end){
			return null;
		}
		setAllCount(count);
		List<MatchPath> res=result.subList(begin, end);
		return res;
	}
	
	@Override
	public FlowTrafficStats queryTrafficStatus(
			Map<String, String> queryConditions) {
		try {
			if(queryConditions==null || queryConditions.isEmpty())
			{
				//return null;
			}
			List<QueryClauseItem> items = new ArrayList<QueryClauseItem>();
			//System.err.println(queryConditions.get("src_mac").isEmpty());
			//System.err.println(queryConditions.get("src_mac"));
			if(queryConditions.containsKey("src_mac")&&!queryConditions.get("src_mac").isEmpty())
				items.add(new QueryClauseItem("src_mac",queryConditions.get("src_mac"),QueryClauseItem.OpType.EQ));
			if(queryConditions.containsKey("dst_mac")&&!queryConditions.get("dst_mac").isEmpty())
				items.add(new QueryClauseItem("dst_mac",queryConditions.get("dst_mac"),QueryClauseItem.OpType.EQ));
			if(queryConditions.containsKey("src_ip")&&!queryConditions.get("src_ip").isEmpty())
				items.add(new QueryClauseItem("src_ip",queryConditions.get("src_ip"),QueryClauseItem.OpType.EQ));
			if(queryConditions.containsKey("dst_ip")&&!queryConditions.get("dst_ip").isEmpty())
				items.add(new QueryClauseItem("dst_ip",queryConditions.get("dst_ip"),QueryClauseItem.OpType.EQ));
			if(queryConditions.containsKey("src_port")&&!queryConditions.get("src_port").isEmpty())
				items.add(new QueryClauseItem("src_port",queryConditions.get("src_port"),QueryClauseItem.OpType.EQ));
			if(queryConditions.containsKey("dst_port")&&!queryConditions.get("dst_port").isEmpty())
				items.add(new QueryClauseItem("dst_port",queryConditions.get("dst_port"),QueryClauseItem.OpType.EQ));
			
			if(queryConditions.containsKey("time")&&!queryConditions.get("time").isEmpty())
				items.add(new QueryClauseItem("time",queryConditions.get("time"),QueryClauseItem.OpType.EQ));
			
			if(queryConditions.containsKey("starttime")&&!queryConditions.get("starttime").isEmpty())
				items.add(new QueryClauseItem("time",queryConditions.get("starttime"),QueryClauseItem.OpType.GTE));
			if(queryConditions.containsKey("endtime")&&!queryConditions.get("endtime").isEmpty())
				items.add(new QueryClauseItem("time",queryConditions.get("endtime"),QueryClauseItem.OpType.LTE));
			
			String[] defaultColumnNames={"SUM(`pkg_count`) as `pkg_count`","SUM(`byte_count`) as `byte_count`"
//					,"src_mac","dst_mac","src_ip","dst_ip","src_port","dst_port","time"
					};
			
			QueryClause qc = new QueryClause(items,tableName,defaultColumnNames,null);
			@SuppressWarnings("unchecked")
			List<FlowTrafficStats> result = (List<FlowTrafficStats>)storageSource.executeQuery(qc, FlowTrafficStats.class);
						
			if(result.size()>0){
				return result.get(0);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void recordTrafficStatus(Map<String, MatchPath> globalFlowMap) {
		if(globalFlowMap==null || globalFlowMap.isEmpty() )
			return;
//		System.out.printf("Ticks: %d\n",FlowTrafficStats.ticks);
		for( MatchPath mp: globalFlowMap.values()){
			//System.err.println("packetCount="+mp.packetCount);
			for(FlowMatch fm:mp.getmatchlist()){
				// ignore when no traffic
				if(mp.packetCount==0 || mp.byteCount==0)
					continue;
				String matchString=fm.getMatch();
				String pathlinks=mp.getpathlink().toString();//pathlink
				matchString+=";"+pathlinks;
				Long[] stats= flowTrafficStatMap.get(matchString);
				
				if(stats==null){
					stats=new Long[]{0l,0l};
					flowTrafficStatMap.put(matchString, stats);
				}
				stats[0]+=mp.packetCount;
				stats[1]+=mp.byteCount;
//				System.err.printf("[Flow traffic stats] interval=%ds, match={%s}, packets=%d, bytes=%d, total_pkts=%d, total_bytes=%d\n",
//						3, mp.getmatchlist().get(0).getMatch(), mp.packetCount, mp.byteCount, stats[0], stats[1] );
			}
		}
		
		/*for (Long[] stats : flowTrafficStatMap.values()) {
			System.err.println(stats[0]);
		}*/
		Date now=new Date();
		//System.out.println(now.getTime()-FlowTrafficStats.lastRecordTimestamp );
//		if( now.getTime()-FlowTrafficStats.lastRecordTimestamp > recordTimeInterval*6 ){
		if( now.getTime()-FlowTrafficStats.lastRecordTimestamp > recordTimeInterval*60000 ){
			//System.out.println(DateTime.now());
			for(java.util.Map.Entry<String, Long[]> entry: flowTrafficStatMap.entrySet()){
				try{
					FlowTrafficStats trafficStats 
						= new FlowTrafficStats(now, entry.getKey(),entry.getValue()[0],entry.getValue()[1]);
					storageSource.insertEntity(tableName, trafficStats);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			log.info("{} traffic status records in last {} min restored in DB.",flowTrafficStatMap.size(), recordTimeInterval);
			flowTrafficStatMap.clear();
			//
			FlowTrafficStats.lastRecordTimestamp=now.getTime();
		}
//		FlowTrafficStats.ticks = (FlowTrafficStats.ticks+1) % FlowTrafficStats.MAX_TICKS;
//		if(FlowTrafficStats.ticks==0){
//		}
	}
	public void recordGlobalFlow2DB(Map<String, MatchPath> globalFlowMap){
	//	System.out.println("step into 2DB");
		if (globalFlowMap == null || globalFlowMap.isEmpty())
			return;
		//synchronized(indexExsit){
		//    Settings settings = ImmutableSettings.settingsBuilder()
		//        .put("cluster.name", "yuyuan").build();
	 	//    TransportClient client =  new TransportClient(settings);
		
		//    client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	    /*
		try{
		    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("current");
		    deleteIndexRequest.listenerThreaded(false);
		    client.admin().indices().delete(deleteIndexRequest);
		    }
        finally{
        */
		for(String key :globalFlowMap.keySet()){
			String globalFlowId=key;
			MatchPath mp=globalFlowMap.get(key);
			String links = mp.getpathlink().toString();
			LinkedList<FlowMatch> matchList = mp.getmatchlist();
			
			for (FlowMatch flowMatch : matchList) {
				
				String flowId=flowMatch.getId();
				this.storageSource.setTablePrimaryKeyName(tablenameGlobalRecord, "flowId");
				FlowGlobalRecord search=(FlowGlobalRecord) this.storageSource.getEntity(tablenameGlobalRecord, flowId, FlowGlobalRecord.class);
				if(search!=null){
					/*
		            client.prepareIndex("current", "globalflow")
				        .setSource(search.getDBElements())
				        .execute()
				        .actionGet();
				        */
					break;
					}
				String src_mac=flowMatch.getDataLayerSource();
				String dst_mac=flowMatch.getDataLayerDestination();
				String src_ip=flowMatch.getNetworkSource();
				String dst_ip=flowMatch.getNetworkDestination();
				String src_port=flowMatch.getTransportSource()+"";
				String dst_port=flowMatch.getTransportDestination()+"";
				String pkg_count=Long.toString(mp.packetCount);
				String byte_count=Long.toString(mp.byteCount);
				String createTime=DateTime.now().toString();
				String lastTime=DateTime.now().toString();
				FlowGlobalRecord flowGlobalRecord=new FlowGlobalRecord(globalFlowId, flowId, src_mac, dst_mac, 
						src_ip, dst_ip, src_port, dst_port, pkg_count, byte_count, links, createTime, lastTime);
				this.storageSource.insertEntity(tablenameGlobalRecord, flowGlobalRecord);
                /*
				client.prepareIndex("current", "globalflow")
				        .setSource(flowGlobalRecord.getDBElements())
				        .execute()
				        .actionGet();
			}
		}
		client.close();
		indexExsit = true; 
		*/
        
		    }
	    }
	}
	public void recordGlobalFlow2ES(HashMap<String, MatchPath> finishedFlow) {
		if (finishedFlow == null || finishedFlow.isEmpty())
			return;
		for(String key :finishedFlow.keySet()){
			String json = finishedFlow.get(key).writeJsonString();
	 	    Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", "yuyuan").build();
	 	    TransportClient client =  new TransportClient(settings);
		    client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

            client.prepareIndex("historyrecord", "globalflow")
		        .setSource(json)
		        .execute()
		        .actionGet();
		    client.close();
		    System.out.println("process storing to ES");
	    }
	}
	
}

