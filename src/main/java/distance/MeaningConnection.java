package distance;

/***
 * Implement simple meaning connection. More complex meaning may be add later
 * 
 */
public class MeaningConnection implements IDistance {

	public double getRelation(String phrase1, String phrase2) {
		if (phrase1.contains(phrase2))
			return 1.0;

		if (phrase2.contains(phrase1))
			return 1.0;

		return 0.0;
	}

	@Override
	public double distance(String phrase1, String phrase2) {
		return this.getRelation(phrase1, phrase2);
	}
}
