package database;

import static org.junit.Assert.*;

import org.junit.Test;

public class DatabaseManagmentTest {

	@Test
	public void testFillArticleCodesTable() {
		for (int freeDist = 3; freeDist <= 5; ++freeDist) {
			try {
				DatabaseManagment.fillArticleCodesTable(freeDist);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Unexpected exception.");
			}
		}
	}

}
