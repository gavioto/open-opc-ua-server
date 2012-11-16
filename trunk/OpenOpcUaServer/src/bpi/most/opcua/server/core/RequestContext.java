package bpi.most.opcua.server.core;

/**
 * hold context information for a single request. this context object is
 * realized with thread locals, hence it is unique for every request. but with the static getter
 * it is accessible from everywhere
 * 
 * @author harald
 * 
 */
public class RequestContext {

	//TODO find central way to init the requestcontext.
	
	/**
	 * session of the client
	 */
	private Session session;

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param session
	 *            the session to set
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<RequestContext>() {

		@Override
		protected RequestContext initialValue() {
			return super.initialValue();
		}

	};

	public static RequestContext get() {
		return CONTEXT.get();
	}

	public static void reset() {
		CONTEXT.remove();
	}
}
