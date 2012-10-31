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
import bpi.most.opcua.server.handler.referencefilter.IReferenceFilter;
import bpi.most.opcua.server.handler.referencefilter.RefDirectionFilter;
import bpi.most.opcua.server.handler.referencefilter.RefTypeFilter;
import bpi.most.opcua.server.handler.referencefilter.TargetNodeTypeFilter;

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
		//TODO consider this value. therefore continuation points have to be supported!! therefore we have to store the point in our sessioin --> session mgmt has to work better
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
	
	/**
	 * This method does browsing for one single node
	 * @param browseDesc
	 * @return
	 */
	private BrowseResult browse(BrowseDescription browseDesc){
		//fetch all references.
		List<ReferenceDescription> allReferences = server.getAddrSpace().browseNode(browseDesc.getNodeId());
		
		/*
		 * there are several filters which are be applied on the fetched references because the client may want to restrict them:
		 * 
		 * 1.) filter them by reference direction
		 * 2.) filter them by the targets Node's NodeClass
		 * 3.) filter them by reference type
		 * 
		 * we do the filtering here because its a central point and so not every nodemanager has to deal with the filtering
		 */
		List<IReferenceFilter> filters = new ArrayList<IReferenceFilter>();
		filters.add(new RefDirectionFilter());
		filters.add(new TargetNodeTypeFilter());
		filters.add(new RefTypeFilter());
		
		//apply every filter
		List<ReferenceDescription> filteredReferences = allReferences;
		for (IReferenceFilter filter: filters){
			filteredReferences = filter.filter(filteredReferences, browseDesc);
		}
		
		//filter fields of the remaining referenceDescriptions to match the clients request. he may want just a view fields set
		EnumSet<BrowseResultMask> resMask = BrowseResultMask.getSet(browseDesc.getResultMask());
//		LOG.debug("resultmask for browserequest: " + resMask.toString());
		List<ReferenceDescription> resultingDescriptions = new ArrayList<ReferenceDescription>();
		for (ReferenceDescription refDesc: filteredReferences){
			resultingDescriptions.add(filterRefDescFields(refDesc, resMask));
		}
		
		//create the result
		BrowseResult result = new BrowseResult();
		result.setStatusCode(StatusCode.GOOD);
		result.setReferences(filteredReferences.toArray(new ReferenceDescription[resultingDescriptions.size()]));
		return result;
	}
	
	/**
	 * the client can decide which fields he want to be set in the resulting {@link ReferenceDescription}. This is
	 * done by a {@link BrowseResultMask}. This method sets only those fields, the client requested with its sent
	 * {@link BrowseResultMask}.
	 * @param refDescToFilter
	 * @return
	 */
	private ReferenceDescription filterRefDescFields(ReferenceDescription refDescToFilter, EnumSet<BrowseResultMask> mask){
		ReferenceDescription result;
		
		if (mask.contains(BrowseResultMask.All)){
			//return all fields
			result = refDescToFilter;
		}else{
			result = new ReferenceDescription();
			result.setNodeId(refDescToFilter.getNodeId());
			
			//only return requested fields
			if (mask.contains(BrowseResultMask.ReferenceTypeId)){
				result.setReferenceTypeId(refDescToFilter.getReferenceTypeId());
			}
			if (mask.contains(BrowseResultMask.IsForward)){
				result.setIsForward(refDescToFilter.getIsForward());
			}
			if (mask.contains(BrowseResultMask.NodeClass)){
				result.setNodeClass(refDescToFilter.getNodeClass());
			}
			if (mask.contains(BrowseResultMask.BrowseName)){
				result.setBrowseName(refDescToFilter.getBrowseName());
			}
			if (mask.contains(BrowseResultMask.DisplayName)){
				result.setDisplayName(refDescToFilter.getDisplayName());
			}
			if (mask.contains(BrowseResultMask.TypeDefinition)){
				result.setTypeDefinition(refDescToFilter.getTypeDefinition());
			}
		}
		
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
