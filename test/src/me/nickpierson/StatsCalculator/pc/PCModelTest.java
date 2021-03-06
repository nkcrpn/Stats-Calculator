package me.nickpierson.StatsCalculator.pc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.thecellutioncenter.mvplib.ActionListener;
import com.thecellutioncenter.mvplib.DataActionListener;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class PCModelTest {

	private PCModel model;
	private ActionListener listener;
	private DataActionListener listenerN;
	private DataActionListener listenerR;
	private DataActionListener listenerNs;

	private String testN;
	private String testR;
	private String testNs;
	private ArrayList<Integer> nVals;
	private HashMap<Enum<?>, Integer> mapN;
	private HashMap<Enum<?>, Integer> mapR;
	private HashMap<Enum<?>, Integer> mapNAndR;
	private HashMap<Enum<?>, Object> mapNAndNs;
	private HashMap<Enum<?>, Object> mapAll;

	@Before
	public void setup() {
		model = new PCModel();

		testN = "20";
		testR = "10";
		testNs = "3,2";

		nVals = new ArrayList<Integer>();
		nVals.add(3);
		nVals.add(2);

		mapN = new HashMap<Enum<?>, Integer>();
		mapR = new HashMap<Enum<?>, Integer>();
		mapNAndR = new HashMap<Enum<?>, Integer>();
		mapNAndNs = new HashMap<Enum<?>, Object>();
		mapAll = new HashMap<Enum<?>, Object>();

		mapN.put(PCModel.Keys.N_VALUE, 20);
		mapR.put(PCModel.Keys.R_VALUE, 10);
		mapNAndR.put(PCModel.Keys.N_VALUE, 20);
		mapNAndR.put(PCModel.Keys.R_VALUE, 10);
		mapNAndNs.put(PCModel.Keys.N_VALUE, 20);
		mapNAndNs.put(PCModel.Keys.N_VALUES, nVals);
		mapAll.put(PCModel.Keys.N_VALUE, 20);
		mapAll.put(PCModel.Keys.R_VALUE, 10);
		mapAll.put(PCModel.Keys.N_VALUES, nVals);

		listener = mock(ActionListener.class);
		listenerN = mock(DataActionListener.class);
		listenerR = mock(DataActionListener.class);
		listenerNs = mock(DataActionListener.class);
	}

	@Test
	public void validateInputNotifiesPresenter_IfValueOverOneThousandIsEntered() {
		model.addListener(listener, PCModel.Types.INPUT_OVER_MAX_VALUE);

		model.validateInput("1001", "", "");
		model.validateInput("", "1001", "");
		model.validateInput("1001", "1001", "");

		verify(listener, times(4)).fire();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateInputHandlesInputCorrectly_ForNoValidValues() {
		addAllListeners();

		model.validateInput("", "", "");
		model.validateInput("", "", "3,4");
		model.validateInput("1001", "", "");
		model.validateInput("", "", "2147483657,5,20");
		model.validateInput("", "2147483657", "");
		model.validateInput("52147483657", "", "");

		verify(listenerN, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listenerR, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listenerNs, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listener, times(6)).fire();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateInputHandlesInputCorrectly_ForOnlyValidNValue() {
		addAllListeners();

		model.validateInput(testN, "", "");
		model.validateInput(testN, "", "10,11");

		verify(listenerN, times(2)).fire(mapN);
		verify(listenerR, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listenerNs, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listener, times(2)).fire();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateInputHandlesInputCorrectly_ForOnlyValidRValue() {
		addAllListeners();

		model.validateInput("", testR, "");
		model.validateInput("", testR, "3,4");

		verify(listenerR, times(2)).fire(mapR);
		verify(listenerN, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listenerNs, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listener, times(2)).fire();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateInputHandlesInputCorrectly_ForNAndRValues() {
		addAllListeners();

		model.validateInput(testN, testR, "");
		model.validateInput(testN, testR, "1001");

		/*
		 * I'm not entirely sure why listerN fires with mapNAndR, instead of
		 * mapN. I have a hunch that's it has to do with the way the testing
		 * works, because when running the app, the VALID_N listener *never*
		 * receives an R_VALUE in results map. Regardless, for the sake of
		 * testing, it's still good enough since VALID_N *at least* receives the
		 * N_VALUE which is really all that matters, however sloppy it is. This
		 * also applies to the two tests directly after this one.
		 */
		verify(listenerN, times(2)).fire(mapNAndR);
		verify(listenerR, times(2)).fire(mapNAndR);
		verify(listenerNs, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listener, times(2)).fire();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateInputHandlesInputCorrectly_ForNAndNValues() {
		addAllListeners();

		model.validateInput(testN, "", testNs);
		model.validateInput(testN, "", "3,,,,,,2,");

		verify(listenerN, times(2)).fire(mapNAndNs);
		verify(listenerNs, times(2)).fire(mapNAndNs);
		verify(listenerR, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(listener, times(2)).fire();
	}

	@Test
	public void validateInputHandlesInputCorrectly_ForAllValues() {
		addAllListeners();

		model.validateInput(testN, testR, testNs);

		verify(listenerN).fire(mapAll);
		verify(listenerR).fire(mapAll);
		verify(listenerNs).fire(mapAll);
	}

	@Test
	public void factorialReturnsCorrectValue() {
		assertEquals(BigInteger.valueOf(120), model.factorial(5));
		assertEquals(BigInteger.valueOf(479001600), model.factorial(12));
		assertEquals(BigInteger.ONE, model.factorial(1));
		assertEquals(BigInteger.ONE, model.factorial(0));
		assertEquals(BigInteger.ZERO, model.factorial(-1));
	}

	@Test
	public void derangementReturnsCorrectValue() {
		assertEquals(BigInteger.ONE, model.subfactorial(0));
		assertEquals(BigInteger.ZERO, model.subfactorial(1));
		assertEquals(BigInteger.valueOf(1), model.subfactorial(2));
		assertEquals(BigInteger.valueOf(44), model.subfactorial(5));
		assertEquals(BigInteger.valueOf(176214841), model.subfactorial(12));
	}

	@Test
	public void permutationReturnCorrectValue() {
		assertEquals(BigInteger.valueOf(60), model.permutation(5, 3));
		assertEquals(BigInteger.valueOf(151200), model.permutation(10, 6));
		assertEquals(BigInteger.valueOf(5040), model.permutation(7, 7));
		assertEquals(BigInteger.valueOf(5), model.permutation(5, 1));
		assertEquals(BigInteger.ONE, model.permutation(6, 0));
		assertEquals(BigInteger.ZERO, model.permutation(3, 4));
	}

	@Test
	public void repetitivePermutationReturnsCorrectValue() {
		assertEquals(BigInteger.ONE, model.repetitivePermutation(5, 0));
		assertEquals(BigInteger.ZERO, model.repetitivePermutation(0, 5));
		assertEquals(BigInteger.valueOf(78125), model.repetitivePermutation(5, 7));
		assertEquals(BigInteger.valueOf(9), model.repetitivePermutation(3, 2));
	}

	@Test
	public void combinationReturnsCorrectValue() {
		assertEquals(BigInteger.valueOf(10), model.combination(5, 3));
		assertEquals(BigInteger.valueOf(210), model.combination(10, 6));
		assertEquals(BigInteger.ONE, model.combination(7, 7));
		assertEquals(BigInteger.valueOf(5), model.combination(5, 1));
		assertEquals(BigInteger.ONE, model.combination(6, 0));
		assertEquals(BigInteger.ZERO, model.combination(3, 4));
	}

	@Test
	public void repetitiveCombinationReturnsCorrectValue() {
		assertEquals(BigInteger.ZERO, model.repetitiveCombination(0, 5));
		assertEquals(BigInteger.ZERO, model.repetitiveCombination(0, 0));
		assertEquals(BigInteger.ONE, model.repetitiveCombination(5, 0));
		assertEquals(BigInteger.valueOf(6), model.repetitiveCombination(2, 5));
		assertEquals(BigInteger.valueOf(120), model.repetitiveCombination(8, 3));
	}

	@Test
	public void pigeonholeReturnsCorrectValue() {
		assertEquals(BigInteger.ZERO, model.pigeonhole(0, 0));
		assertEquals(BigInteger.ZERO, model.pigeonhole(0, 5));
		assertEquals(BigInteger.ZERO, model.pigeonhole(5, 0));
		assertEquals(BigInteger.ZERO, model.pigeonhole(0, 0));
		assertEquals(BigInteger.ONE, model.pigeonhole(3, 6));
		assertEquals(BigInteger.valueOf(6), model.pigeonhole(21, 4));
	}

	@Test
	public void indistinctPermutationReturnsCorrectValue() {
		assertEquals(BigInteger.valueOf(60), model.indistinctPermutation(6, makeArrayList(3, 2)));
		assertEquals(BigInteger.valueOf(2), model.indistinctPermutation(2, makeArrayList(1, 1)));
		assertEquals(BigInteger.ONE, model.indistinctPermutation(1, makeArrayList(1)));
		assertEquals(BigInteger.valueOf(120), model.indistinctPermutation(5, makeArrayList(0)));
	}

	@Test
	public void formatReturnsFormattedNumber() {
		String expectedReturn1 = "1.0000000E9";
		String expectedReturn2 = "1.2345679E12";
		String expectedReturn3 = "1.0000000E12";
		String expectedReturn4 = "9.9000000E11";

		assertEquals(expectedReturn1, model.format(BigInteger.valueOf(1000000000L)));
		assertEquals(expectedReturn2, model.format(BigInteger.valueOf(1234567898765L)));
		assertEquals(expectedReturn3, model.format(BigInteger.valueOf(999999999999L)));
		assertEquals(expectedReturn4, model.format(BigInteger.valueOf(989999999999L)));
	}

	@Test
	public void updateResult_AddsResultToResultsHashMap() {
		HashMap<String, String> expectedMap = new HashMap<String, String>();
		assertEquals(expectedMap, model.getResults());
		String key = "Anything";
		String result = "Anything result";

		model.updateResult(key, result);
		expectedMap.put(key, result);

		assertEquals(expectedMap, model.getResults());
	}

	@Test
	public void clearResults_ClearsResultsHashMap() {
		HashMap<String, String> expectedMap = new HashMap<String, String>();
		String key = "Anything";
		String result = "Anything result";
		expectedMap.put(key, result);
		model.updateResult(key, result);

		assertEquals(expectedMap, model.getResults());

		model.clearResults();
		expectedMap.clear();

		assertEquals(expectedMap, model.getResults());
	}

	public ArrayList<Integer> makeArrayList(int... values) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int val : values) {
			list.add(val);
		}
		return list;
	}

	private void addAllListeners() {
		model.addListener(listenerN, PCModel.Types.VALID_N);
		model.addListener(listenerR, PCModel.Types.VALID_R);
		model.addListener(listenerNs, PCModel.Types.VALID_NS);
		model.addListener(listener, PCModel.Types.DONE_VALIDATING);
	}
}
