package queries;

import java.io.IOException;

public class TestPerformer {

	public static void main(String[] args) {
		try {
			QueryPerformer performer = new QueryPerformer();
			performer.setQuery("\"22 CA581203\"~4");
			performer.results();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
