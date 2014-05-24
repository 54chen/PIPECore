package uk.ac.imperial.pipe.dsl;

import uk.ac.imperial.pipe.models.component.place.DiscretePlace;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * Usage:
 * APlace.withId("P0").andCapacity(5).containing(5, "Red).tokens();
 */
public class APlace implements DSLCreator<Place> {
    private String id;
    private int capacity;
    private Map<String, Integer> tokenCounts = new HashMap<>();

    int x = 0;
    int y = 0;

    private APlace(String id) { this.id = id; }

    public static APlace withId(String id) {
        return new APlace(id);
    }

    public APlace andCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public APlace containing(int count, String name) {
        tokenCounts.put(name, count);
        return this;
    }

    /**
     * Added for readability
     * E.g. containing(5, "Default).tokens()
     */
    public APlace tokens() {
        return this;
    }

    /**
     * Added for readability
     * E.g. containing(1, "Default).token()
     */
    public APlace token() {
        return this;
    }

    @Override
    public Place create(Map<String, Token> tokens, Map<String, Place> places, Map<String, Transition> transitions, Map<String, RateParameter> rateParameters) {
        Place place = new DiscretePlace(id, id);
        place.setX(x);
        place.setY(y);

        place.setCapacity(capacity);
        place.setTokenCounts(tokenCounts);

        places.put(id, place);
        return place;
    }


    /**
     * Chains adding tokens
     * E.g.
     * contains(1, "Red").token().and(2, "Blue").tokens();
     * @param count token count
     * @param tokenName token name
     * @return instance of APlace for chaining
     */
    public APlace and(int count, String tokenName) {
        tokenCounts.put(tokenName, count);
        return this;
    }

    public APlace locatedAt(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
}
