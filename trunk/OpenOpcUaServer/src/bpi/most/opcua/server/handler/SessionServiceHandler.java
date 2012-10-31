package bpi.most.opcua.server.handler;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.core.ActivateSessionRequest;
import org.opcfoundation.ua.core.ActivateSessionResponse;
import org.opcfoundation.ua.core.AnonymousIdentityToken;
import org.opcfoundation.ua.core.CancelRequest;
import org.opcfoundation.ua.core.CancelResponse;
import org.opcfoundation.ua.core.CloseSessionRequest;
import org.opcfoundation.ua.core.CloseSessionResponse;
import org.opcfoundation.ua.core.CreateSessionRequest;
import org.opcfoundation.ua.core.CreateSessionResponse;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.core.SessionServiceSetHandler;
import org.opcfoundation.ua.core.SignatureData;
import org.opcfoundation.ua.core.UserIdentityToken;
import org.opcfoundation.ua.core.UserNameIdentityToken;
import org.opcfoundation.ua.core.X509IdentityToken;
import org.opcfoundation.ua.encoding.DecodingException;
import org.opcfoundation.ua.transport.EndpointServiceRequest;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.transport.security.SecurityPolicy;
import org.opcfoundation.ua.utils.CertificateUtils;
import org.opcfoundation.ua.utils.CryptoUtil;

import bpi.most.opcua.server.core.Session;
import bpi.most.opcua.server.core.SessionManager;
import bpi.most.opcua.server.core.util.ArrayUtils;

public class SessionServiceHandler extends ServiceHandlerBase implements SessionServiceSetHandler {

	private static final Logger LOG = Logger
			.getLogger(SessionServiceHandler.class);
	
	private static final int NONCE_LENGTH = 32;
	
	/**
	 * several calls to the ActivateSession are done because
	 * <ul>
	 * <li>the client created a new securechannel</li>
	 * <li>the client changes its identity. the new credentials have
	 * also be passed to the application logic and therefore to the
	 * domain specific systems</li>
	 * </ul>
	 */
	@Override
	public void onActivateSession(
			EndpointServiceRequest<ActivateSessionRequest, ActivateSessionResponse> serviceReq)
			throws ServiceFaultException {
		LOG.info("---------------------------- ON ACTIVATE SESSION REQUEST ");
		LOG.debug(serviceReq);
		
		ActivateSessionRequest req = serviceReq.getRequest();
		NodeId authToken = req.getRequestHeader().getAuthenticationToken();
		Session session = server.getSessionManager().getSession(authToken);
		if (session == null){
			//TODO return service fault!
		}
		
		ExtensionObject oToken = req.getUserIdentityToken();
		//how to get the UserIdentityToken?
		if (oToken != null){
			try {
				UserIdentityToken uToken = (UserIdentityToken) oToken.decode();
				if (uToken instanceof UserNameIdentityToken){
					UserNameIdentityToken userNameToken = (UserNameIdentityToken) uToken;
					String user = userNameToken.getUserName();
					byte[] encryptPasswd = null;
					String encryptAlgo = userNameToken.getEncryptionAlgorithm();
					if (userNameToken.getPassword() != null){
						encryptPasswd = userNameToken.getPassword();
					}else{
						LOG.info(((byte[])oToken.getObject()).length + "\n" + oToken.getTypeId());
						LOG.info("password is null!");
					}
					
					LOG.info(String.format("==> user %s authenticats with password %s, encryption algorithm is %s, policy id is %s", user, encryptPasswd, encryptAlgo, userNameToken.getPolicyId()));
				
				
					/*
					 * decrypt the password
					 */
					try {
						if (encryptPasswd != null){
							LOG.info("encrypted passwd length: " + encryptPasswd.length);
							KeyPair kp = server.getStackServer().getApplicationInstanceCertificate();
							
							Cipher cipher = CryptoUtil.getAsymmetricCipher(encryptAlgo);
							cipher.init(Cipher.DECRYPT_MODE, kp.getPrivateKey().getPrivateKey());
							byte[] decryptedBytes = cipher.doFinal(encryptPasswd);
							
							
							/*
							 * part 4 describes the structure of the decrypted bytes: chapter 7.35.1, table 169
							 * 
							 * length: first 4 bytes are the length of the data + the length of the last nonce.
							 * tokenData: the token data of length x
							 * serverNonce: the last sent servernonce of length NONCE_LENGTH
							 */
							byte[] passwdBytes = Arrays.copyOfRange(decryptedBytes, 4, decryptedBytes.length - NONCE_LENGTH);
							byte[] lastNonceBytes = Arrays.copyOfRange(decryptedBytes, decryptedBytes.length - NONCE_LENGTH, decryptedBytes.length);
							
							String password = new String(passwdBytes);
							
							LOG.info("passwd: " + password);
							LOG.info("nonce equals last one: " + session.getLastNonce().equals(lastNonceBytes));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				
				
				}else if (uToken instanceof X509IdentityToken){
					X509IdentityToken certToken = (X509IdentityToken) uToken;
				}else if (uToken instanceof AnonymousIdentityToken){
					AnonymousIdentityToken anonymToken = (AnonymousIdentityToken) uToken;
					
					//TODO ?
				}
				
			} catch (DecodingException e) {
				e.printStackTrace();
			}
		}else{
			LOG.debug("--> User wants an anonymous session");
		}
		
		ActivateSessionResponse resp = new ActivateSessionResponse();
		
		
		//TODO call server implementation with user credentials
		session.setActive(true);
		
		// build response header
		ResponseHeader respHeader = buildRespHeader(req);
		resp.setResponseHeader(respHeader);
		
		//TODO validate clientSignature
		
		/*
		 * set response specific stuff
		 */
		byte[] nonce = CryptoUtil.createNonce(NONCE_LENGTH);
		resp.setServerNonce(nonce);
		session.setLastNonce(nonce);
		
		
		
		resp.setResults(new StatusCode[]{StatusCode.GOOD});
		LOG.debug("client sent software certificates:" + req.getClientSoftwareCertificates());
		
		sendResp(serviceReq, resp);
	}

	@Override
	public void onCancel(
			EndpointServiceRequest<CancelRequest, CancelResponse> serviceReq)
			throws ServiceFaultException {
		LOG.debug("---------------------------- ON CANCEL SESSION REQUEST ");
		LOG.debug(serviceReq);

		//TODO
	}

	@Override
	public void onCloseSession(
			EndpointServiceRequest<CloseSessionRequest, CloseSessionResponse> serviceReq)
			throws ServiceFaultException {
		LOG.debug("---------------------------- ON CLOSE REQUEST ");
		LOG.debug(serviceReq);
		
		CloseSessionRequest req = serviceReq.getRequest();
		
		//sessions identified by this token is going to be closed and deleted to free up resources
		NodeId authToken = req.getRequestHeader().getAuthenticationToken();
		LOG.debug("client closes session with authToken: " + authToken);
		
		if (req.getDeleteSubscriptions()){
			//TODO delete subscriptions
		}else{
			//TODO do not delete them, but keep reference to them
		}
		
		CloseSessionResponse resp = new CloseSessionResponse(buildRespHeader(req));
		sendResp(serviceReq, resp);
	}

	@Override
	public void onCreateSession(
			EndpointServiceRequest<CreateSessionRequest, CreateSessionResponse> serviceReq)
			throws ServiceFaultException {
		LOG.debug("---------------------------- ON CREATE SESSION REQUEST ");
//		LOG.debug(serviceReq);
		
		CreateSessionRequest req = serviceReq.getRequest();
		CreateSessionResponse resp = new CreateSessionResponse();
		
		// build response header
		ResponseHeader respHeader = buildRespHeader(req);
		resp.setResponseHeader(respHeader);

		//OPC UA Profiles supported by the Server
		//resp.setServerSoftwareCertificates(arg0)
		
		//when we have the information model
		//When a Session is created, the Server adds an entry for the Client in its SessionDiagnosticArray Variable. See Part 5 for a description of this Variable.

		LOG.info("client nonce is: " + req.getClientNonce());
		
		SessionManager sessionMgr = getSessionManager();
		
		Session session = sessionMgr.createSession();
		session.setClientDescription(req.getClientDescription());
		
		//make this visible in the adressspace
		//if this is empty, the server creates a value
		session.setSessionName(req.getSessionName());
		
		
		try {
			session.setClientCertificate(new Cert(req.getClientCertificate()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		session.setTimeout(getSessionTimeout(req.getRequestedSessionTimeout()));
		session.setMaxRespMsgSize(req.getMaxResponseMessageSize());
		
		NodeId sessionID = NodeId.randomGUID(1);
		session.setSessionID(sessionID);
		NodeId authToken = NodeId.randomGUID(1);
		session.setAuthenticationToken(authToken);
		
		server.getSessionManager().addSession(session);
		LOG.debug("created session for client: " + session);
		
		resp.setSessionId(sessionID);
		resp.setAuthenticationToken(authToken);
		resp.setRevisedSessionTimeout(session.getTimeout());
		byte[] nonce = CryptoUtil.createNonce(NONCE_LENGTH);
		resp.setServerNonce(nonce); //create a nonce with a length of 32 byte
		
		session.setLastNonce(nonce);
		
		byte[] serverCert = server.getStackServer().getApplicationInstanceCertificate().getCertificate().getEncoded();
		resp.setServerCertificate(serverCert); //TODO
		
		resp.setServerSoftwareCertificates(null);
		
		//the signed nonce from the client
		try {
			if (req.getClientCertificate() != null){
				resp.setServerSignature(getServerSignature(serviceReq.getChannel().getSecurityPolicy(), ArrayUtils.concat(req.getClientCertificate(), req.getClientNonce())));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOG.info("clients wants endpoints for uri: " + req.getServerUri());
		List<EndpointDescription> endpointList = server.getEndpointDescriptionsForUri(req.getServerUri());
		LOG.info("found endpoints: " + endpointList.size());
		if (endpointList.size() > 0){
			EndpointDescription[] endpointArray = new EndpointDescription[endpointList.size()];
			//TODO fill endpontdescription with missing data!!
			endpointList.toArray(endpointArray);
			resp.setServerEndpoints((EndpointDescription[]) endpointArray);
		}
		
		//this parameter is not used
		resp.setMaxRequestMessageSize(new UnsignedInteger(0));
		
		sendResp(serviceReq, resp);
	}
	
	private void validate(CreateSessionRequest req){
		//if host in req.getEndpointUrl() does not match me, 
		//raise auditurlmismatch event (is that really important for me??)
	}
	
	private void validate(ActivateSessionRequest req){
	}
	
	private void validate(CancelRequest req){
	}
	
	private void validate(CloseSessionRequest req){
	}
	
	private SignatureData getServerSignature(SecurityPolicy secPolicy, byte[] dataToSign) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException{
		SignatureData signature = null;
		
		PrivKey key = server.getStackServer().getApplicationInstanceCertificate().getPrivateKey();
		signature = CertificateUtils.sign(key.getPrivateKey(), secPolicy.getAsymmetricSignatureAlgorithmUri(), dataToSign);
		
		return signature;
	}
	
	/**
	 * creates a reasonable sessiontimeout in milliseconds and therefore
	 * tries to meet the clients decision. we only support sessiontimeouts
	 * between 10 minutes and one hour for now
	 * @param requestedTimeout
	 * @return
	 */
	private Double getSessionTimeout(Double requestedTimeout){
		Double revisedTimeout;
		if (requestedTimeout != null &&
				(requestedTimeout >= 1000 * 60 * 10 ||
				 requestedTimeout <= 1000 * 60 * 60)){
			//TODO may change this her e
			revisedTimeout = requestedTimeout;
		}else{
			revisedTimeout = 1000 * 60 * 30.0; //30 min
		}
		
		return revisedTimeout;
	}
}
