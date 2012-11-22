package bpi.most.opcua.server.core;

import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.builtintypes.NodeId;

/**
 * TODO: timeout mechanism for sessions: quarz
 * clients have to make some kind of a touch on the session if an
 * requests appears. actually this can be done by the getSessionmethod here
 * 
 * create an ISessionManager with several implementations
 *   one could hold the session in a memcache so that several instances of the manager can be used. hence also several instances of the opc ua server could be used.
 * 
 * @author harald
 *
 */
public class SessionManager<T> {

	/**
	 * {@liparamServerSecureChannelnk Session}s managed by the server.
	 * both, activated and not yet activated sessions are stored here.
	 */
	private Map<NodeId, Session<T>> sessions;
	
	/**
	 * counter which is increased for every created session
	 */
	private static int sessionCount;
	
	public SessionManager(){
		sessions = new HashMap<NodeId, Session<T>>();
	}
	
	public Session<T> createSession(){
		Session<T> newSession = new Session<T>();
		return newSession;
	}
	
	public void addSession(Session<T> s){
		sessions.put(s.getAuthenticationToken(), s);
	}
	
	public Session<T> getSession(NodeId authToken){
		Session<T> s = sessions.get(authToken);
		
		if (s == null){
			//TODO raise service fault
		}else{
			if (!s.isActive()){
				//TODO raise service fault
			}
		}
		
		//TODO touch the session to reset session-timeout
		
		return s;
	}
	
	public boolean closeSession(NodeId authToken){
		boolean success = true;
		
		//TODO clear up everything. maybe delete subscriptions!
		sessions.remove(authToken);
		
		return success;
	}
}

