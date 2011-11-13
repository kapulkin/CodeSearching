package trellises;

public class TailbitingCodeTrellis implements ITrellis {
	ITrellis trellis;
	int cycles;

	public TailbitingCodeTrellis(ConvCodeTrellis trellis, int cycles) {
		this((ITrellis)trellis, cycles);
	}
	
	/**
	 * Создает решетку tailbiting кода на лету на основе решетки сверточного кода. Входная решетка <code>trellis</code> должна быть циклической.
	 * @param trellis решетка сверточного кода.
	 * @param cycles колличество циклов, которое можно пройти по решетке.
	 */
	public TailbitingCodeTrellis(ITrellis trellis, int cycles) {
		this.trellis = trellis;
		this.cycles = cycles;
	}
	
	@Override
	public int layersCount() {
		return trellis.layersCount() * cycles + 1;
	}

	@Override
	public long layerSize(int layer) {
		if (layer >= layersCount()) {
			throw new IndexOutOfBoundsException("layer = " + layer);
		}
		return trellis.layerSize(layer % trellis.layersCount());
	}

	@Override
	public ITrellisIterator iterator(int layer, long vertexIndex) {
		return new TBCodeTrellisIterator(trellis, cycles, layer, vertexIndex);
	}

}
