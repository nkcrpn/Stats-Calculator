package me.nickpierson.StatsCalculator.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.nickpierson.StatsCalculator.utils.Constants;
import android.app.Activity;

import com.thecellutioncenter.mvplib.DataActionHandler;

public class BasicModel extends DataActionHandler {

	private Activity activity;

	public enum Types {
		VALID_INPUT, INVALID_INPUT, SAVE_SUCCESSFUL, SAVE_FAILED, LOAD_ERROR, DELETE_ERROR;
	}

	public enum Keys {
		INVALID_POSITION, VALIDATED_LIST, INVALID_TEXT;
	}

	public BasicModel(Activity activity) {
		this.activity = activity;
	}

	public HashMap<String, Double> getEmptyResults() {
		HashMap<String, Double> results = new HashMap<String, Double>();
		for (String title : Constants.BASIC_TITLES) {
			results.put(title, Double.NaN);
		}

		return results;
	}

	public void validateInput(String input) {
		HashMap<Enum<?>, Object> results = new HashMap<Enum<?>, Object>();

		if (input.length() == 0) {
			eventInvalid(results, 1, "");
			return;
		}

		String[] values = input.split(",");
		for (int i = 0; i < values.length; i++) {
			String currVal = values[i];

			if (currVal.length() == 0) {
				continue;
			} else if (currVal.contains("x")) {
				if (!isValidFreqItem(currVal)) {
					eventInvalid(results, i + 1, currVal);
					return;
				}
			} else if (!isValidDouble(currVal)) {
				eventInvalid(results, i + 1, currVal);
				return;
			}
		}

		ArrayList<Double> list = convertList(input);

		if (list.isEmpty()) {
			eventInvalid(results, 1, "");
			return;
		}

		results.put(Keys.VALIDATED_LIST, list);
		dataEvent(Types.VALID_INPUT, results);
	}

	private void eventInvalid(HashMap<Enum<?>, Object> results, int position, String text) {
		results.put(Keys.INVALID_POSITION, position);
		results.put(Keys.INVALID_TEXT, text);
		dataEvent(Types.INVALID_INPUT, results);
	}

	private boolean isValidFreqItem(String val) {
		String[] values = val.split("x");
		if (values.length != 2) {
			return false;
		}

		if (!isValidDouble(values[0]) || !isValidFrequency(values[1])) {
			return false;
		}

		return true;
	}

	private boolean isValidFrequency(String string) {
		if (!isValidInteger(string)) {
			return false;
		}

		int value = Integer.valueOf(string);
		if (value > Constants.MAX_FREQUENCY) {
			return false;
		}

		return true;
	}

	private boolean isValidInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isValidDouble(String string) {
		try {
			Double.valueOf(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private ArrayList<Double> convertList(String input) {
		ArrayList<Double> convertedList = new ArrayList<Double>();
		for (String value : input.split(",")) {
			if (value.length() == 0) {
				continue;
			} else if (value.contains("x")) {
				String[] freqItem = value.split("x");
				for (int i = 0; i < Integer.valueOf(freqItem[1]); i++) {
					convertedList.add(Double.valueOf(freqItem[0]));
				}
			} else {
				convertedList.add(Double.valueOf(value));
			}
		}

		return convertedList;
	}

	public HashMap<String, Double> calculateResults(List<Double> numberList) {
		HashMap<String, Double> results = new HashMap<String, Double>();

		Collections.sort(numberList);

		Double size = (double) numberList.size();
		Double sum = calculateSum(numberList);
		Double arithMean = sum / size;
		Double firstQuart = calculateFirstQuartile(numberList);
		Double thirdQuart = calculateThirdQuartile(numberList);
		Double sampleVar = calculateSampleVariance(numberList, arithMean, size);
		Double popVar = calculatePopVariance(numberList, arithMean, size);
		Double sampleDev = Math.sqrt(sampleVar);
		double popDev = Math.sqrt(popVar);

		results.put(Constants.SIZE, size);
		results.put(Constants.SUM, sum);
		results.put(Constants.MIN, numberList.get(0));
		results.put(Constants.MAX, numberList.get(numberList.size() - 1));
		results.put(Constants.ARITH_MEAN, arithMean);
		results.put(Constants.GEO_MEAN, calculateGeoMean(numberList));
		results.put(Constants.MODE, calculateMode(numberList));
		results.put(Constants.RANGE, calculateRange(numberList));
		results.put(Constants.FIRST_QUART, firstQuart);
		results.put(Constants.MEDIAN, calculateMedian(numberList, size));
		results.put(Constants.THIRD_QUART, thirdQuart);
		results.put(Constants.IQR, thirdQuart - firstQuart);
		results.put(Constants.SAMPLE_VAR, sampleVar);
		results.put(Constants.POP_VAR, popVar);
		results.put(Constants.SAMPLE_DEV, sampleDev);
		results.put(Constants.POP_DEV, popDev);
		results.put(Constants.COEFF_VAR, sampleDev / arithMean);
		results.put(Constants.SKEWNESS, calculateSkewness(numberList, arithMean, popDev));
		results.put(Constants.KURTOSIS, calculateKurtosis(numberList, arithMean, popDev));

		return results;
	}

	private double calculateSum(List<Double> numberList) {
		double sum = 0;
		for (double num : numberList) {
			sum += num;
		}
		return sum;
	}

	private double calculateFirstQuartile(List<Double> numberList) {
		if (numberList.size() == 1) {
			return numberList.get(0);
		}

		int index;
		if (numberList.size() % 2 == 1) {
			index = (int) Math.floor(numberList.size() / 2.0);
		} else {
			index = numberList.size() / 2;
		}

		List<Double> lowerHalf = new ArrayList<Double>();
		for (int i = 0; i < index; i++) {
			lowerHalf.add(numberList.get(i));
		}

		return calculateMedian(lowerHalf, lowerHalf.size());
	}

	private double calculateMedian(List<Double> numberList, double length) {
		int size = (int) length;

		if (size % 2 == 1) {
			int index = (int) Math.floor(size / 2.0);
			return numberList.get(index);
		} else {
			int half = size / 2;
			return (numberList.get(half - 1) + numberList.get(half)) / 2;
		}
	}

	private double calculateThirdQuartile(List<Double> numberList) {
		if (numberList.size() == 1) {
			return numberList.get(0);
		}

		int index;
		if (numberList.size() % 2 == 1) {
			index = ((int) Math.floor(numberList.size() / 2.0)) + 1;
		} else {
			index = numberList.size() / 2;
		}

		List<Double> upperHalf = new ArrayList<Double>();
		for (int i = index; i < numberList.size(); i++) {
			upperHalf.add(numberList.get(i));
		}

		return calculateMedian(upperHalf, upperHalf.size());
	}

	private double calculateMode(List<Double> numberList) {
		HashMap<Double, Integer> freqs = new HashMap<Double, Integer>();
		for (double num : numberList) {
			Integer freq = freqs.get(num);
			freqs.put(num, freq == null ? 1 : freq + 1);
		}

		double mode = 0;
		int max = 0;
		boolean isSimilarMax = true;
		for (Map.Entry<Double, Integer> entry : freqs.entrySet()) {
			int freq = entry.getValue();
			if (freq > max) {
				isSimilarMax = false;
				max = freq;
				mode = entry.getKey();
			} else if (freq == max) {
				isSimilarMax = true;
			}
		}

		if (isSimilarMax) {
			return Double.NaN;
		} else {
			return mode;
		}
	}

	private double calculateRange(List<Double> numberList) {
		double max = -Double.MAX_VALUE, min = Double.MAX_VALUE;
		for (double num : numberList) {
			if (num > max) {
				max = num;
			}
			if (num < min) {
				min = num;
			}
		}

		return max - min;
	}

	private double calculatePopVariance(List<Double> numberList, double average, double size) {
		double numerator = calculateVarianceNumerator(numberList, average);
		return numerator / size;
	}

	private double calculateSampleVariance(List<Double> numberList, double average, double size) {
		double numerator = calculateVarianceNumerator(numberList, average);
		return numerator / (size - 1);
	}

	private double calculateVarianceNumerator(List<Double> numberList, double average) {
		double sum = 0;
		for (double num : numberList) {
			sum += Math.pow(num - average, 2);
		}
		return sum;
	}

	private double calculateGeoMean(List<Double> numberList) {
		double value = 0;
		for (double number : numberList) {
			if (number < 0) {
				return Double.NaN;
			}

			value += Math.log(number);
		}

		value = value / numberList.size();

		return Math.pow(Math.E, value);
	}

	private double calculateSkewness(List<Double> numberList, double mean, double stdDev) {
		return calculateKurtOrSkew(3, numberList, mean, stdDev);
	}

	private double calculateKurtosis(List<Double> numberList, double mean, double stdDev) {
		return calculateKurtOrSkew(4, numberList, mean, stdDev);
	}

	private double calculateKurtOrSkew(int power, List<Double> numberList, double mean, double stdDev) {
		double sum = 0;
		for (double number : numberList) {
			sum += Math.pow(number - mean, power);
		}

		double denom = Math.pow(stdDev, power) * (numberList.size());

		return sum / denom;
	}

	public HashMap<String, String> formatResults(HashMap<String, Double> oldResults) {
		HashMap<String, String> results = new HashMap<String, String>();

		for (String key : oldResults.keySet()) {
			results.put(key, format(oldResults.get(key)));
		}

		return results;
	}

	private String format(Double num) {
		String result;
		if (num >= Constants.MAX_PLAIN_FORMAT) {
			DecimalFormat format = new DecimalFormat(Constants.DECIMAL_FORMAT_LARGE);
			result = format.format(num);
		} else {
			DecimalFormat format = new DecimalFormat();
			format.setMaximumFractionDigits(Constants.DECIMAL_PLACES_LARGE);
			result = format.format(num);
		}

		return result;
	}

	public void saveList(String name, String input) {
		File outputFile = new File(activity.getFilesDir(), name);
		if (outputFile.exists()) {
			event(Types.SAVE_FAILED);
			return;
		}

		try {
			FileOutputStream output = new FileOutputStream(outputFile);
			output.write(input.getBytes());
			output.close();
		} catch (Exception e) {
			event(Types.SAVE_FAILED);
			return;
		}

		event(Types.SAVE_SUCCESSFUL);
	}

	public String[] getSavedLists() {
		File internalDir = activity.getFilesDir();
		return internalDir.list();
	}

	public String loadList(String listName) {
		File listFile = new File(activity.getFilesDir(), listName);

		StringBuilder list = new StringBuilder();
		try {
			FileInputStream input = new FileInputStream(listFile);
			byte[] bytes = new byte[input.available()];

			input.read(bytes);

			for (byte b : bytes) {
				list.append((char) b);
			}

			input.close();
		} catch (IOException e) {
			event(Types.LOAD_ERROR);
			e.printStackTrace();
		}

		return list.toString();
	}

	public void deleteList(String listName) {
		File file = new File(activity.getFilesDir(), listName);
		boolean isDeleted = file.delete();
		if (!isDeleted) {
			event(Types.DELETE_ERROR);
		}
	}
}
