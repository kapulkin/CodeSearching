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
	public int layerSize(int layer);
	
	public TrellisIterator iterator(int layer, int vertexIndex);
}