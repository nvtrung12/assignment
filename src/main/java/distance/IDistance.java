package distance;

public interface IDistance {
	/**
	 * Distance from 2 text
	 * 
	 * @param phrase1
	 * @param phrase2
	 * @return double value from 0.0 to 1.0
	 */
	public double distance(String phrase1, String phrase2);
}
