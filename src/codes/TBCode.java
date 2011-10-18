package codes;

import math.ConvCodeAlgs;
import math.MinDistance;
import math.PolyMatrix;
import trellises.BlockCodeTrellis;
import trellises.ITrellis;
import trellises.Trellis;
import trellises.Trellises;

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
	public ITrellis getTrellis() {
		if (trellis == null) {
			PolyMatrix minBaseGen = ConvCodeAlgs.getMinimalBaseGenerator(parentCode.generator());
			
			Trellis explicit_trellis = ConvCodeAlgs.buildTrellis(ConvCodeAlgs.buildSpanForm(minBaseGen));
			MinDistance.computeDistanceMetrics(explicit_trellis);
			trellis = explicit_trellis;
		}
		
		return trellis;
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
