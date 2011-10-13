package math;

import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;

public class ArrayTree<E extends Comparable<E>> implements RandomAccess, Iterable<E>, Collection<E> {
	static class TreeItem<E> {
		public TreeItem(E data) {
			this.data = data;
			leftCount = rightCount = 0;
			left = right = null;
		}
		
		public TreeItem(E data, TreeItem<E> left, TreeItem<E> right) {
			this.data = data;
			this.left = left;
			this.leftCount = left.leftCount + left.rightCount + 1;
			this.right = right;
			this.rightCount = right.leftCount + right.rightCount + 1;
		}

		E data;
		int leftCount, rightCount;
		TreeItem<E> left, right;
	}

	static class TreeIterator<E> implements Iterator<E> {
		private E[] array;
		private int index;
		
		private TreeIterator(E[] array) {
			this.array = array;
			index = 0;
		}
		
		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public E next() {
			return array[index++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	TreeItem<E> root;
	
	public ArrayTree() {
		root = null;
	}

	public ArrayTree(Iterable<? extends E> collection) {
		addAll(collection);
	}
	
	@Override
	public boolean add(E data) {
		if (root == null) {
			root = new TreeItem<E>(data);
			return true;
		}
		
		return tryAdd(root, new TreeItem<E>(data));
	}

	private boolean tryAdd(TreeItem<E> parent, TreeItem<E> item) {
		int cmp = parent.data.compareTo(item.data);
		
		if (cmp > 0) {
			if (parent.right == null) {
				parent.right = item;
				parent.rightCount = item.leftCount + item.rightCount + 1;
				return true;
			} else {
				boolean changed = tryAdd(parent.right, item);
				if (changed) {
					parent.rightCount += item.leftCount + item.rightCount + 1;
				}
				return changed;
			}
		}
			
		if (cmp < 0) {
			if (parent.left == null) {
				parent.left = item;
				parent.leftCount = item.leftCount + item.rightCount + 1;
				return true;
			} else {
				boolean changed = tryAdd(parent.left, item);
				if (changed) {
					parent.leftCount += item.leftCount + item.rightCount + 1;
				}
				return changed;
			}
		}

		// cmp == 0
		return false;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> collection) {
		Iterable<? extends E> iterable = (Iterable<? extends E>)collection;
		return addAll(iterable);
	}
	
	public boolean addAll(Iterable<? extends E> collection) {
		boolean changed = false;
		for (E data : collection) {
			changed |= add(data);
		}
		
		return changed;
	}

	@Override
	public void clear() {
		root = null;
	}

	@Override
	public boolean contains(Object object) {
		E data = (E)object;
		
		if (object != null && data == null) {
			throw new ClassCastException();
		}
		
		if (root == null) {
			return false;
		}
		
		return find(root, data) != null;
	}
	
	private TreeItem<E> find(TreeItem<E> parent, E data) {
		int cmp = parent.data.compareTo(data);

		if (cmp > 0) {
			if (parent.right == null) {
				return null;
			} else {
				return find(parent.right, data);
			}
		}
		
		if (cmp < 0) {
			if (parent.left == null) {
				return null;
			} else {
				return find(parent.left, data);
			}
		}

		// cmp == 0
		return parent;
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		for (Object object : collection) {
			if (!contains(object)) {
				return false;
			}
		} 
		return true;
	}

	@Override
	public boolean isEmpty() {
		return root == null;
	}

	@Override
	public boolean remove(Object object) {
		if (!contains(object)) {
			return false;
		}
		
		E data = (E)object;
		
		//TODO:
		// найти вершину, содержащую data, и ее родиителя.
		// разобраться с детьми вершины:
		// если детей нет - все OK
		// если ребенок один - вешаем поддерево не место текущей
		// если детей два - вешаем левое поддерево на место текщей, правое - к наибольшей вершине левого поддерева
		
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		for (Object object : collection) {
			remove(object);
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		for (Object object : this) {
			if (!collection.contains(object)) {
				remove(object);
			}
		}
		return false;
	}

	@Override
	public int size() {
		return isEmpty() ? 0 : root.leftCount + root.rightCount + 1;
	}

	@Override
	public Object[] toArray() {
		Object array[] = new Object[size()];
		putToArray(root, array, 0);
		return array;
	}
	
	private void putToArray(TreeItem<E> parent, Object array[], int index) {
		if (parent.left != null) {
			putToArray(parent.left, array, index);
		}
		array[index + parent.leftCount] = parent.data;
		if (parent.right != null) {
			putToArray(parent.right, array, index + parent.leftCount + 1);
		}
	}

	@Override
	public <T> T[] toArray(T[] array) {
		T[] my_array = (array.length >= size()) ? array : (T[])new Object[size()];
		putToArray(root, my_array, 0);
		return array;
	}

	@Override
	public Iterator<E> iterator() {
		E[] array = (E[])new Object[size()];
		return new TreeIterator<E>(toArray(array));
	}

}
