package math.tests;

import static org.junit.Assert.*;

import math.Poly;

import org.junit.Test;

public class PolyTest {
	
	@Test // constructor
	public void correctPolyOfPositiveDegreeExample() {
		Poly poly1 = new Poly(new Boolean[] {true, false, true, true});
		assertTrue(poly1.getCoeff(poly1.getDegree()));
		assertEquals(3, poly1.getDegree());

		Poly poly2 = new Poly(new Boolean[] {true, false, true, false});
		assertTrue(poly2.getCoeff(poly2.getDegree()));
		assertEquals(2, poly2.getDegree());		
	}
	
	@Test // constructor
	public void correctPolyOfZeroDegreeExample() {
		Poly poly = new Poly();
		assertEquals(0, poly.getDegree());
		assertFalse(poly.getCoeff(0));
		
		Poly zeroPoly = new Poly(new Boolean[] {false});
		assertEquals(0, zeroPoly.getDegree());
		assertFalse(zeroPoly.getCoeff(0));

		Poly identityPoly = new Poly(new Boolean[] {true});
		assertEquals(0, identityPoly.getDegree());
		assertTrue(identityPoly.getCoeff(0));
	}
		
	@Test // setCoeff
	public void shouldRemainCorrectAfterSetCoeff() {
		Poly poly = new Poly(new Boolean[] {true, false, true, true});
		
		poly.setCoeff(3, false);
		assertTrue(isPolyCorrect(poly));
		assertEquals(2, poly.getDegree());
	}

	@Test // setCoeff
	public void couldSetOnlyExistingCoeffs() {
		Poly poly = new Poly(new Boolean[] {true, false, true, true});
		
		poly.setCoeff(1, true);
		assertTrue(isPolyCorrect(poly));

		try {
			poly.setCoeff(4, true);
			fail("This code shouldn't be run because of expected exception.");
		} catch (IndexOutOfBoundsException e) {
			System.out.println("All is ok: expected exception was thrown.");
		}

		try {
			poly.setCoeff(5, true);
			fail("This code shouldn't be run because of expected exception.");
		} catch (IndexOutOfBoundsException e) {
			System.out.println("All is ok: expected exception was thrown.");
		}
	}

	@Test // add
	public void shouldRemainCorrectAfterAdd() {
		Poly poly1 = new Poly(new Boolean[] {true, false, true, true});
		Poly poly2 = new Poly(new Boolean[] {false, false, false, true});
		
		poly1.add(poly2);
		assertTrue(isPolyCorrect(poly1));
		assertEquals(2, poly1.getDegree());
	}
	
	@Test // add
	public void shouldAllowSelfAdding() {
		Poly poly = new Poly(new Boolean[] {false, false, false, true});
		poly.add(poly);

		assertTrue(isPolyCorrect(poly));
		assertEquals(0, poly.getDegree());
	}
	
	@Test // increaseDegree
	public void shouldIncreaseDegreeOfNonZeroPoly() {
		Poly poly = new Poly(new Boolean[] {false, false, false, true});
		
		assertEquals(3, poly.getDegree());
		poly.increaseDegree(3);
		assertEquals(6, poly.getDegree());
		assertTrue(isPolyCorrect(poly));
	}
	
	@Test // increaseDegree
	public void shouldNotIncreaseDegreeOfZeroPoly() {
		Poly poly = new Poly();

		assertEquals(0, poly.getDegree());
		poly.increaseDegree(3);
		assertEquals(0, poly.getDegree());
		assertTrue(isPolyCorrect(poly));
	}

	@Test // mul
	public void shouldRemainCorrectAfterMul() {
		Poly poly1 = new Poly(new Boolean[] {true, true});
		Poly poly2 = new Poly(new Boolean[] {true, false, true});
		Poly poly = poly1.mul(poly2);
		
		assertEquals(poly.getDegree(), poly1.getDegree() + poly2.getDegree());
		assertTrue(isPolyCorrect(poly));
	}
	
	@Test // mul
	public void multiplicationByZeroShouldBeZero() {
		Poly poly1 = new Poly(new Boolean[] {true, false, true});
		Poly zero = new Poly();
		
		Poly poly;

		poly = poly1.mul(zero);
		assertEquals(0, poly.getDegree());
		assertFalse(poly.getCoeff(0));
		
		poly = zero.mul(poly1);
		assertEquals(0, poly.getDegree());
		assertFalse(poly.getCoeff(0));
	}
	
	@Test // getDegree
	public void identityPolyDegreeIsZero() {
		Poly poly = new Poly(new Boolean[] {true});
		assertEquals(0, poly.getDegree());
	}
	
	@Test // getDegree
	public void zeroPolyDegreeIsZero() {
		Poly poly = new Poly();
		assertEquals(0, poly.getDegree());
	}
	
	@Test // getDegree
	public void polyDegreeShouldBeCorrect() {
		Poly poly = new Poly(new Boolean[] {false, true, false, true, false, false});
		assertEquals(3, poly.getDegree());
	}

	@Test // all other methods
	public void methodsShouldBeCorrect() {
		Poly poly1 = new Poly(new Boolean[] {true, true, false, true});
		Poly poly2 = new Poly(new Boolean[] {false, true, true});
		
		assertTrue(isPolyCorrect(poly1.getQuotient(poly2)));
		
		assertTrue(isPolyCorrect(poly1.getRemainder(poly2)));
		
		Poly division[] = poly1.divide(poly2);
		assertTrue(isPolyCorrect(division[0]));
		assertTrue(isPolyCorrect(division[1]));

		Poly euclid[] = Poly.extendedEuclid(poly1, poly2);
		assertTrue(isPolyCorrect(euclid[0]));
		assertTrue(isPolyCorrect(euclid[1]));
		assertTrue(isPolyCorrect(euclid[2]));
	}
	
	private boolean isPolyCorrect(Poly poly) {
		return poly.getDegree() == 0 ||
			poly.getCoeff(poly.getDegree()) != false;
	}
}
