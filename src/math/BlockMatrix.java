package math;

public class BlockMatrix{
	
	private int totalColumns = 0;
	private int totalRows = 0;
	private Matrix[][] blocks;
	private int[] verticalSizes;
	private int[] horizontalSizes;	
	
	public BlockMatrix(int m, int n)
	{
		verticalSizes = new int[m];
		horizontalSizes = new int[n];
		
		for(int i = 0;i < m;i ++)
		{
			verticalSizes[i] = 0;
		}
		
		for(int i = 0;i < n;i ++)
		{
			horizontalSizes[i] = 0;
		}
		
		blocks = new Matrix[m][n];		
	}
	
	public BlockMatrix(Matrix[] row)
	{
		verticalSizes = new int[1];
		horizontalSizes = new int[row.length];
				
		verticalSizes[0] = row[0].getRowCount();
		totalRows = row[0].getRowCount();
		
		for(int i = 0;i < row.length;i ++)
		{
			horizontalSizes[i] = row[i].getColumnCount();
			totalColumns += row[i].getColumnCount();
		}
		
		blocks = new Matrix[1][row.length];
		blocks[0] = row;
	}
	
	public BlockMatrix(Matrix mat, int bh, int bw)
	{
		int m = mat.getRowCount() / bh, n = mat.getColumnCount() / bw;
		
		verticalSizes = new int[m];
		horizontalSizes = new int[n];
		
		for(int i = 0;i < m;i ++)
		{
			verticalSizes[i] = bh;
			totalRows += bh;
		}
		
		for(int i = 0;i < n;i ++)
		{
			horizontalSizes[i] = bw;
			totalColumns += bw;
		}
		
		blocks = new Matrix[m][n];
		
		for(int bi = 0;bi < m;bi ++)
		{
			for(int bj = 0;bj < n;bj ++)
			{
				blocks[bi][bj] = mat.getSubMatrix(bi * bh, bj * bw, bh, bw);
			}
		}
	}
	
	public int getVerticalSize(int sec)
	{
		return verticalSizes[sec];
	}
	
	public int getHorizontalSize(int sec)
	{
		return horizontalSizes[sec];
	}
	
	public int getTotalRowCount()
	{		
		return totalRows;
	}
	
	public int getTotalColumnCount()
	{		
		return totalColumns;
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
			if(verticalSizes[i] == 0)
			{
				verticalSizes[i] =  value.getRowCount();
				totalRows += verticalSizes[i];			
			}
			
			if(horizontalSizes[j] == 0)
			{
				horizontalSizes[j] = value.getColumnCount();
				totalColumns += horizontalSizes[j];
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
					unblocked.setZeroBlock(rowPos, colPos, verticalSizes[i], horizontalSizes[j]);
				}else{
					unblocked.setBlock(rowPos, colPos, blocks[i][j]);
				}
				colPos += horizontalSizes[j];
			}
			rowPos += verticalSizes[i];
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
	
	public BlockMatrix mul(BlockMatrix mat)
	{
		BlockMatrix res = new BlockMatrix(getRowCount(), mat.getColumnCount());
		
		for(int i = 0;i < getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				Matrix val = new Matrix(verticalSizes[i], verticalSizes[i]);
				
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
	}
	
}

