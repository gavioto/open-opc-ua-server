package bpi.most.opcua.example.basic.nodes;

import java.util.List;

import org.opcfoundation.ua.core.NodeClass;

import bpi.most.opcua.server.annotation.AnnotationNodeManager;
import bpi.most.opcua.server.annotation.Description;
import bpi.most.opcua.server.annotation.DisplayName;
import bpi.most.opcua.server.annotation.ID;
import bpi.most.opcua.server.annotation.UaNode;

@UaNode(nodeClass=NodeClass.Object)
public class Floor {

	@ID
	private int level;
	
	@DisplayName
	private String name;
	
	@Description
	private String description;
	
	private List<Room> rooms;

	/**
	 * empty constructor is mandatory when using {@link AnnotationNodeManager}
	 */
	public Floor() {
	}

	/**
	 * @param level
	 * @param name
	 * @param description
	 */
	public Floor(int level, String name, String description) {
		this.level = level;
		this.name = name;
		this.description = description;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the rooms
	 */
	public List<Room> getRooms() {
		return rooms;
	}

	/**
	 * @param rooms the rooms to set
	 */
	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}
	
}
