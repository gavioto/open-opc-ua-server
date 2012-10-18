package bpi.most.opcua.server.core.util;

import java.util.List;
import java.util.UUID;

import org.opcfoundation.ua.builtintypes.BuiltinsMap;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.Node;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.ReferenceNode;

public class NodeUtils {

	public static void addReferenceToNode(Node node, ReferenceNode refNode){
		ReferenceNode[] newRefNode = new ReferenceNode[]{refNode};
		if (node != null){
			if (node.getReferences() == null){
				node.setReferences(newRefNode);
			}else{
				node.setReferences(ArrayUtils.concat(node.getReferences(), newRefNode));
			}
		}
	}
	
	public static void addReferenceListToNode(Node node, List<ReferenceNode> refNodeList){
		ReferenceNode[] newRefNode = refNodeList.toArray(new ReferenceNode[refNodeList.size()]);
		if (node.getReferences() == null){
			node.setReferences(newRefNode);
		}else{
			node.setReferences(ArrayUtils.concat(node.getReferences(), newRefNode));
		}
	}
	
	public static boolean NodeIsValid(Node node) {
		return node != null && node.getNodeId() != null;
	}

	public static ReferenceDescription mapReferenceNodeToDesc(
			ReferenceNode refNode, Node referencedNode) {
		ReferenceDescription refDesc;

		refDesc = getRefDescForNode(referencedNode, refNode.getReferenceTypeId(), !refNode.getIsInverse());

		return refDesc;
	}
	
	public static ReferenceDescription getRefDescForNode(Node referencedNode, NodeId referenceType,  boolean isForward){
		ReferenceDescription refDesc = new ReferenceDescription();

		refDesc.setBrowseName(referencedNode.getBrowseName());
		refDesc.setDisplayName(referencedNode.getDisplayName());
		refDesc.setNodeClass(referencedNode.getNodeClass());
		refDesc.setReferenceTypeId(referenceType);
		refDesc.setTypeDefinition(getTypeDefinition(referencedNode));
		refDesc.setNodeId(toExpandedNodeId(referencedNode.getNodeId()));
		refDesc.setIsForward(isForward);
		
		return refDesc;
	}
	
	/**
	 * searches all references of the given {@link Node} to find the one from type
	 * hasTypeDefinition. the target ID of the first match is returned.
	 * @param node
	 * @return
	 */
	public static ExpandedNodeId getTypeDefinition(Node node){
		ExpandedNodeId typeDef = null;
		
		if (node != null && node.getReferences() != null){
			for (ReferenceNode refNode: node.getReferences()){
				if (!refNode.getIsInverse() && Identifiers.HasTypeDefinition.equals(refNode.getReferenceTypeId())){
					typeDef = refNode.getTargetId();
					break;
				}
			}
		}
		
		return typeDef;
	}
	
	public static ExpandedNodeId toExpandedNodeId(NodeId nodeId){
		return new ExpandedNodeId(nodeId);
	}

	public static NodeId toNodeId(ExpandedNodeId expNodeId) {
		NodeId nodeId = null;
		switch (expNodeId.getIdType()) {
		case Guid:
			nodeId = new NodeId(expNodeId.getNamespaceIndex(),
					(UUID) expNodeId.getValue());
			break;
		case Numeric:
			nodeId = new NodeId(expNodeId.getNamespaceIndex(),
					(UnsignedInteger) expNodeId.getValue());
			break;
		case Opaque:
			nodeId = new NodeId(expNodeId.getNamespaceIndex(),
					(byte[]) expNodeId.getValue());
			break;
		case String:
			nodeId = new NodeId(expNodeId.getNamespaceIndex(),
					(String) expNodeId.getValue());
			break;
		}

		return nodeId;
	}
	
	public static NodeClass getTypeClass(NodeClass nodeClass){
		NodeClass typeClass = null;
		
		if (NodeClass.Object.equals(nodeClass)){
			typeClass = NodeClass.ObjectType;
		}else if (NodeClass.Variable.equals(nodeClass)){
			typeClass = NodeClass.VariableType;
		}
		//TODO map the others
		
		return typeClass;
	}
	
	/**
	 * returns true if the given Object is from any builtin
	 * type. otherwhise false
	 * @param obj
	 * @return
	 */
	public static boolean isBuiltinType(Class<?> clazz){
		boolean isBuiltinType = false;
		
		isBuiltinType = BuiltinsMap.ID_CLASS_MAP.containsRight(clazz);
		
		return isBuiltinType;
	}
	
	
}
