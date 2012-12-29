package bpi.most.opcua.example.basic;

import org.apache.log4j.Logger;

import bpi.most.opcua.server.core.ClientIdentity;
import bpi.most.opcua.server.core.auth.IUserPasswordAuthenticator;

public class SampleAuthenticator implements IUserPasswordAuthenticator {

	private static final Logger LOG = Logger.getLogger(SampleAuthenticator.class);
	
	@Override
	public boolean authenticate(ClientIdentity clientIdentity) {
		LOG.info(String.format("user %s authenticates with password %s", clientIdentity.getUsername(), clientIdentity.getPassword()));
		
		/*
		 * actual authentication would be done here
		 */
		boolean authenticated = true;
		
		return authenticated;
	}

}
