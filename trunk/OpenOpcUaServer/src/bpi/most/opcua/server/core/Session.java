package bpi.most.opcua.server.core;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.ActivateSessionRequest;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.transport.ServerSecureChannel;
import org.opcfoundation.ua.transport.security.Cert;

public class Session {

	/**
	 * Human readable identification of the session
	 */
	private String sessionName;

	/**
	 * unique ID assigned by the server. is used to identify the Session in the
	 * audit logs and in the Server address space
	 */
	private NodeId sessionID;

	/**
	 * informatin that describes the client application
	 */
	private ApplicationDescription clientDescription;

	private Cert clientCertificate;

	/**
	 * current {@link ServerSecureChannel}
	 */
	private ServerSecureChannel secureChannel;

	/**
	 * used to associate an incoming request with a Session
	 */
	private NodeId authenticationToken;

	/**
	 * timeout after this session will be closed from the server.
	 */
	private Double timeout;

	/**
	 * The maximum size, in bytes, for the body of any response message. A
	 * Bad_ResponseTooLarge service fault is raised if a response message
	 * exceeds this limit.
	 */
	private UnsignedInteger maxRespMsgSize;
	
	/**
	 * indicates if the {@link Session} was already activated by
	 * an valid {@link ActivateSessionRequest}. only if
	 * it is active it can be used and requests are served to
	 * the owning client.
	 */
	private boolean active;
	
	/**
	 * last nonce which was sent to the client.
	 */
	private byte[] lastNonce;

	/**
	 * @return the sessionName
	 */
	public String getSessionName() {
		return sessionName;
	}

	/**
	 * @param sessionName
	 *            the sessionName to set
	 */
	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	/**
	 * @return the sessionID
	 */
	public NodeId getSessionID() {
		return sessionID;
	}

	/**
	 * @param sessionID
	 *            the sessionID to set
	 */
	public void setSessionID(NodeId sessionID) {
		this.sessionID = sessionID;
	}

	/**
	 * @return the clientDescription
	 */
	public ApplicationDescription getClientDescription() {
		return clientDescription;
	}

	/**
	 * @param clientDescription
	 *            the clientDescription to set
	 */
	public void setClientDescription(ApplicationDescription clientDescription) {
		this.clientDescription = clientDescription;
	}

	/**
	 * @return the secureChannel
	 */
	public ServerSecureChannel getSecureChannel() {
		return secureChannel;
	}

	/**
	 * @param secureChannel
	 *            the secureChannel to set
	 */
	public void setSecureChannel(ServerSecureChannel secureChannel) {
		this.secureChannel = secureChannel;
	}

	/**
	 * @return the authenticationToken
	 */
	public NodeId getAuthenticationToken() {
		return authenticationToken;
	}

	/**
	 * @param authenticationToken
	 *            the authenticationToken to set
	 */
	public void setAuthenticationToken(NodeId authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	/**
	 * @return the clientCertificate
	 */
	public Cert getClientCertificate() {
		return clientCertificate;
	}

	/**
	 * @param clientCertificate
	 *            the clientCertificate to set
	 */
	public void setClientCertificate(Cert clientCertificate) {
		this.clientCertificate = clientCertificate;
	}

	/**
	 * @return the timeout
	 */
	public Double getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(Double timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the maxRespMsgSize
	 */
	public UnsignedInteger getMaxRespMsgSize() {
		return maxRespMsgSize;
	}

	/**
	 * @param maxRespMsgSize the maxRespMsgSize to set
	 */
	public void setMaxRespMsgSize(UnsignedInteger maxRespMsgSize) {
		this.maxRespMsgSize = maxRespMsgSize;
	}
	
	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the lastNonce
	 */
	public byte[] getLastNonce() {
		return lastNonce;
	}

	/**
	 * @param lastNonce the lastNonce to set
	 */
	public void setLastNonce(byte[] lastNonce) {
		this.lastNonce = lastNonce;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Session [sessionName=" + sessionName + ", sessionID="
				+ sessionID + ", clientDescription=" + clientDescription
				+ ", clientCertificate=" + clientCertificate
				+ ", secureChannel=" + secureChannel + ", authenticationToken="
				+ authenticationToken + ", timeout=" + timeout
				+ ", maxRespMsgSize=" + maxRespMsgSize + "]";
	}
}
