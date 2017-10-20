/** 
*    Copyright 2014 BUPT. 
**/ 
package com.sds.securitycontroller.knowledge.manager;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.sds.securitycontroller.restserver.RestletRoutable;

public class KnowledgeBaseRoutable implements RestletRoutable {

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
        router.attach("/", KnowledgeBaseResource.class);
        router.attach("/{type}", KnowledgeBaseResource.class);
        router.attach("/{type}/", KnowledgeBaseResource.class);
        /*
        router.attach("/{type}/{id}", KnowledgeBaseResource.class);
        router.attach("/{type}/{id}/related/{related_type}", KnowledgeBaseResource.class);
        router.attach("/{type}/{id}/affiliated/{affiliated_type}", KnowledgeBaseResource.class);
        router.attach("/{type}/user_id/{user_id}", KnowledgeBaseResource.class);
        */
        // old APIs 
        router.attach("/{domain}/{type}", KnowledgeBaseResource.class);
        router.attach("/{domain}/{type}/", KnowledgeBaseResource.class);
        router.attach("/{domain}/{type}/{id}", KnowledgeBaseResource.class);
        router.attach("/{domain}/{type}/{id}/related/{related_domain}/{related_type}", KnowledgeBaseResource.class);
        router.attach("/{domain}/{type}/{id}/affiliated/{affiliated_domain}/{affiliated_type}", KnowledgeBaseResource.class);
        router.attach("/{domain}/{type}/user_id/{user_id}", KnowledgeBaseResource.class);
        return router;
	}

	@Override
	public String basePath() {
		return "/sc/knowledgebase";
	}

}
