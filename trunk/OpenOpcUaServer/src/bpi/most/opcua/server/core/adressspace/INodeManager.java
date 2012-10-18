package bpi.most.opcua.server.core.adressspace;

import java.util.List;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Node;
import org.opcfoundation.ua.core.ReferenceDescription;

import bpi.most.opcua.server.core.UAServerException;

/**
 * Every {@link INodeManager} is responsible for all nodes
 * belonging to ONE specific namespace
 * 
 * TODO:
 * to not block the client, we could  implement a timeout when calling
 * getNode or getReferences on the implementation of this 
 * interface. something like this:
 * <pre>

ExecutorService executor = Executors.newCachedThreadPool();
Callable<Object> task = new Callable<Object>() {
   public Object call() {
      return something.blockingMethod();
   }
};
Future<Object> future = executor.submit(task);
try {
   Object result = future.get(5, TimeUnit.SECONDS); 
} catch (TimeoutException ex) {
   // handle the timeout
} catch (InterruptedException e) {
   // handle the interrupts
} catch (ExecutionException e) {
   // handle other exceptions
} finally {
   future.cancel(); // may or may not desire this
}
</pre>
 * 
 * @author harald
 *
 */
public interface INodeManager {

	public void init(AddressSpace addrSpace, int nsIndex);
	
	public Node getNode(NodeId nodeId) throws UAServerException;
	
	public List<ReferenceDescription> getReferences(NodeId nodeId) throws UAServerException;
	
	public DataValue readNodeAttribute(NodeId nodeId, UnsignedInteger attrId);
	
	public void addNode(Node node, NodeId parentNode, NodeId referenceId);
}
