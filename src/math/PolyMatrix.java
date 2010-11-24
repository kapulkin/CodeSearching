package math;

public class PolyMatrix {

	private Poly[][] data;
	
	public PolyMatrix(int m, int n)
	{
		data = new Poly[m][n];
		
		for(int i = 0;i < m;i ++)
		{
			for(int j = 0;j < n;j ++)
			{
				data[i][j] = new Poly();
			}
		}
	}
	
	public int getRowCount()
	{
		return data.length;
	}
	
	public int getColumnCount()
	{
		return data[0].length;
	}
	
	public void set(int i, int j, Poly p)
	{
		data[i][j] = p;
	}
	
	public Poly get(int i, int j)
	{
		return data[i][j];
	}
	
	public void add(PolyMatrix mat)
	{
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < getColumnCount();j ++)
			{
				data[i][j] = data[i][j].sum(mat.get(i, j)); 
			}
		}
	}
	
	public PolyMatrix mul(PolyMatrix mat)
	{
		PolyMatrix res = new PolyMatrix(getRowCount(), mat.getColumnCount());
		
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				Poly val = new Poly();
				
				for(int k = 0;k < getColumnCount();k ++)
				{
					val = val.sum(get(i, k).mul(mat.get(k, j)));
				}
				
				res.set(i, j, val);
			}
		}
		
		return res;
	}
	
	public PolyMatrix clone()
	{
		PolyMatrix clon = new PolyMatrix(getRowCount(), getColumnCount());
		
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < getColumnCount();j ++)
			{
				clon.set(i, j, get(i, j).clone());
			}
		}
		
		return clon;
	}
	
	public static PolyMatrix getIdentity(int size)
	{
		PolyMatrix e = new PolyMatrix(size, size);
		
		for(int i = 0;i < size;i ++)
		{
			e.set(i, i, new Poly(new Boolean[]{true}));
		}
		
		return e;
	}
}
