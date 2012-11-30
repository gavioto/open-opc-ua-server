package bpi.most.opcua.server.handler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.core.AttributeServiceSetHandler;
import org.opcfoundation.ua.core.HistoryData;
import org.opcfoundation.ua.core.HistoryReadRequest;
import org.opcfoundation.ua.core.HistoryReadResponse;
import org.opcfoundation.ua.core.HistoryReadResult;
import org.opcfoundation.ua.core.HistoryReadValueId;
import org.opcfoundation.ua.core.HistoryUpdateRequest;
import org.opcfoundation.ua.core.HistoryUpdateResponse;
import org.opcfoundation.ua.core.Node;
import org.opcfoundation.ua.core.ReadAtTimeDetails;
import org.opcfoundation.ua.core.ReadEventDetails;
import org.opcfoundation.ua.core.ReadProcessedDetails;
import org.opcfoundation.ua.core.ReadRawModifiedDetails;
import org.opcfoundation.ua.core.ReadRequest;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.WriteRequest;
import org.opcfoundation.ua.core.WriteResponse;
import org.opcfoundation.ua.transport.EndpointServiceRequest;

import bpi.most.opcua.server.core.UAServerException;
import bpi.most.opcua.server.core.util.ArrayUtils;

public class AttributeServiceHandler extends ServiceHandlerBase implements AttributeServiceSetHandler {

	private static final Logger LOG = Logger
	.getLogger(AttributeServiceHandler.class);
	
	
	/**
	 * history read only for variables and properties -> historydatanodes
	 * 
	 */
	@Override
	public void onHistoryRead(
			EndpointServiceRequest<HistoryReadRequest, HistoryReadResponse> serviceReq)
			throws ServiceFaultException {
		HistoryReadRequest req = serviceReq.getRequest();
		HistoryReadResponse resp = new HistoryReadResponse();
		LOG.info("---------------  got history read request: ");
		
		for (HistoryReadValueId id: req.getNodesToRead()){
			LOG.debug("nodeid: " + id.getNodeId());
		}
		
		try {
			Object historyDetails = req.getHistoryReadDetails().decode();
			if (historyDetails instanceof ReadEventDetails){
				
			}else if (historyDetails instanceof ReadAtTimeDetails){
				
			}else if (historyDetails instanceof ReadProcessedDetails){
				
			}else if (historyDetails instanceof ReadRawModifiedDetails){
				ReadRawModifiedDetails rawModifiedDetails = (ReadRawModifiedDetails) historyDetails;
				LOG.debug(rawModifiedDetails);
				Date start = new Date(rawModifiedDetails.getStartTime().getTimeInMillis());
				Date end = new Date(rawModifiedDetails.getEndTime().getTimeInMillis());
				SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				LOG.debug("start: " + dFormat.format(start));
				LOG.debug("end: " + dFormat.format(end));
				
				HistoryReadResult historyResult = new HistoryReadResult();
				HistoryData data = new HistoryData();
				List<DataValue> values = new ArrayList<DataValue>();
				
				//lets mock some data
				Double[] mockData = new Double[]{1.12, 1.23, 1.55, 1.22, 1.65, 2.75, 2.03, 2.23, 2.11, 1.77, 1.23, 1.66};
				long diff = end.getTime() - start.getTime();
				for (int i = 0; i< mockData.length; i++){
					//create timestamps so that the datavalues are equally distributed over the requested time range
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(diff * i + start.getTime());
					DateTime dateTime = new DateTime(cal);
					values.add(new DataValue(new Variant(mockData[i]), StatusCode.GOOD, dateTime, dateTime));
				}
				
				data.setDataValues(ArrayUtils.toArray(values, DataValue.class));
				historyResult.setHistoryData(ExtensionObject.binaryEncode(data));
				
				resp.setResults(new HistoryReadResult[]{historyResult});
			}
			LOG.info(historyDetails.getClass().getName());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		
		resp.setResponseHeader(buildRespHeader(req));
		serviceReq.sendResponse(resp);
	}

	@Override
	public void onHistoryUpdate(
			EndpointServiceRequest<HistoryUpdateRequest, HistoryUpdateResponse> arg0)
			throws ServiceFaultException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRead(EndpointServiceRequest<ReadRequest, ReadResponse> serviceReq)
			throws ServiceFaultException {
		ReadRequest req = serviceReq.getRequest();
		ReadResponse resp = new ReadResponse();
		
		//contains all DataValues which are sent pack to the clients read request
		List<DataValue> dataValues = new ArrayList<DataValue>();
		
		/*
		 * a small temporary map for read nodes from the addressspace,
		 * because we may want to read different attributes from one node 
		 * and do not want to fetch the node again from the addressspace
		 */
		Map<NodeId, Node> readNodes = new HashMap<NodeId, Node>();
		
		//read all nodes the client wants
		for (ReadValueId readId: req.getNodesToRead()){
			LOG.debug("client sent read request. nodeid: " + readId.getNodeId() + "; attrId:" + readId.getAttributeId());
			//check temp map
			Node nodeToRead = readNodes.get(readId.getNodeId());
			if (nodeToRead == null){
				try {
					nodeToRead = getAddressSpace().getNode(readId.getNodeId());
				} catch (UAServerException e) {
					LOG.error(e.getMessage(), e);
					
					//set a bad datavalue for this node and get on with the next one
					dataValues.add(new DataValue(StatusCode.BAD));// buildDataValue(nodeToRead, readId.getAttributeId()));
					continue;
				}
				//read it from addressspace and store it in the map
				readNodes.put(readId.getNodeId(), nodeToRead);
			}
			
			if (nodeToRead == null){
				LOG.warn("did not find node with id " + readId.getNodeId());
			}
			
			dataValues.add(buildDataValue(nodeToRead, readId.getAttributeId()));
		}
		
		//build response and send it to the client
		resp.setResponseHeader(buildRespHeader(req));
		resp.setResults(dataValues.toArray(new DataValue[dataValues.size()]));
		serviceReq.sendResponse(resp);
	}
	
	/**
	 * builds a DataValue for the given {@link ReadValueId}.
	 * what the clients wants to read is defined by
	 * {@link ReadValueId#getNodeId()} and {@link ReadValueId#getAttributeId()}
	 * @param readId
	 * @return
	 */
	private DataValue buildDataValue(Node nodeToRead, UnsignedInteger attrId){
		DataValue val = new DataValue();
		
		if (nodeToRead != null){
			nodeToRead.readAttributeValue(attrId, val);
			
			val.setStatusCode(StatusCode.GOOD);
			val.setServerTimestamp(new DateTime());
			val.setSourceTimestamp(new DateTime());
		}else{
			val.setStatusCode(StatusCode.BAD);
		}
		
	//	LOG.debug("read value " + val.getValue().getValue());
		
		return val;
	}

	@Override
	public void onWrite(EndpointServiceRequest<WriteRequest, WriteResponse> arg0)
			throws ServiceFaultException {
		// TODO Auto-generated method stub

	}
}
