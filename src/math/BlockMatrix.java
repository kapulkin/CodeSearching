package math;

/**
 * Матрица битовых блоков размером vSize на hSize   
 * 
 * @author fedor
 *
 */
public class BlockMatrix{
		
	private Matrix[][] blocks;
	private int vSize;
	private int hSize;
	
	public BlockMatrix(int m, int n)
	{		
		vSize = 0;
		hSize = 0;
		blocks = new Matrix[m][n];		
	}
	
	/**
	 * Переводит матрицу полиномов в матрицу блоков размером 
	 * 1 x <code>cellWidth</code>. Каждый блок матрицы представляет собой 
	 * <code>cellWidth</code> коэффициентов соотвествующего полинома.
	 * @param matr матрица полиномов
	 * @param cellWidth ограничение степени
	 */
	public BlockMatrix(PolyMatrix matr, int cellWidth)
	{
		vSize = 1;
		hSize = cellWidth;
		
		blocks = new Matrix[matr.getRowCount()][matr.getColumnCount()];
		for(int i = 0;i < matr.getRowCount();i ++)
		{
			for(int j = 0;j < matr.getColumnCount();j ++)
			{
				Matrix cell = new Matrix(1, cellWidth);
				
				for(int k = 0;k < cellWidth;k ++)
				{
					cell.set(0, k, matr.get(i, j).getCoeff(k));
				}
				
				blocks[i][j] = cell;
			}
		}
	}
	
	public BlockMatrix(Matrix[] row)
	{
		vSize = row[0].getRowCount();
		hSize = row[0].getColumnCount();		
		
		blocks = new Matrix[1][row.length];
		blocks[0] = row;
	}
	
	public BlockMatrix(Matrix mat, int bh, int bw)
	{
		int m = mat.getRowCount() / bh, n = mat.getColumnCount() / bw;
				
		vSize = bh;
		hSize = bw;
		blocks = new Matrix[m][n];
		
		for(int bi = 0;bi < m;bi ++)
		{
			for(int bj = 0;bj < n;bj ++)
			{
				blocks[bi][bj] = mat.getSubMatrix(bi * bh, bj * bw, bh, bw);
			}
		}
	}
	
	public int getVerticalSize()
	{
		return vSize;
	}
	
	public int getHorizontalSize()
	{
		return hSize;
	}
	
	public int getTotalRowCount()
	{		
		return vSize * getRowCount();
	}
	
	public int getTotalColumnCount()
	{		
		return hSize * getColumnCount();
	}
	
	public int getRowCount()
	{
		return blocks.length;
	}
	
	public int getColumnCount()
	{
		return blocks[0].length;
	}
	
	public Matrix get(int i, int j)
	{
		return blocks[i][j];
	}
	
	public void set(int i, int j, Matrix value)
	{
		blocks[i][j] = value;
		
		if(value != null)
		{
			if(vSize == 0)
			{
				vSize =  value.getRowCount();
			}
			
			if(hSize == 0)
			{
				hSize = value.getColumnCount();				
			}
		}
	}
	
	public Matrix breakBlockStructure()
	{
		Matrix unblocked = new Matrix(getTotalRowCount(), getTotalColumnCount());		
		int rowPos = 0;
		
		for(int i = 0;i < getRowCount();i ++)
		{
			int colPos = 0;
			for(int j = 0;j < getColumnCount();j ++)
			{
				if(blocks[i][j] == null)
				{
					unblocked.setZeroBlock(rowPos, colPos, vSize, hSize);
				}else{
					unblocked.setBlock(rowPos, colPos, blocks[i][j]);
				}
				colPos += hSize;
			}
			rowPos += vSize;
		}
		
		return unblocked; 
	}
	
	public void add(BlockMatrix mat)
	{		
		for(int i = 0;i < getRowCount();i ++)
		{			
			for(int j = 0;j < getColumnCount();j ++)
			{
				if(mat.blocks[i][j] != null)
				{
					if(blocks[i][j] == null)
					{
						blocks[i][j] = mat.blocks[i][j];						
					}
					else{
						blocks[i][j].add(mat.blocks[i][j]);
					}
				}
			}
		}		
	}
	
	/*public BlockMatrix mul(BlockMatrix mat)
	{
		BlockMatrix res = new BlockMatrix(getRowCount(), mat.getColumnCount());
		
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				Matrix val = new Matrix(vSize, vSize);
				
				for(int k = 0;k < getColumnCount();k ++)
				{
					if(blocks[i][k] != null && mat.blocks[k][j] != null)
					{
						val.add(blocks[i][k].mul(mat.blocks[k][j]));
					}
				}
				
				res.set(i, j, val);
			}
		}
		
		return res;
	}
	
	public BitSet mul(BitSet vec)
	{
		BitSet res = new BitSet(getTotalRowCount());
		int res_index = 0;
		
		for(int i = 0;i < getRowCount();i ++)
		{
			BitSet val = new BitSet(verticalSizes[i]);
			int vec_index = 0;
			
			for(int j = 0;j < getColumnCount();j ++)
			{
				BitSet subvec = vec.get(vec_index, vec_index + horizontalSizes[j]);
				val.xor(blocks[i][j].mul(subvec));
				vec_index += horizontalSizes[j];
			}
			
			for(int j = 0;j < val.size();j ++)
			{
				res.set(res_index + j, val.get(j));
			}
			
			res_index += verticalSizes[i];
		}
		
		return res;
	}/**/
	
}

