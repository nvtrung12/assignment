package distance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JaccardDistance extends MeaningConnection implements IDistance {
	public double getRelation(String phrase1, String phrase2) {
		return this.distance(phrase1, phrase2);
	}

	@Override
	public double distance(String phrase1, String phrase2) {
		if (null == phrase1 || null == phrase2)
			return 0.0;

		String[] arr1 = phrase1.split(" ");
		String[] arr2 = phrase2.split(" ");

		List<String> s1 = Arrays.asList(arr1);
		Set<String> s2 = new HashSet<String>(Arrays.asList(arr2));

		List<String> intersect = s1.stream().filter(s2::contains).collect(Collectors.toList());
		return (float) intersect.size() / (s1.size() + s2.size());
	}
}
