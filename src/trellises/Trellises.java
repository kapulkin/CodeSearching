package trellises;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import trellises.Trellis.Vertex;

import math.BitArray;
import math.PolyMatrix;

public class Trellises {	
	
	public static class CosetTrellis implements ITrellis {
		private ITrellis codeTrellis;
		private BitArray cosetWord;
		
		public class Iterator implements ITrellisIterator {
			private ITrellisIterator iterator;
			private int position;
			
			private class EdgeWrapper implements ITrellisEdge {
				private ITrellisEdge edge;
				
				public EdgeWrapper(ITrellisEdge edge) {
					this.edge = edge;
				}
				
				@Override
				public long src() {					
					return edge.src();
				}

				@Override
				public long dst() {
					return edge.dst();
				}

				@Override
				public BitArray bits() {
					BitArray bits = edge.bits();
					
					bits.and(cosetWord.get(position, position + bits.getFixedSize()));
					return bits;
				}

				@Override
				public int metric(int i) {					
					return bits().cardinality();
				}

				@Override
				public int[] metrics() {					
					return new int[] { bits().cardinality() };
				}
				
			}
			
			public Iterator(ITrellisIterator iterator, int position) {
				this.iterator = iterator;
				this.position = position;
			}
			
			@Override
			public boolean hasForward() {				
				return iterator.hasForward();
			}

			@Override
			public boolean hasBackward() {				
				return iterator.hasBackward();
			}

			@Override
			public void moveForward(int edgeIndex)
					throws NoSuchElementException {
				position += iterator.getAccessors()[0].bits().getFixedSize();
				iterator.moveForward(edgeIndex);				
			}

			@Override
			public void moveBackward(int edgeIndex)
					throws NoSuchElementException {
				position -= iterator.getPredecessors()[0].bits().getFixedSize();
				iterator.moveBackward(edgeIndex);
			}

			@Override
			public ITrellisEdge[] getAccessors() {
				ITrellisEdge[] accessors = iterator.getAccessors();
				EdgeWrapper[] _accessors = new EdgeWrapper[accessors.length];
				
				for (int i = 0;i < accessors.length; ++i) {
					_accessors[i] = new EdgeWrapper(accessors[i]);					
				}
				
				return _accessors;
			}

			@Override
			public ITrellisEdge[] getPredecessors() {
				ITrellisEdge[] predecessors = iterator.getPredecessors();
				EdgeWrapper[] _predecessors = new EdgeWrapper[predecessors.length];
				
				for (int i = 0;i < predecessors.length; ++i) {
					_predecessors[i] = new EdgeWrapper(predecessors[i]);					
				}
				
				return _predecessors;
			}

			@Override
			public int layer() {				
				return iterator.layer();
			}

			@Override
			public long vertexIndex() { 
				return iterator.vertexIndex();
			}
			
			@Override
			public Iterator clone() {
				return new Iterator(iterator.clone(), position);
			}
			
		}
		
		public CosetTrellis(ITrellis codeTrellis, BitArray cosetWord) {
			this.codeTrellis = codeTrellis;
			this.cosetWord = cosetWord;
		}
		
		@Override
		public int layersCount() {			
			return codeTrellis.layersCount();
		}

		@Override
		public long layerSize(int layer) {			
			return codeTrellis.layerSize(layer);
		}

		@Override
		public ITrellisIterator iterator(int layer, long vertexIndex) {	
			int position = 0;
			
			for (int l = 0;l < layer; ++l) {
				position += codeTrellis.iterator(l, 0).getAccessors()[0].bits().getFixedSize();
			}
			
			return new Iterator(codeTrellis.iterator(layer, vertexIndex), position);
		}
		
	} 
	
	
	public static Trellis trellisFromParityCheckHR(PolyMatrix parityCheck)
	{
		int degree = parityCheck.get(0, parityCheck.getColumnCount() - 1).getDegree();
		int levels = parityCheck.getColumnCount() - 1;		
		boolean mergeLastLayers = true;
		
		if (parityCheck.get(0, parityCheck.getColumnCount() - 2).getDegree() == degree &&
				(parityCheck.getColumnCount() > 2 && parityCheck.get(0, parityCheck.getColumnCount() - 3).getDegree() == degree)) {
			++degree;
			levels += 2;
			mergeLastLayers = false;
		}
		
		ArrayList<Trellis.Vertex[]> layers = new ArrayList<Trellis.Vertex[]>();
		Trellis.Vertex[] firstLayer = new Trellis.Vertex[1<<degree];
	
		// индекс вершины определяет содержимое регистров памяти
		for(int v = 0;v < firstLayer.length;v ++)
		{
			firstLayer[v] = new Vertex();
			firstLayer[v].Accessors = new IntEdge[2];
			if (mergeLastLayers)
				firstLayer[v].Predecessors = new IntEdge[2];
			else
				firstLayer[v].Predecessors = new IntEdge[1];
		}
		layers.add(firstLayer);
		
		for(int l = 0;l < levels-1;l ++)
		{
			Trellis.Vertex[] lastLayer = layers.get(l);
			Trellis.Vertex[] newLayer = new Trellis.Vertex[1 << degree];
			
			for(int v = 0;v < newLayer.length;v ++)
			{
				newLayer[v] = new Vertex();
				if (l == levels - 3 && !mergeLastLayers)
					newLayer[v].Accessors = new IntEdge[1];
				else
					newLayer[v].Accessors = new IntEdge[2];
				
				if (l == levels - 2 && !mergeLastLayers)
					newLayer[v].Predecessors = new IntEdge[1];
				else
					newLayer[v].Predecessors = new IntEdge[2];
				
				// индекс ребра соответствует значению l-ого бита кодового слова.
				IntEdge edge0 = new IntEdge();
				IntEdge edge1 = new IntEdge();
				
				// при переходе из lastLayer по единичному ребру содержимое памяти изменилось в соотв. с H[l]
				// вычисляем маску изменения памяти
				int h = 0;
				for(int i = 0;i < parityCheck.get(0, l).getDegree() + 1;i ++)
				{
					if(parityCheck.get(0, l).getCoeff(i) == true)
					{
						h |= (1<<i);
					}
				}
				
				edge0.bits = new BitArray(1);
				edge0.bits.set(0, true);
				edge0.src = v ^ h;	// при переходе из lastLayer по нулевому ребру содержимое памяти не менялось
				edge0.dst = v;
				edge0.metrics = new int[0];
				
				newLayer[v].Predecessors[0] = edge0;				
				
				if(lastLayer[edge0.src].Accessors[0] == null)
				{
					lastLayer[edge0.src].Accessors[0] = edge0;
				}else{
					lastLayer[edge0.src].Accessors[1] = edge0;
				}
				
				if (l == levels - 2 && !mergeLastLayers) {					
					continue;
				}
				
				edge1.bits = new BitArray(1);				
				edge1.src = v;
				edge1.dst = v;
				edge1.metrics = new int[0];
				
				newLayer[v].Predecessors[1] = newLayer[v].Predecessors[0];
				newLayer[v].Predecessors[0] = edge1;
				
				if(lastLayer[edge1.src].Accessors[0] == null)
				{
					lastLayer[edge1.src].Accessors[0] = edge1;
				}else{
					lastLayer[edge1.src].Accessors[1] = lastLayer[edge1.src].Accessors[0];
					lastLayer[edge1.src].Accessors[0] = edge1;
				}
								
			}
			
			layers.add(newLayer);
		}
		
		Trellis.Vertex[] finalLayer = layers.get(levels - 1);
		
		// основное требование при переходе: младший регистр памяти должен стать равен нулю, т.к. это бит синдрома
		for(int v = 0;v < finalLayer.length;v ++)
		{	
			if (!mergeLastLayers) {
				firstLayer[v].Predecessors = finalLayer[v].Predecessors;
				continue;
			}
			
			int h1 = 0, h2 = 0;
			
			for(int i = 0;i < parityCheck.get(0, levels-1).getDegree() + 1;i ++)
			{
				if(parityCheck.get(0, levels-1).getCoeff(i) == true)
				{
					h1 |= (1<<i);
				}
			}
			
			for(int i = 0;i < parityCheck.get(0, levels).getDegree() + 1;i ++)
			{
				if(parityCheck.get(0, levels).getCoeff(i) == true)
				{
					h2 |= (1<<i);
				}
			}
			
			IntEdge edge0 = new IntEdge();
			IntEdge edge1 = new IntEdge();
			
			// нулевое ребро соотв. нулевому значению предпоследнего бита кодового слова. 
			edge0.bits = new BitArray(2);
			edge0.src = v;
			edge0.metrics = new int[0];
			
			if((v & 1) == 0)
			{	
				edge0.dst = (v >> 1);				
			}else{
				edge0.dst = ((v ^ h2) >> 1);
				edge0.bits.set(1, true);
			}
			
			finalLayer[v].Accessors[0] = edge0;				
			
			if(firstLayer[edge0.dst].Predecessors[0] == null)
			{
				firstLayer[edge0.dst].Predecessors[0] = edge0;
			}else{
				firstLayer[edge0.dst].Predecessors[1] = edge0;
			}			
			// единичное ребро соотв. единичному значению предпоследнего бита кодового слова. 
			edge1.bits = new BitArray(2);
			edge1.bits.set(0, true);
			edge1.src = v;
			edge1.metrics = new int[0];
			
			if(((v ^ h1) & 1) == 0)
			{	
				edge1.dst = ((v ^ h1) >> 1);				
			}else{
				edge1.dst = (((v ^ h1) ^ h2) >> 1);
				edge1.bits.set(1, true);
			}
			
			finalLayer[v].Accessors[1] = edge1;				
			
			if(firstLayer[edge1.dst].Predecessors[0] == null)
			{
				firstLayer[edge1.dst].Predecessors[0] = edge1;
			}else{
				firstLayer[edge1.dst].Predecessors[1] = edge1;
			}
		}
		
		if (!mergeLastLayers) {
			layers.remove(levels - 1);
		}
		
		Trellis trellis = new Trellis();
				
		trellis.Layers = new Trellis.Vertex[layers.size()][];
		layers.toArray(trellis.Layers);
		
		return trellis;
	}

	/**
	 * Создает решетку в явном виде, эквивалентную исходной. Если <code>trellis</code> - и есть решетка в явном виде, то возвращает ее.  
	 * @param trellis решетка
	 * @return <code>trellis</code>, если <code>trellis</code> - решетка в явном виде.
	 */
	public static Trellis getExplicitTrellisOf(ITrellis trellis) {
		if (trellis instanceof Trellis) {
			return (Trellis)trellis;
		}

		return buildExplicitTrellis(trellis);
	}
	
	/**
	 * Строит решетку в явном виде эквивалентную исходной. Входная решетка
	 * должна удовлетворять ограничениям создаваемой решетки класса Trellis. 
	 * @param trellis решетка кода.
	 * @return решетка кода, эквивалентная исходной.
	 */
	public static Trellis buildExplicitTrellis(ITrellis trellis) {
		Trellis newTrellis = new Trellis();

		newTrellis.Layers = new Vertex[trellis.layersCount()][];
		
		for (int layer = 0; layer < newTrellis.Layers.length; ++layer) {
			long layerSize = trellis.layerSize(layer);
			if (layerSize > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Trellis contains layers of length more, then " + Integer.MAX_VALUE);
			}
			newTrellis.Layers[layer] = new Vertex[(int)layerSize];
			for (int vertexIndex = 0; vertexIndex < newTrellis.Layers[layer].length; ++vertexIndex) {
				Vertex vertex = newTrellis.Layers[layer][vertexIndex] = new Vertex();
				
				ITrellisIterator iterator = trellis.iterator(layer, vertexIndex);
				ITrellisEdge accessors[] = iterator.getAccessors();
				vertex.Accessors = new IntEdge[accessors.length];
				for (int e = 0; e < accessors.length; ++e) {
					if (accessors[e].src() != vertexIndex) {
						throw new IllegalArgumentException("Wrong src index on the edge: " + layer + ", " + vertexIndex + ", " + e);
					}
					if (accessors[e].dst() >= Integer.MAX_VALUE || accessors[e].dst() < 0) {
						throw new IllegalArgumentException("A dst index on the edge is not inside [0, " + Integer.MAX_VALUE + "]:" +
								layer + ", " + vertexIndex + ", " + e);
					}

					vertex.Accessors[e] = new IntEdge(accessors[e]);
				}
				
				ITrellisEdge predecessors[] = iterator.getPredecessors();
				vertex.Predecessors = new IntEdge[predecessors.length];
				for (int i = 0; i < predecessors.length; ++i) {
					vertex.Predecessors[i] = new IntEdge(predecessors[i]);
				}
			}
		}
				
		return newTrellis;
	}
	
}
