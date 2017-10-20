package com.sds.securitycontroller.directory.registry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.securitycontroller.directory.ModuleCommand;
import com.sds.securitycontroller.directory.ModuleCommandResponse;
import com.sds.securitycontroller.utils.JsonRequest;
import com.sds.securitycontroller.utils.JsonResponse;

public class RegistryCommandManageResource extends ServerResource{
	protected static Logger log = LoggerFactory.getLogger(RegistryCommandManageResource.class);
	IRegistryManagementService regManager = null;
	JsonRequest request=null;
	JsonResponse response=new JsonResponse();
	String command = null;

	
	@Override  
    public void doInit() {    
        regManager = 
                (IRegistryManagementService)getContext().getAttributes().
                get(IRegistryManagementService.class.getCanonicalName());
        command = (String) getRequestAttributes().get("command");
        
    }  
	

	@Get("json")
    public Object handleGetRequest() {		 
		do{
			if(null != command){
				/*ModuleCommand moduleCommand = regManager.findCommand(command);
				if (null == moduleCommand) {
					response.setMessage(404, "no such command");
					break;
				}
				try {					
					response.buildData("command", moduleCommand);					 
				} catch (IOException e) {
					log.error("write json response failed: ", e);
					response.setMessage(404,"write json response failed");
					break;
				}*/				
			}else{
				List<ModuleCommand> moduleCommands = regManager.getAllCommands();
				try {
					response.buildData("commands", moduleCommands);
				} catch (IOException e) {
					log.error("write json response failed: ", e);
					response.setMessage(404,"write json response failed");	
					break;
				}				
			}
			response.setCode(200);
		}while(false);
		
		return response.toString();
	}

	@Post
	public String handlePosts(String fmJson) throws JsonProcessingException, IOException{

		Map<String, String> options = new HashMap<String, String>();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode=mapper.readTree(fmJson);
		String subcommand =rootNode.path("subcmd").asText();
		String object =rootNode.path("object").asText();
		JsonNode optionsNode =rootNode.path("options");
		Entry<String, JsonNode> optionNode = null;
		Iterator<Entry<String, JsonNode>> on = optionsNode.fields();
		
		while(on.hasNext()){
			optionNode = on.next();
			options.put(optionNode.getKey(), optionNode.getValue().asText());
		}

		ModuleCommandResponse resp = new ModuleCommandResponse(command, subcommand, object, options);
		String result = regManager.executeCommand(resp);
		response.setData("\"result\": \""+result+"\"");
		response.setCode(200);
		return response.toString();
	}
}
