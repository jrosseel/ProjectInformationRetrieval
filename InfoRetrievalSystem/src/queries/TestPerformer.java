package queries;

import java.io.IOException;

public class TestPerformer {

	public static void main(String[] args) {
		try {
			QueryPerformer performer = new QueryPerformer();
			// Proximity Query (~ 4 = within 4 words)
			performer.setQuery("\"22 CA581203\"~4");
			// Normal query
			//performer.setQuery("CA581203");
			// Wildcard
			//performer.setQuery("CA*");
			performer.results();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
