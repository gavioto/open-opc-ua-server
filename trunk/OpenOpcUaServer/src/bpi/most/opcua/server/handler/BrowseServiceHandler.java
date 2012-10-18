package bpi.most.opcua.server.handler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.core.AddNodesRequest;
import org.opcfoundation.ua.core.AddNodesResponse;
import org.opcfoundation.ua.core.AddReferencesRequest;
import org.opcfoundation.ua.core.AddReferencesResponse;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseNextRequest;
import org.opcfoundation.ua.core.BrowseNextResponse;
import org.opcfoundation.ua.core.BrowseRequest;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.BrowseResult;
import org.opcfoundation.ua.core.BrowseResultMask;
import org.opcfoundation.ua.core.DeleteNodesRequest;
import org.opcfoundation.ua.core.DeleteNodesResponse;
import org.opcfoundation.ua.core.DeleteReferencesRequest;
import org.opcfoundation.ua.core.DeleteReferencesResponse;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.NodeManagementServiceSetHandler;
import org.opcfoundation.ua.core.QueryFirstRequest;
import org.opcfoundation.ua.core.QueryFirstResponse;
import org.opcfoundation.ua.core.QueryNextRequest;
import org.opcfoundation.ua.core.QueryNextResponse;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.RegisterNodesRequest;
import org.opcfoundation.ua.core.RegisterNodesResponse;
import org.opcfoundation.ua.core.TranslateBrowsePathsToNodeIdsRequest;
import org.opcfoundation.ua.core.TranslateBrowsePathsToNodeIdsResponse;
import org.opcfoundation.ua.core.UnregisterNodesRequest;
import org.opcfoundation.ua.core.UnregisterNodesResponse;
import org.opcfoundation.ua.transport.EndpointServiceRequest;

import bpi.most.opcua.server.core.util.Stopwatch;

public class BrowseServiceHandler extends ServiceHandlerBase implements NodeManagementServiceSetHandler{

	private static long lastResponse = System.currentTimeMillis();
	
	private static final Logger LOG = Logger
			.getLogger(BrowseServiceHandler.class);
	
	@Override
	public void onAddNodes(
			EndpointServiceRequest<AddNodesRequest, AddNodesResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddReferences(
			EndpointServiceRequest<AddReferencesRequest, AddReferencesResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteNodes(
			EndpointServiceRequest<DeleteNodesRequest, DeleteNodesResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteReferences(
			EndpointServiceRequest<DeleteReferencesRequest, DeleteReferencesResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBrowse(
			EndpointServiceRequest<BrowseRequest, BrowseResponse> serviceReq)
			throws ServiceFaultException {
//		LOG.info("==> last browse-response sent " + (System.currentTimeMillis() - lastResponse) + "ms ago");
		
		Stopwatch sw = new Stopwatch();
		sw.start();
		
		BrowseRequest req = serviceReq.getRequest();
		BrowseResponse resp = new BrowseResponse();
		
		resp.setResponseHeader(buildRespHeader(req));
		
		//handle the request
	//	LOG.info("viewdescription: " + req.getView());
		
		//max references per node to return; 0 -> no limitation.
		int maxReferences = req.getRequestedMaxReferencesPerNode().intValue();
		
		LOG.info("clients sends browserequest for " + req.getNodesToBrowse().length + " nodes");
		List<BrowseResult> browseResults = new ArrayList<BrowseResult>();
		for (BrowseDescription browseDesc: req.getNodesToBrowse()){
			LOG.info("got browse request for nodeID " + browseDesc.getNodeId());
			browseResults.add(browse(browseDesc));
		}
		
		resp.setResults(browseResults.toArray(new BrowseResult[browseResults.size()]));
		
		sendResp(serviceReq, resp);
		
		sw.stop();
//		LOG.info("---> browseRequest took " + sw.getDuration() + "ms");
		lastResponse = System.currentTimeMillis();
	}
	
	private BrowseResult browse(BrowseDescription browseDesc){
		BrowseResult result = new BrowseResult();
		result.setStatusCode(StatusCode.GOOD);
		
		EnumSet<NodeClass> classMask = browseDesc.getNodeClassMask().intValue() == 0 ? NodeClass.ALL : NodeClass.getSet(browseDesc.getNodeClassMask());
		LOG.debug("classmask for browserequest: " + classMask.toString());
		
		//TODO consider NodeClass mask
		EnumSet<BrowseResultMask> resMask = BrowseResultMask.getSet(browseDesc.getResultMask());
		LOG.debug("resultmask for browserequest: " + resMask.toString());
		//TODO consider BrowseResultMask
		
		List<ReferenceDescription> allReferences = server.getAddrSpace().browseNode(browseDesc.getNodeId());
		
		//TODO create some filtermechanism which we can also use for classMask and resultMask
		List<ReferenceDescription> filteredReferences = new ArrayList<ReferenceDescription>();
		
		/**
		 * apply filtering of references, based on the request
		 */
		for (ReferenceDescription refDesc: allReferences){
			
			/*
			 * here we filter out the hastypedefinition reference (does not have to exist one), because its not relevant for the children.
			 * and the hastypedefinition is not built by an seperate reference, but is set in the ReferenceDescription#TypeDefinition property.
			 * 
			 */
//			if (!Identifiers.HasTypeDefinition.equals(refDesc.getReferenceTypeId())){
				
				if (!browseDesc.getBrowseDirection().equals(BrowseDirection.Both)){
					//consider borwsedirection
					boolean isForward = BrowseDirection.Forward.equals(browseDesc.getBrowseDirection());
						if (refDesc.getIsForward() == isForward){
							filteredReferences.add(refDesc);
						}
				}else{
					filteredReferences.add(refDesc);
				}
//			}
		}
		
		result.setReferences(filteredReferences.toArray(new ReferenceDescription[filteredReferences.size()]));
		return result;
	}

	@Override
	public void onBrowseNext(
			EndpointServiceRequest<BrowseNextRequest, BrowseNextResponse> serviceReq)
			throws ServiceFaultException {
		BrowseNextRequest req = serviceReq.getRequest();
		BrowseNextResponse resp = new BrowseNextResponse();
		
		LOG.info("---------------  got onbrowsenext request: " + req);
	}

	@Override
	public void onTranslateBrowsePathsToNodeIds(
			EndpointServiceRequest<TranslateBrowsePathsToNodeIdsRequest, TranslateBrowsePathsToNodeIdsResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRegisterNodes(
			EndpointServiceRequest<RegisterNodesRequest, RegisterNodesResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnregisterNodes(
			EndpointServiceRequest<UnregisterNodesRequest, UnregisterNodesResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQueryFirst(
			EndpointServiceRequest<QueryFirstRequest, QueryFirstResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQueryNext(
			EndpointServiceRequest<QueryNextRequest, QueryNextResponse> paramEndpointServiceRequest)
			throws ServiceFaultException {
		// TODO Auto-generated method stub
		
	}

}
