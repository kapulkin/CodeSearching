package trellises;

import java.util.NoSuchElementException;

public interface ITrellisIterator extends Cloneable {
	public boolean hasForward();
	public boolean hasBackward();
	public void moveForward(int edgeIndex) throws NoSuchElementException;
	public void moveBackward(int edgeIndex) throws NoSuchElementException;

	public ITrellisEdge[] getAccessors();
	public ITrellisEdge[] getPredecessors();
	

	public int layer();
	public long vertexIndex();
	
	public ITrellisIterator clone();
}
