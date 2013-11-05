package me.nickpierson.StatsCalculator.pc;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import me.nickpierson.StatsCalculator.utils.MyConstants;

import com.thecellutioncenter.mvplib.ActionListener;
import com.thecellutioncenter.mvplib.DataActionListener;

public class PCPresenter {

	protected static void setup(final PCModel model, final PCView view) {
		view.showDefaultValues();

		view.addListener(new ActionListener() {

			@Override
			public void fire() {
				validateInput(model, view);
			}
		}, PCView.Types.DONE_PRESSED);

		view.addListener(new ActionListener() {

			@Override
			public void fire() {
				if (!view.isKeypadVisible()) {
					view.showKeypad();
				}
			}
		}, PCView.Types.EDITTEXT_CLICKED);

		model.addListener(new DataActionListener() {

			@Override
			public void fire(HashMap<Enum<?>, ?> data) {
				int n = (Integer) data.get(PCModel.Keys.N_VALUE);

				view.showResults();
				setNFactorial(model, view, n);
				view.setRFactorial(MyConstants.NOT_APPLICABLE);
				view.setPermutation(MyConstants.NOT_APPLICABLE);
				view.setCombination(MyConstants.NOT_APPLICABLE);
				view.setIndistinct(MyConstants.NOT_APPLICABLE);
			}
		}, PCModel.Types.ONLY_VALID_N);

		model.addListener(new DataActionListener() {

			@Override
			public void fire(HashMap<Enum<?>, ?> data) {
				int r = (Integer) data.get(PCModel.Keys.R_VALUE);

				view.showResults();
				view.setNFactorial(MyConstants.NOT_APPLICABLE);
				setRFactorial(model, view, r);
				view.setPermutation(MyConstants.NOT_APPLICABLE);
				view.setCombination(MyConstants.NOT_APPLICABLE);
				view.setIndistinct(MyConstants.NOT_APPLICABLE);
			}
		}, PCModel.Types.ONLY_VALID_R);

		model.addListener(new DataActionListener() {

			@Override
			public void fire(HashMap<Enum<?>, ?> data) {
				int n = (Integer) data.get(PCModel.Keys.N_VALUE);
				int r = (Integer) data.get(PCModel.Keys.R_VALUE);

				view.showResults();
				setNFactorial(model, view, n);
				setRFactorial(model, view, r);
				setPermAndComb(model, view, n, r);
				view.setIndistinct(MyConstants.NOT_APPLICABLE);
			}
		}, PCModel.Types.VALID_N_AND_R);

		model.addListener(new DataActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void fire(HashMap<Enum<?>, ?> data) {
				int n = (Integer) data.get(PCModel.Keys.N_VALUE);
				ArrayList<Integer> nVals = (ArrayList<Integer>) data.get(PCModel.Keys.N_VALUES);

				view.showResults();
				setNFactorial(model, view, n);
				view.setRFactorial(MyConstants.NOT_APPLICABLE);
				view.setPermutation(MyConstants.NOT_APPLICABLE);
				view.setCombination(MyConstants.NOT_APPLICABLE);
				setIndistinct(model, view, n, nVals);
			}
		}, PCModel.Types.VALID_N_AND_NS);

		model.addListener(new DataActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void fire(HashMap<Enum<?>, ?> data) {
				int n = (Integer) data.get(PCModel.Keys.N_VALUE);
				int r = (Integer) data.get(PCModel.Keys.R_VALUE);
				ArrayList<Integer> nVals = (ArrayList<Integer>) data.get(PCModel.Keys.N_VALUES);

				view.showResults();
				setNFactorial(model, view, n);
				setRFactorial(model, view, r);
				setPermAndComb(model, view, n, r);
				setIndistinct(model, view, n, nVals);
			}
		}, PCModel.Types.ALL_VALUES_VALID);

		model.addListener(new ActionListener() {

			@Override
			public void fire() {
				view.showToast(MyConstants.MESSAGE_INPUT_OVER_MAX);
			}
		}, PCModel.Types.INPUT_OVER_MAX_VALUE);
	}

	private static void validateInput(final PCModel model, final PCView view) {
		view.showDefaultValues();
		model.validateInput(view.getNVal(), view.getRVal(), view.getNVals());
	}

	private static void setNFactorial(final PCModel model, final PCView view, int n) {
		view.setNFactorial(format(model.factorial(n), model));
	}

	private static void setRFactorial(final PCModel model, final PCView view, int r) {
		view.setRFactorial(format(model.factorial(r), model));
	}

	private static void setPermAndComb(final PCModel model, final PCView view, int n, int r) {
		view.setPermutation(format(model.permutation(n, r), model));
		view.setCombination(format(model.combination(n, r), model));
	}

	private static void setIndistinct(final PCModel model, final PCView view, int n, ArrayList<Integer> nVals) {
		view.setIndistinct(format(model.indistinctPermutation(n, nVals), model));
	}

	private static String format(BigInteger number, PCModel model) {
		String stringValue;
		if (number.compareTo(BigInteger.valueOf(MyConstants.MAX_PLAIN_FORMAT)) == 1) {
			stringValue = model.format(number);
		} else {
			stringValue = new DecimalFormat().format(number);
		}
		return stringValue;
	}
}