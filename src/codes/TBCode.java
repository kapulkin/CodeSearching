package codes;

import math.ConvCodeAlgs;
import math.MinDistance;
import math.PolyMatrix;
import trellises.Trellis;

/**
 * Tailbiting код. Усеченый код при L0 = 0.
 * @author fedor
 *
 */
public class TBCode extends TruncatedCode {	
	/**
	 * Построение тайлбитингого кода по сверточному
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
	public Trellis getTrellis()
	{	
		if(trellis != null) {
			return trellis;
		}
		
		PolyMatrix minBaseGen = ConvCodeAlgs.getMinimalBaseGenerator(parentCode.generator());
		Trellis trellis = ConvCodeAlgs.buildTrellis(ConvCodeAlgs.buildSpanForm(minBaseGen));
		
		/*Trellis segment = Trellises.trellisSegmentFromGenSF(getGeneratorSpanForm(), 0, parentCode.getK());
		
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
		}/**/		
		
		return trellis;
	}
	
	public int getMinDist()
	{
		if(minDist != -1)
			return minDist;
		
		return getMinDistByTrellis();
	}
	
	/**
	 * 
	 * @return Минимальное расстояние кода
	 */
	public int getMinDistByTrellis()
	{
		if(minDist != -1)
		{
			return minDist;
		}
		
		Trellis t = getTrellis();
		
		MinDistance.computeDistanceMetrics(t);
		minDist = MinDistance.findMinDistWithVA(t, 0, blockGenMatr.getColumnCount(), false);
		
		return minDist;
	}
	
}
