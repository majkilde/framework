/*
 * <<
 * XPage Debug Toolbar
 * Copyright 2012 Mark Leusink
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License
 * >> 
 */

package com.debug;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;

import com.ibm.jscript.std.ObjectObject;
import com.ibm.jscript.types.FBSUtility;

@SuppressWarnings("unchecked")
public class DebugToolbar {

	private static final String TYPE_ERROR = "error";
	private static final String TYPE_INFO = "info";
	private static final String TYPE_DEBUG = "debug";
	private static final String TYPE_WARNING = "warning";
	private static final int MAX_MESSAGES = 1500;
		
	private HashMap<String, Object> toolbarConfig = null;
	
	private String msgContext = null;
	
	public DebugToolbar() {
		
	}
	
	public DebugToolbar( String msgContext ) {

		this.msgContext = msgContext;
		
	}

	/*
	 * log a message to the toolbar
	 */
	public void log(String msg, String msgContext, String type) {
		
		try {
			HashMap<String, Object> conf = getToolbarConfig();
			
			if (conf == null) {
			
				//config could not be retrieved/ created: log to console
				System.out.println("dBar: " + msg);
			
			} else {
			
				boolean loaded = (Boolean) conf.get("loaded");		//is the toolbar loaded/ enabled?
				
				if (loaded) {

					Vector<ObjectObject> messages = (Vector<ObjectObject>) conf.get("logMessages");
					
					//create a new message and add it to the vector of messsages
					ObjectObject newMsg = new ObjectObject();
					
					newMsg.putProperty("text", FBSUtility.wrap(msg) );
					newMsg.putProperty("type", FBSUtility.wrap(type) );
					newMsg.putProperty("date", FBSUtility.wrap( new Date().getTime() ));	//store date as milliseconds
					newMsg.putProperty("msgContext", FBSUtility.wrap((msgContext != null ? msgContext : this.msgContext)) );
					
					messages.add(0, newMsg);
					
					if (messages.size() > MAX_MESSAGES) {
						messages.remove(messages.size() - 1);
					}
					
					conf.put("logMessages", messages);
					
				}
			}
		} catch (Exception e) {
			System.err.println("dBar: error while logging: " + e.getMessage());
		
		}
	}

	public void info(String msg) {
		log(msg, null, TYPE_INFO);
	}
	public void info(String msg, String msgContext) {
		log(msg, msgContext, TYPE_INFO);
	}
	
	public void debug(String msg) {
		log(msg, null, TYPE_DEBUG);
	}
	public void debug(String msg, String msgContext) {
		log(msg, msgContext, TYPE_DEBUG);
	}

	public void error(String msg) {
		log(msg, null, TYPE_ERROR);
	}
	public void error(String msg, String msgContext) {
		log(msg, msgContext, TYPE_ERROR);
	}

	public void warn(String msg) {
		log(msg, null, TYPE_WARNING);
	}
	public void warn(String msg, String msgContext) {
		log(msg, msgContext, TYPE_WARNING);
	}

	/*
	 * returns current config or creates new (default) config
	 */
	public HashMap getToolbarConfig() {

		try {			
			if (toolbarConfig != null) {

				return toolbarConfig;
				
			} else {
				//retrieve config from sessionScope
				
				final FacesContext context = FacesContext.getCurrentInstance();
				final Map<Object, Object> sessionScope = (Map<Object, Object>) context.getApplication().getVariableResolver().resolveVariable(context, "sessionScope");
				
				HashMap toolbarConf = (HashMap) sessionScope.get("debugToolbar");
				
				if (toolbarConf != null) {

					this.toolbarConfig = toolbarConf;
					return toolbarConf;
					
				} else {
					
					//construct new default toolbar config
					HashMap<String, Object> config = new HashMap();
					
					config.put("contentType", null);
					config.put("hidden", false);
					config.put("loaded", true);
					config.put("logMessages", new Vector<ObjectObject>());
					config.put("timers", new java.util.HashMap());
					config.put("messagesDetached", false);
					config.put("hideLogTypes", new ArrayList<String>() );
					config.put("showLists", true);
					config.put("consolePath", FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/debugToolbarConsole.xsp");
					
					this.toolbarConfig = config;
					sessionScope.put("debugToolbar", config);
					
					return config;
				}
				
			}
		} catch (Exception e) {
			System.err.println("dBar: error while retrieving config: " + e.getMessage());
		}
		
		return null;
		
	}

}
