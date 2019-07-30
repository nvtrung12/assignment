package edu.assessment;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class AnotherTest {

	public static void main(String[] args) {
		(new AnotherTest()).testReadResource();

	}

	private void testReadResource() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("defaults/Ignored_concept_default.txt").getFile());

		try (Scanner scanner = new Scanner(file)) {

			while (scanner.hasNext()) {
				System.out.println(scanner.nextLine());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
