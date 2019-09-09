package simple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

public class test {
	public static int combine(int n, int k) {
        if (k > n) {
            return 0;
        } else if (k == 0 || k == n) {
            return 1;
        } else {
            return (combine(n - 1, k) + combine(n - 1, k - 1)); // Binary Recursion
        }
    }
	public static void main(String[] args) {
		MultiValuedMap<String, String> map = new ArrayListValuedHashMap<>();
		map.put("1", "a");
		map.put("1", "b");
		Map<String, Set<String>> maps =new HashMap<>();
		maps.put("a", new HashSet<>());
		System.out.println(maps);
		
		
		System.out.println("combine(10, 2) = " + combine(10, 2));
	}
	

}
