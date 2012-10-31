package bpi.most.opcua.server.handler;

import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.ServiceRequest;
import org.opcfoundation.ua.builtintypes.ServiceResponse;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.core.RequestHeader;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.transport.EndpointServiceRequest;

import bpi.most.opcua.server.core.SessionManager;
import bpi.most.opcua.server.core.UAServer;
import bpi.most.opcua.server.core.adressspace.AddressSpace;

public class ServiceHandlerBase{

	protected UAServer server;
	
	protected DiagnosticInfo buildDiagnostics(){
		return null;
	}
	
	protected void validateHeader(RequestHeader reqHeader){
		//TODO do header validation here
		
		//TODO validate authtoken if it matches the session
	}
	
	protected ResponseHeader buildRespHeader(ServiceRequest req) {
		// TODO common validation

		// TODO logging

		// build response header
		ResponseHeader respHeader = new ResponseHeader();
		respHeader.setTimestamp(new DateTime());
		
		// requesthandle is always the one the client sent in its request
		respHeader.setRequestHandle(req.getRequestHeader()
				.getRequestHandle());
		
		respHeader.setServiceResult(StatusCode.GOOD);
		respHeader.setServiceDiagnostics(buildDiagnostics());
		
		return respHeader;
	}
	
	/**
	 * common send response method
	 * @param req
	 * @param resp
	 */
	protected <T extends ServiceResponse> void sendResp(EndpointServiceRequest<? extends ServiceRequest, T> req, T resp){
		//TODO some logging here
		
		req.sendResponse(resp);
	}

	public void init(UAServer server){
		this.server = server;
	}
	
	protected SessionManager getSessionManager(){
		return server.getSessionManager();
	}
	
	protected AddressSpace getAddressSpace(){
		return server.getAddrSpace();
	}
}
