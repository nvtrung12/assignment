package simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.math3.analysis.function.Pow;

public class test {

	private static void permute(String input) {
		List<char[]> result = new ArrayList<>();
		int n = input.length();
		int max = 1 << n;
		input = input.toLowerCase();

		for (int i = 0; i < max; i++) {
			char combination[] = input.toCharArray();

			for (int j = 0; j < n; j++) {
				if (((i >> j) & 1) == 1) {
					combination[j] = (char) (combination[j] - 32);
				}
				result = Arrays.asList(combination);

			}

			// result.add(combination.toString());
		}
		
	}

	public static int combine(int n, int k) {// abc Abc aBc ABc abC AbC aBC ABC

		if (k > n) {
			return 0;
		} else if (k == 0 || k == n) {
			return 1;
		} else {
			return (combine(n - 1, k) + combine(n - 1, k - 1)); // Binary Recursion
		}
	}

	public static void main(String[] args) {
		System.out.println(Math.pow(8, 1D/3));
	}

}
