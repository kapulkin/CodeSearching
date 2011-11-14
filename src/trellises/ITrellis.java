package trellises;

public interface ITrellis {
	/**
	 * Возвращает колличество ярусов в решетке.
	 * @return колличество ярусов в решетке
	 */
	public int layersCount();
	/**
	 * Возвращает колличество вершин в ярусе <code>layer</code>.
	 * @param layer номер яруса
	 * @return колличество вершин в ярусе <code>layer</code>
	 */
	public long layerSize(int layer);
	
	public ITrellisIterator iterator(int layer, long vertexIndex);
}
