package webservice;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 */
public class StatisticalUtils {
	/**
	 * return frequency to draw chart
	 * 
	 * @param data
	 * @return
	 */
	public static int[] histogram(Map<String, Integer> data) {
		Map<Integer, Integer> hisData = new HashMap<>();
		int min = 0, max = 0;
		for (String key : data.keySet()) {
			Integer val = data.get(key);
			Integer oldVal = hisData.get(val);
			hisData.put(val, null == oldVal ? 1 : 1 + oldVal);
		}
		min = hisData.keySet().stream().mapToInt(i -> i).min().getAsInt();
		max = hisData.keySet().stream().mapToInt(i -> i).max().getAsInt();

		int[] ret = new int[max + 1];
		for (int i = 0; i <= max; ++i) {
			Integer val = hisData.get(i);
			if (null == val)
				val = 0;
			ret[i] = val;
		}

		return ret;
	}
}
