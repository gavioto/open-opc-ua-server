package bpi.most.opcua.server.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.core.NodeClass;

/**
 * contains fields which are used to create node instances
 * for a specific java-class.
 * 
 * @author harald
 *
 */
public class NodeMapping {

	/**
	 * java class this node mapping is associated to
	 */
	private String javaClassName;

	/**
	 * NodeClass which should be used when creating nodes from this NodeMapping
	 */
	private NodeClass nodeClass;
	
	private Field idField;
	private Field displNameField;
	private Field descField;
	
	/**
	 * type definition created for this nodemapping (and the underlying javaClass).S
	 */
	private ExpandedNodeId typeDefinition;
	
	/**
	 * field which contains the variables value. this is only used for Variable-Nodes.
	 * one variable-node can only have one value
	 */
	private Field valueField;

	private Map<String, ReferenceMapping> referencesByName;

	/**
	 * 
	 */
	public NodeMapping() {
	}

	/**
	 * @param javaClassName
	 * @param nodeClass
	 * @param idField
	 * @param displNameField
	 * @param descField
	 * @param propertyFields
	 */
	public NodeMapping(String javaClassName, NodeClass nodeClass,
			Field idField, Field displNameField, Field descField,
			Map<String, ReferenceMapping> referencesByName) {
		this.javaClassName = javaClassName;
		this.nodeClass = nodeClass;
		this.idField = idField;
		this.displNameField = displNameField;
		this.descField = descField;
		this.referencesByName = referencesByName != null ? referencesByName : new HashMap<String, ReferenceMapping>();
	}

	/**
	 * @return the javaClassName
	 */
	public String getJavaClassName() {
		return javaClassName;
	}

	/**
	 * @param javaClassName the javaClassName to set
	 */
	public void setJavaClassName(String javaClassName) {
		this.javaClassName = javaClassName;
	}

	/**
	 * @return the idField
	 */
	public Field getIdField() {
		return idField;
	}

	/**
	 * @param idField the idField to set
	 */
	public void setIdField(Field idField) {
		this.idField = idField;
	}

	/**
	 * @return the displNameField
	 */
	public Field getDisplNameField() {
		return displNameField;
	}

	/**
	 * @param displNameField the displNameField to set
	 */
	public void setDisplNameField(Field displNameField) {
		this.displNameField = displNameField;
	}

	/**
	 * @return the descField
	 */
	public Field getDescField() {
		return descField;
	}

	/**
	 * @param descField the descField to set
	 */
	public void setDescField(Field descField) {
		this.descField = descField;
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
	/**
	 * @return the referencesByName
	 */
	public Map<String, ReferenceMapping> getReferencesByName() {
		return referencesByName;
	}

	/**
	 * @param referencesByName the referencesByName to set
	 */
	public void setReferencesByName(Map<String, ReferenceMapping> referencesByName) {
		this.referencesByName = referencesByName;
	}
	
	/**
	 * @return the valueField
	 */
	public Field getValueField() {
		return valueField;
	}

	/**
	 * @param valueField the valueField to set
	 */
	public void setValueField(Field valueField) {
		this.valueField = valueField;
	}
	
	/**
	 * @return the typeDefinition
	 */
	public ExpandedNodeId getTypeDefinition() {
		return typeDefinition;
	}

	/**
	 * @param typeDefinition the typeDefinition to set
	 */
	public void setTypeDefinition(ExpandedNodeId typeDefinition) {
		this.typeDefinition = typeDefinition;
	}

	/**
	 * reads the value of the field in the given obj
	 * @param f
	 * @param obj
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private Object readValue(Field field, Object obj) throws IllegalArgumentException, IllegalAccessException{
		field.setAccessible(true);
		return field.get(obj);
	}
	
	public Object readIdField(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return readValue(idField, obj);
	}
	
	public String readDisplNameField(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return readValue(displNameField, obj).toString();
	}
	
	public String readDescField(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return readValue(descField, obj).toString();
	}
	
	public Object readValueField(Object obj) throws IllegalArgumentException, IllegalAccessException{
		return readValue(valueField, obj).toString();
	}
	
	public Object readProperty(String name, Object obj) throws IllegalArgumentException, IllegalAccessException{
		return readValue(referencesByName.get(name).getField(), obj);
	}
	
	public String getFieldTypeName(String name){
		return referencesByName.get(name).getField().getClass().getName();
	}
	
	/**
	 * reads the value from the given Objects field with the given name. if the field is of an built-in type,
	 * the value is read directly.
	 * <br/><br/>
	 * the field can also be some custom object, where a single property of the custom object has to be
	 * annotated with {@link Value}. the value of the {@link Value}-annotated field in the custom object is returned
	 * in that case.
	 * @param name
	 * @param obj
	 * @return
	 */
	public Object readValue(String name, Object obj){
		return null;
	}
	
	/**
	 * returns the type of the referenced Node identified by the given name
	 * @param name
	 * @return
	 */
	public Class<?> getReferencedDataType(String name){
		Class<?> type = null;
		Field field = referencesByName.get(name).getField();
		if (field != null){
			type = field.getType();
		}
		return type;
	}
	
	/**
	 * returns an ReferenceMapping for a given fieldName. The
	 * field should be annotated with any annotation which defines
	 * references like @Property or @Reference
	 * @param fieldName
	 * @return
	 */
	public ReferenceMapping getReferenceByName(String fieldName){
		return referencesByName.get(fieldName);
	}
	
	
}
