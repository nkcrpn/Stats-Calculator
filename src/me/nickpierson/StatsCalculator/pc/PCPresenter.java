package me.nickpierson.StatsCalculator.pc;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import me.nickpierson.StatsCalculator.utils.Constants;

import com.thecellutioncenter.mvplib.ActionListener;
import com.thecellutioncenter.mvplib.DataActionListener;

public class PCPresenter {

	protected static void setup(final PCModel model, final PCView view) {
		showEmptyResults(model, view);

		view.addListener(new ActionListener() {

			@Override
			public void fire() {
				model.clearResults();
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
				String nFact = format(model.factorial(n), model);
				String nSubfact = format(model.subfactorial(n), model);

				model.updateResult(Constants.N_FACT, nFact);
				model.updateResult(Constants.N_SUBFACT, nSubfact);
			}
		}, PCModel.Types.VALID_N);

		model.addListener(new DataActionListener() {

			@Override
			public void fire(HashMap<Enum<?>, ?> data) {
				int r = (Integer) data.get(PCModel.Keys.R_VALUE);
				String rFact = format(model.factorial(r), model);
				String rSubfact = format(model.subfactorial(r), model);

				model.updateResult(Constants.R_FACT, rFact);
				model.updateResult(Constants.R_SUBFACT, rSubfact);

				Integer n = (Integer) data.get(PCModel.Keys.N_VALUE);
				if (n != null) {
					String perm = format(model.permutation(n, r), model);
					String comb = format(model.combination(n, r), model);
					String repPerm = format(model.repetitivePermutation(n, r), model);
					String repComb = format(model.repetitiveCombination(n, r), model);
					String pigeon = format(model.pigeonhole(n, r), model);

					model.updateResult(Constants.PERM, perm);
					model.updateResult(Constants.COMB, comb);
					model.updateResult(Constants.REP_PERM, repPerm);
					model.updateResult(Constants.REP_COMB, repComb);
					model.updateResult(Constants.PIGEONHOLE, pigeon);
				}
			}
		}, PCModel.Types.VALID_R);

		model.addListener(new DataActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void fire(HashMap<Enum<?>, ?> data) {
				int n = (Integer) data.get(PCModel.Keys.N_VALUE);
				ArrayList<Integer> nVals = (ArrayList<Integer>) data.get(PCModel.Keys.N_VALUES);
				String indistinctPerm = format(model.indistinctPermutation(n, nVals), model);

				model.updateResult(Constants.INDISTINCT_PERM, indistinctPerm);
			}
		}, PCModel.Types.VALID_NS);

		model.addListener(new ActionListener() {

			@Override
			public void fire() {
				view.showToast(Constants.MESSAGE_INPUT_OVER_MAX);
			}
		}, PCModel.Types.INPUT_OVER_MAX_VALUE);

		model.addListener(new ActionListener() {

			@Override
			public void fire() {
				showResults(view, model.getResults());
			}
		}, PCModel.Types.DONE_VALIDATING);
	}

	private static void showEmptyResults(PCModel model, PCView view) {
		model.clearResults();
		showResults(view, model.getResults());
	}

	private static void showResults(PCView view, HashMap<String, String> results) {
		for (String title : Constants.PC_TITLES) {
			if (results.get(title) == null) {
				results.put(title, Constants.PC_DEFAULT_RESULT_VALUE);
			}
		}

		view.showResults(results);
	}

	private static void validateInput(PCModel model, PCView view) {
		model.validateInput(view.getNVal(), view.getRVal(), view.getNVals());
	}

	private static String format(BigInteger number, PCModel model) {
		String stringValue;
		if (number.compareTo(BigInteger.valueOf(Constants.MAX_PLAIN_FORMAT)) == 1) {
			stringValue = model.format(number);
		} else {
			stringValue = new DecimalFormat().format(number);
		}
		return stringValue;
	}
}
