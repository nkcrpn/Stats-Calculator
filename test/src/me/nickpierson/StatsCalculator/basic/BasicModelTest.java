package me.nickpierson.StatsCalculator.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.nickpierson.StatsCalculator.utils.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.app.Activity;

import com.thecellutioncenter.mvplib.ActionListener;
import com.thecellutioncenter.mvplib.DataActionListener;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class BasicModelTest {

	public BasicModel model;
	protected double DELTA = .000001;

	private DataActionListener validDataListener;
	private DataActionListener invalidDataListener;
	private ActionListener validListener;
	private ActionListener invalidListener;
	protected Activity activity;

	@Before
	public void setup() {
		activity = mock(Activity.class);
		model = new BasicModel(activity);

		validDataListener = mock(DataActionListener.class);
		invalidDataListener = mock(DataActionListener.class);
		validListener = mock(ActionListener.class);
		invalidListener = mock(ActionListener.class);
	}

	@Test
	public void modelReturnsEmptyHashMapOnRequest() {
		HashMap<String, Double> emptyMap = model.getEmptyResults();

		for (Double result : emptyMap.values()) {
			assertTrue(Double.isNaN(result));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateInputNotifiesCorrectly_GivenValidInput() {
		HashMap<Enum<?>, ArrayList<Double>> validMap1 = new HashMap<Enum<?>, ArrayList<Double>>();
		HashMap<Enum<?>, ArrayList<Double>> validMap2 = new HashMap<Enum<?>, ArrayList<Double>>();
		String validInput1 = "500,30x3,59.0233";
		String validInput2 = "55.5,,31.3x2,-3,.2,-.23";
		String validInput3 = "60.1123x100000";
		ArrayList<Double> validList1 = makeValidList(500, 30, 30, 30, 59.0233);
		ArrayList<Double> validList2 = makeValidList(55.5, 31.3, 31.3, -3, .2, -.23);
		validMap1.put(BasicModel.Keys.VALIDATED_LIST, validList1);
		validMap2.put(BasicModel.Keys.VALIDATED_LIST, validList2);
		addAllListeners();

		model.validateInput(validInput1);
		model.validateInput(validInput2);
		model.validateInput(validInput3);

		verify(validDataListener).fire(validMap1);
		verify(validDataListener).fire(validMap2);
		verify(validDataListener, times(3)).fire((HashMap<Enum<?>, ?>) any(Object.class));
		verify(invalidDataListener, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateInputNotifiesCorrectly_GivenInvalidInput() {
		HashMap<Enum<?>, Object> invalidMapFirstPos = new HashMap<Enum<?>, Object>();
		HashMap<Enum<?>, Object> invalidMapThirdPos = new HashMap<Enum<?>, Object>();
		invalidMapFirstPos.put(BasicModel.Keys.INVALID_POSITION, 1);
		invalidMapThirdPos.put(BasicModel.Keys.INVALID_POSITION, 3);
		addAllListeners();

		verifyInvalidInput(",,", invalidMapFirstPos, "");

		verifyInvalidInput("23x1.5", invalidMapFirstPos, "23x1.5");

		verifyInvalidInput("23x100001", invalidMapFirstPos, "23x100001");

		verifyInvalidInput("x5,27", invalidMapFirstPos, "x5");

		verifyInvalidInput("5x,27", invalidMapFirstPos, "5x");

		verifyInvalidInput("23,52x2,56..8,9", invalidMapThirdPos, "56..8");

		verifyInvalidInput("23,25.6,23..5x2,7", invalidMapThirdPos, "23..5x2");

		verifyInvalidInput("23,25.6,25-5,7", invalidMapThirdPos, "25-5");

		verifyInvalidInput("23,25.6,25xx5,7", invalidMapThirdPos, "25xx5");

		verify(validDataListener, never()).fire((HashMap<Enum<?>, ?>) any(Object.class));
	}

	public void verifyInvalidInput(String invalidInput, HashMap<Enum<?>, Object> invalidMap, String invalidText) {
		model.validateInput(invalidInput);
		invalidMap.put(BasicModel.Keys.INVALID_TEXT, invalidText);
		verify(invalidDataListener).fire(invalidMap);
	}

	public void addAllListeners() {
		model.addListener(validDataListener, BasicModel.Types.VALID_INPUT);
		model.addListener(invalidDataListener, BasicModel.Types.INVALID_INPUT);
	}

	@Test
	public void calculateResults_CalculatesCorrectResult() {
		ArrayList<Double> sampleInput = makeValidList(45, 68.1, 29.4, -54, -.19, 3.0001);

		HashMap<String, Double> actualResults = model.calculateResults(sampleInput);

		assertEquals(6.0, actualResults.get(Constants.SIZE), DELTA);
		assertEquals(91.3101, actualResults.get(Constants.SUM), DELTA);
		assertEquals(-54, actualResults.get(Constants.MIN), DELTA);
		assertEquals(68.1, actualResults.get(Constants.MAX), DELTA);
		assertEquals(15.21835, actualResults.get(Constants.ARITH_MEAN), DELTA);
		assertEquals(Double.NaN, actualResults.get(Constants.GEO_MEAN), DELTA);
		assertEquals(Double.NaN, actualResults.get(Constants.MODE), DELTA);
		assertEquals(122.1, actualResults.get(Constants.RANGE), DELTA);
		assertEquals(-.19, actualResults.get(Constants.FIRST_QUART), DELTA);
		assertEquals(16.20005, actualResults.get(Constants.MEDIAN), DELTA);
		assertEquals(45, actualResults.get(Constants.THIRD_QUART), DELTA);
		assertEquals(45.19, actualResults.get(Constants.IQR), DELTA);
		assertEquals(1812.483527, actualResults.get(Constants.SAMPLE_VAR), DELTA);
		assertEquals(1510.402939, actualResults.get(Constants.POP_VAR), DELTA);
		assertEquals(42.573272, actualResults.get(Constants.SAMPLE_DEV), DELTA);
		assertEquals(38.863902, actualResults.get(Constants.POP_DEV), DELTA);
		assertEquals(2.797495, actualResults.get(Constants.COEFF_VAR), DELTA);
		assertEquals(-.4542037, actualResults.get(Constants.SKEWNESS), DELTA);
		assertEquals(2.314556, actualResults.get(Constants.KURTOSIS), DELTA);

		ArrayList<Double> sampleInput1 = makeValidList(45, 68.1, 29.4, 54, 5.3, 5.3);
		HashMap<String, Double> actualResult1 = model.calculateResults(sampleInput1);
		assertEquals(22.695621, actualResult1.get(Constants.GEO_MEAN), DELTA);
		assertEquals(5.3, actualResult1.get(Constants.MODE), DELTA);

		ArrayList<Double> sampleInput2 = makeValidList(-99.5, -55, -32.2);
		HashMap<String, Double> actualResult2 = model.calculateResults(sampleInput2);
		assertEquals(67.3, actualResult2.get(Constants.RANGE), DELTA);

		ArrayList<Double> sampleInput3 = makeValidList(25, 25, 36);
		HashMap<String, Double> actualResult3 = model.calculateResults(sampleInput3);
		assertEquals(25, actualResult3.get(Constants.FIRST_QUART), DELTA);

		ArrayList<Double> sampleInput4 = makeValidList(31.8, 32.4, 32.4, 36.2, 37.1);
		HashMap<String, Double> actualResult4 = model.calculateResults(sampleInput4);
		assertEquals(32.1, actualResult4.get(Constants.FIRST_QUART), DELTA);
		assertEquals(36.65, actualResult4.get(Constants.THIRD_QUART), DELTA);

		ArrayList<Double> sampleInput5 = makeValidList(31.8, 34.2, 36.4, 36.4, 37.1);
		HashMap<String, Double> actualResult5 = model.calculateResults(sampleInput5);
		assertEquals(33, actualResult5.get(Constants.FIRST_QUART), DELTA);
		assertEquals(36.75, actualResult5.get(Constants.THIRD_QUART), DELTA);
	}

	@Test
	public void formatResults_FormatsResultsCorrectly() {
		HashMap<String, Double> results = new HashMap<String, Double>();
		results.put("1", 999.0);
		results.put("2", 1000.0);
		results.put("3", 2345.67);
		results.put("4", 999999999.999);
		results.put("5", 1000000000.0);
		results.put("6", 123456789101112.0);

		HashMap<String, String> testResults = model.formatResults(results);

		assertEquals("999", testResults.get("1"));
		assertEquals("1,000", testResults.get("2"));
		assertEquals("2,345.67", testResults.get("3"));
		assertEquals("999,999,999.999", testResults.get("4"));
		assertEquals("1E9", testResults.get("5"));
		assertEquals("1.234567891E14", testResults.get("6"));
	}

	@Test
	public void saveListNotifiesCorrectly_GivenValidInput() throws FileNotFoundException {
		addAllListListeners();
		String testName = "testName.txt";
		String input = "34x3,72.1,1337.H4CK3R";
		File testFile = new File(testName);

		model.saveList(testName, input);

		verify(validListener).fire();
		verify(invalidListener, never()).fire();

		testFile.delete();
	}

	@Test
	public void saveListNotifiesCorrectly_GivenInvalidInput() throws IOException {
		addAllListListeners();
		String testName = "someName.txt";
		String input = "OLLEH";
		File alreadyExists = new File(testName);
		alreadyExists.createNewFile();

		File testFile = new File(testName);
		FileOutputStream fakeStream = new FileOutputStream(testFile);
		when(activity.openFileOutput(testName, 0)).thenReturn(fakeStream);

		model.saveList(testName, input);

		verify(invalidListener).fire();
		verify(validListener, never()).fire();

		testFile.delete();
		alreadyExists.delete();
	}

	private void addAllListListeners() {
		model.addListener(validListener, BasicModel.Types.SAVE_SUCCESSFUL);
		model.addListener(invalidListener, BasicModel.Types.SAVE_FAILED);
	}

	@Test
	public void whenGetSavedLists_ThenSavedListsAreReturned() throws IOException {
		when(activity.getFilesDir()).thenReturn(new File("./testDir"));
		String listOne = "first";
		String listTwo = "second";
		File file1 = new File("testDir/" + listOne);
		File file2 = new File("testDir/" + listTwo);
		file1.createNewFile();
		file2.createNewFile();

		String[] lists = model.getSavedLists();

		assertTrue(lists.length == 2);

		assertEquals(lists[0], listOne);
		assertEquals(lists[1], listTwo);

		file1.delete();
		file2.delete();
	}

	@Test
	public void loadList_ReturnsCorrectListInput() {
		when(activity.getFilesDir()).thenReturn(new File("./testDir"));
		String listName = "someList";
		String expectedInput = "6,7,8,9";
		File listFile = new File("testDir/" + listName);
		makeList(listFile, expectedInput);
		model.addListener(invalidListener, BasicModel.Types.LOAD_ERROR);

		String realInput = model.loadList("someList");

		assertEquals(expectedInput, realInput);
		verify(invalidListener, never()).fire();

		listFile.delete();
	}

	@Test
	public void deleteList_DeletesListFromMemory() throws IOException {
		when(activity.getFilesDir()).thenReturn(new File("./testDir"));
		String listName = "someOtherList";
		File listFile = new File("testDir/" + listName);
		makeList(listFile, "any random input");
		model.addListener(invalidListener, BasicModel.Types.DELETE_ERROR);

		listFile.createNewFile();
		assertTrue(listFile.exists());

		model.deleteList(listName);

		verify(invalidListener, never()).fire();
		assertFalse(listFile.exists());
	}

	public void makeList(File file, String input) {
		FileOutputStream output;
		try {
			output = new FileOutputStream(file);
			output.write(input.getBytes());
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected ArrayList<Double> makeValidList(double... args) {
		ArrayList<Double> validList = new ArrayList<Double>();
		for (double val : args) {
			validList.add(val);
		}
		return validList;
	}
}
