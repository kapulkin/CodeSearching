package trellises;

import java.util.NoSuchElementException;

public interface ITrellisIterator {
	public boolean hasForward();
	public boolean hasBackward();
	public void moveForward(int edgeIndex) throws NoSuchElementException;
	public void moveBackward(int edgeIndex) throws NoSuchElementException;

	public Trellis.Edge[] getAccessors();
	public Trellis.Edge[] getPredecessors();
	

	public int layer();
	public int vertexIndex();
	
	public ITrellisIterator clone();
}
