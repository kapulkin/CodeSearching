package math;

/**
 * Матрица булевых значений
 * 
 * @author fedor
 *
 */
public class Matrix implements Cloneable {			
	
	private BitArray[] data;
	
	public Matrix(int m, int n)
	{
		data = new BitArray[m];
		
		for(int i = 0;i < m;i ++)
		{
			data[i] = new BitArray(n);			
		}
	}
	
	public Matrix(BitArray[] data)
	{
		this.data = data;
	}
	
	public int getRowCount()
	{
		return data.length;
	}
	
	public int getColumnCount()
	{
		return data[0].getFixedSize();
	}
	
	public boolean get(int i, int j)
	{
		return data[i].get(j);
	}
	
	public BitArray getRow(int i)
	{
		return data[i];
	}

	public Matrix getSubMatrix(int si, int sj, int rows, int cols)
	{
		Matrix subMat = new Matrix(rows, cols);
		
		for(int i = 0;i < rows;i ++)
		{
			for(int j = 0;j < cols;j ++)
			{
				subMat.set(i, j, get(si + i,sj + j));
			}
		}
		
		return subMat;
	}
	
	public void set(int i, int j, boolean value)
	{
		data[i].set(j, value);		
	}
	
	public void setIdentityBlock(int bi, int bj, int size)
	{
		for(int i = bi;i < bi + size;i ++)
		{
			for(int j = bj;j < bj + size;j ++)
			{
				if(i - bi == j - bj)
				{
					set(i, j, true);
				}else{
					set(i, j, false);
				}
			}
		}
	}
	
	public void setZeroBlock(int bi, int bj, int rows, int cols)
	{
		for(int i = bi;i < bi + rows;i ++)
		{
			for(int j = bj;j < bj + cols;j ++)
			{				
				set(i, j, false);				
			}
		}
	}
	
	public void setBlock(int bi, int bj, Matrix block)
	{
		for(int i = bi;i < bi + block.getRowCount();i ++)
		{
			for(int j = bj;j < bj + block.getColumnCount();j ++)
			{				
				set(i, j, block.get(i-bi, j-bj));				
			}
		}
	}
	
	public void setRow(int i, BitArray row)
	{
		data[i] = row;
	}
	
	public void add(Matrix mat)
	{		
		for(int i = 0;i < getRowCount();i ++)
		{			
			data[i].xor(mat.data[i]);
		}		
	}
	
	public Matrix mul(Matrix mat)
	{
		Matrix res = new Matrix(getRowCount(), mat.getColumnCount());
		
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				boolean val = false;
				
				for(int k = 0;k < getColumnCount();k ++)
				{
					val ^= data[i].get(k) & mat.data[k].get(j);
				}
				
				res.data[i].set(j, val);
			}
		}
		
		return res;
	}
	
	public BitArray mul(BitArray vec)
	{
		BitArray res = new BitArray(getRowCount());
		
		for(int i = 0;i < getRowCount();i ++)
		{
			boolean val = false;
			
			for(int j = 0;j < getColumnCount();j ++)
			{
				val ^= data[i].get(j) & vec.get(j);
			}
			
			res.set(i, val);
		}
		
		return res;
	}
	
	public Matrix Transpose()
	{
		Matrix tr = new Matrix(getColumnCount(), getRowCount());
		
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < getColumnCount();j ++)
			{
				tr.set(j, i, get(i, j));
			}
		}
		
		return tr;
	}
	
	public boolean isZero() {
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < getColumnCount();j ++)
			{
				if (get(i, j) == true) {
					return false;
				}
			}
		}
		return true;
	}
	
	public Object clone()
	{
		BitArray[] rowClones = new BitArray[getRowCount()];
		
		for(int i = 0;i < getRowCount();i ++)
		{
			rowClones[i] = (BitArray)data[i].clone();
		}
		
		return new Matrix(rowClones);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Matrix))
		    return false;
		if (this == obj)
		    return true;

		Matrix matrix = (Matrix) obj;
		
		if (matrix.getRowCount() != getRowCount() ||
				matrix.getColumnCount() != getColumnCount()) {
			return false;
		}
		
		for (int i = 0; i < getRowCount(); ++i) {
			if (!matrix.getRow(i).equals(getRow(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		String str = new String();
		
		for (int i = 0; i < getRowCount(); ++i) {
			for (int j = 0; j < getColumnCount(); ++j) {
				str += (get(i, j) ? "1" : "0"); 
			}
			
			str += "\n";
		}
		
		return str;
	}
}
