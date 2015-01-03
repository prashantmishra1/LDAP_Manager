package com.mycompany.ldap.apacheds;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.WordUtils;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.ldap.client.api.*;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/manager")
public class Manager {
	private static final String DEFAULT_LDAP_URL  = "gerrit.cfteam.in";
	private static final String DEFAULT_BASE_DN   = "ou=user,dc=commonfloor,dc=com";
	private static final int DEFAULT_LDAP_PORT    = 10389;
	
	private String LDAP_URL;
	private String BASE_DN;
	private int LDAP_PORT;
	private LdapConnection connection;
	
	private void setValues(JSONObject input){
		try {
			this.LDAP_URL  = input.has("url")? (String) input.get("url"):DEFAULT_LDAP_URL;
			this.LDAP_PORT = input.has("port")? (int) input.get("port"):DEFAULT_LDAP_PORT;
			this.BASE_DN   = input.has("base_dn")? (String) input.get("base_dn"):DEFAULT_BASE_DN;
			
			this.connection = new LdapNetworkConnection(this.LDAP_URL, this.LDAP_PORT);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@POST
	@Path("/addUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject addUser(JSONObject input){
		String CN;
		String password;
		
		setValues(input);
		
		JSONObject jsonResponse = new JSONObject();
		try {
			CN       = (String) input.get("cn");
			password = (String) input.get("password");
			String name = "";
			String nameArr[] = CN.split("\\.");
			for(String s : nameArr){
				name += WordUtils.capitalize(s) + " ";
			}
			System.out.println(name);
			connection.bind("cn=admin,ou=user,dc=commonfloor,dc=com", "apple123");
			
			
		    Entry entry = new DefaultEntry( 
		    	"cn=" + CN + "," + this.BASE_DN,
		    	"displayName",name.trim(),
		    	"objectclass:top",
	            "objectclass:person",
		        "objectclass:inetOrgPerson",
				"objectclass:organizationalPerson",
				"cn",CN,
				"sn",CN,
				"description:Gerrit User",
				"mail",CN +"@commonfloor.com",
				"userPassword",password
		        
		        );
		    
		    System.out.println(entry.toString());
			AddRequest addRequest = new AddRequestImpl();
			addRequest.setEntry( entry );
			
			AddResponse response = connection.add(addRequest);
			connection.unBind();

			jsonResponse.put("result", response.getLdapResult().getResultCode());
			return jsonResponse;
			
		} catch (LdapException e) {
			try {
				StringWriter stackTrace = new StringWriter();
				e.printStackTrace(new PrintWriter(stackTrace));
				jsonResponse.put("error", stackTrace.toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponse;
		} catch (JSONException e) {
			try {
				StringWriter stackTrace = new StringWriter();
				e.printStackTrace(new PrintWriter(stackTrace));
				//jsonResponse.put("error", stackTrace.toString());
				jsonResponse.put("error", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponse;
		}
	}
}

