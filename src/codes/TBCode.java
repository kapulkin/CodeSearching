package codes;

import math.ConvCodeAlgs;
import math.MinDistance;
import trellises.ITrellis;
import trellises.TailbitingCodeTrellis;
import trellises.Trellis;

/**
 * Tailbiting код. Усеченый код при L0 = 0.
 * @author fedor
 *
 */
public class TBCode extends TruncatedCode {	
	/**
	 * Построение тайлбитингового кода по сверточному
	 * @param code сверточный код
	 * @param scaleDelta определяет параметры TB кода: k=(delay+1+scaleDelta)*b, n=(delay+1+scaleDelta)*c, где 
	 * b/c - скорость, а delay - задержка сверточного кода  	 
	 */
	public TBCode(ConvCode code, int scaleDelta) {
		super(code, 0, scaleDelta);
	}
	
	/**
	 * 
	 * @return Тайлбитинговая решетка кода
	 */
	public ITrellis getTrellis() {
		if (trellis == null) {
			trellis = new TailbitingCodeTrellis(parentCode.getTrellis(), cycles);
		}
		
		return trellis;
	}
	
	public int getMinDist() {
		if (minDist == -1) {
			minDist = MinDistance.findMinDist(this);
		}
		
		return minDist;
	}

	/*public Trellis getTrellis()
	{	
		if(trellis != null) {
			return trellis;
		}
		
		Trellis segment = Trellises.trellisSegmentFromGenSF(getGeneratorSpanForm(), 0, parentCode.getK());
		
		Trellis.Vertex[] firstLayer = segment.Layers[0];
		Trellis.Vertex[] lastLayer = segment.Layers[segment.Layers.length-1];
		
		for(int i = 0;i < firstLayer.length;i ++)
		{
			firstLayer[i].Predecessors = lastLayer[i].Predecessors;
		}		
		
		trellis = new Trellis();
		trellis.Layers = new Trellis.Vertex[segment.Layers.length-1][];
		for(int i = 0;i < trellis.Layers.length;i ++)
		{
			trellis.Layers[i] = segment.Layers[i]; 
		}		
		
		return trellis;
	}/**/
}
