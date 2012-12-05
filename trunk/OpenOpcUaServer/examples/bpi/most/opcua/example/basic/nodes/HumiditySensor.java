package bpi.most.opcua.example.basic.nodes;

import org.opcfoundation.ua.core.NodeClass;

import bpi.most.opcua.server.annotation.AnnotationNodeManager;
import bpi.most.opcua.server.annotation.DisplayName;
import bpi.most.opcua.server.annotation.HistoryRead;
import bpi.most.opcua.server.annotation.Property;
import bpi.most.opcua.server.annotation.UaNode;
import bpi.most.opcua.server.annotation.Value;

@UaNode(nodeClass = NodeClass.Variable)
public class HumiditySensor{

	public static final String HISTORY = "humHistory";
	
	/**
	 * current value of the sensor
	 */
	@Value
	@HistoryRead(qualifier=HISTORY)
	private Double value;
	
	@DisplayName
	private String displName = "Relative Humidity";

	@Property
	private String unit = "value between 0 and 1";

	/**
	 * empty constructor is mandatory when using {@link AnnotationNodeManager}
	 */
	public HumiditySensor(){
		
	}
	
	/**
	 * 
	 */
	public HumiditySensor(Double value) {
		this.value = value;
	}
	
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the displName
	 */
	public String getDisplName() {
		return displName;
	}

	/**
	 * @param displName the displName to set
	 */
	public void setDisplName(String displName) {
		this.displName = displName;
	}

	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}
}
