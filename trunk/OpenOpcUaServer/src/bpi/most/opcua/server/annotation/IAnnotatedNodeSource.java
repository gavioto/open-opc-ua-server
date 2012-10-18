package bpi.most.opcua.server.annotation;

import java.util.List;

public interface IAnnotatedNodeSource {

	public Object getObjectById(String className, String id);
	public List<?> getTopLevelElements();
	public List<?> getChildren(String className, String parentId);
	
}
