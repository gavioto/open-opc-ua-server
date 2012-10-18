package bpi.most.opcua.server.annotation;

import java.lang.reflect.Field;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.NodeClass;

/**
 * @author harald
 *
 */
public class ReferenceMapping {

	/**
	 * id of the referencetype. like hasProperty or hasComponent
	 */
	private NodeId referenceType;
	
	/**
	 * field in the containing object. this is used to get
	 * its value
	 */
	private Field field;
	
	/**
	 * description for the referenced node
	 */
	private String description;
	
	/**
	 * datatype of the target node
	 */
	private NodeId datatype;
	
	/**
	 * nodeclass of the target node
	 */
	private NodeClass nodeClass;

	/**
	 * @return the referenceType
	 */
	public NodeId getReferenceType() {
		return referenceType;
	}

	/**
	 * @param referenceType the referenceType to set
	 */
	public void setReferenceType(NodeId referenceType) {
		this.referenceType = referenceType;
	}

	/**
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(Field field) {
		this.field = field;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the datatype
	 */
	public NodeId getDatatype() {
		return datatype;
	}

	/**
	 * @param datatype the datatype to set
	 */
	public void setDatatype(NodeId datatype) {
		this.datatype = datatype;
	}
	
	public String getFieldName(){
		return field.getName();
	}

	/**
	 * @return the nodeClass
	 */
	public NodeClass getNodeClass() {
		return nodeClass;
	}

	/**
	 * @param nodeClass the nodeClass to set
	 */
	public void setNodeClass(NodeClass nodeClass) {
		this.nodeClass = nodeClass;
	}
	
}
