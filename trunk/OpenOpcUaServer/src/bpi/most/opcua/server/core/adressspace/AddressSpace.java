package bpi.most.opcua.server.core.adressspace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Node;
import org.opcfoundation.ua.core.ReferenceDescription;

import bpi.most.opcua.server.core.UAServerException;
import bpi.most.opcua.server.core.util.NodeUtils;
import bpi.most.opcua.server.core.util.Stopwatch;

/**
 * manages the address space of the underlying system.
 * 
 * @author harald
 *
 * TODO think of concurrency! (concurrent collections, not synchronized ones!)
 * TODO handle standard objects (server, session, types, ...)
 * 	ServerStatusDataType
 *
 */
public class AddressSpace {
	
	private static final Logger LOG = Logger.getLogger(AddressSpace.class);
	
	/**
	 * an insert-ordered map of all namespace (NS) nodeMgrs of the addressspace. key is the NS-index of the servers namespacearray,
	 * value is an {@link INodeManager}. the nodeMgrs are called
	 * on their insert order for initialization
	 */
	private Map<Integer, INodeManager> nodeMgrs = new LinkedHashMap<Integer, INodeManager>();
	
	public Node getNode(NodeId nodeId) throws UAServerException{
		//get the node from the managed nodes (the addressspace)
		
		Node node = null;
		
		int nsIndex = nodeId.getNamespaceIndex();
		if (nodeMgrs.containsKey(nsIndex)){
			LOG.debug("ns index for nodeid " + nodeId.toString() + " is " + nsIndex);
			node = nodeMgrs.get(nsIndex).getNode(nodeId);
		}
		
		return node;
	}
	
	public Node getNode(ExpandedNodeId expNodeId) throws UAServerException{
		return getNode(NodeUtils.toNodeId(expNodeId));
	}
	
	public List<ReferenceDescription> browseNode(NodeId nodeId){
		Stopwatch sw = new Stopwatch();
		sw.start();
		
		//check ns for nodeId
		List<ReferenceDescription> refDescs = new ArrayList<ReferenceDescription>();
		
		//collect all references for this node
		List<ReferenceDescription> temp = null;
		for (INodeManager nm: nodeMgrs.values()){
			try{
				temp = nm.getReferences(nodeId);
				
				//we do not trust INodeManager implementations here :)
				if (temp != null){
					LOG.debug("got " + temp.size() + " references from " + nm.getClass().getName());
					refDescs.addAll(temp);
				}
			}catch(UAServerException e){
				//here we catch the exception because we may be able to collect references from at least one nodemanager
				LOG.error(e.getMessage(), e);
			}
		}
	
		sw.stop();
		LOG.debug("---> browsing took " + sw.getDuration() + "ms"); 
		
		return refDescs;
	}
	
	public void addNodeManager(int nsIndex, INodeManager partition){
		if (nodeMgrs.containsKey(nsIndex)){
			//TODO throw new exception - not supported to change nodeMgrs for namespace
		}
		
		nodeMgrs.put(nsIndex, partition);
	}
	
	public INodeManager getNodeManagers(int nsIndex){
		return nodeMgrs.get(nsIndex);
	}
	
	public CoreNodeManager getCoreNodeManager(){
		return (CoreNodeManager) nodeMgrs.get(0);
	}

}
