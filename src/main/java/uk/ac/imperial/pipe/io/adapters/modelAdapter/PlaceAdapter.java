package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import com.google.common.base.Joiner;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedPlace;
import uk.ac.imperial.pipe.io.adapters.model.NameDetails;
import uk.ac.imperial.pipe.io.adapters.model.OffsetGraphics;
import uk.ac.imperial.pipe.io.adapters.model.Point;
import uk.ac.imperial.pipe.io.adapters.utils.ConnectableUtils;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.token.Token;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class PlaceAdapter extends XmlAdapter<AdaptedPlace, Place> {
    private final Map<String, Place> places;

    private final Map<String, Token> tokens;

    /**
     * Empty constructor needed formarshallingg. Since the method to marshall does not actually
     * use these fields it's ok to initialise them as empty/null.
     */
    public PlaceAdapter() {
        places = new HashMap<>();
        tokens = new HashMap<>();
    }

    public PlaceAdapter(Map<String, Place> places, Map<String, Token> tokens) {
        this.tokens = tokens;
        this.places = places;
    }

    @Override
    public Place unmarshal(AdaptedPlace adaptedPlace) {
        NameDetails nameDetails = adaptedPlace.getName();
        Place place = new Place(adaptedPlace.getId(), nameDetails.getName());
        place.setCapacity(adaptedPlace.getCapacity());
        ConnectableUtils.setConnectablePosition(place, adaptedPlace);
        ConnectableUtils.setConntactableNameOffset(place, adaptedPlace);
        place.setTokenCounts(stringToWeights(adaptedPlace.getInitialMarking().getTokenCounts()));
        places.put(place.getId(), place);
        return place;
    }

    @Override
    public AdaptedPlace marshal(Place place) {
        AdaptedPlace adapted = new AdaptedPlace();
        adapted.setId(place.getId());
        ConnectableUtils.setAdaptedName(place, adapted);
        ConnectableUtils.setPosition(place, adapted);

        adapted.setCapacity(place.getCapacity());
        adapted.getInitialMarking().setTokenCounts(weightToString(place.getTokenCounts()));


        OffsetGraphics offsetGraphics = new OffsetGraphics();
        offsetGraphics.point = new Point();
        offsetGraphics.point.setX(place.getMarkingXOffset());
        offsetGraphics.point.setY(place.getMarkingYOffset());
        adapted.getInitialMarking().setGraphics(offsetGraphics);


        return adapted;
    }

    private String weightToString(Map<String, Integer> weights) {
        return Joiner.on(",").withKeyValueSeparator(",").join(weights);
    }

    public Map<String, Integer> stringToWeights(String value) {
        Map<String, Integer> tokenWeights = new HashMap<>();
        if (value.isEmpty()) {
            return tokenWeights;
        }

        String[] commaSeparatedMarkings = value.split(",");
        if (commaSeparatedMarkings.length == 1) {
            Integer weight = Integer.valueOf(commaSeparatedMarkings[0]);
            tokenWeights.put("Default", weight);
        } else {
            for (int i = 0; i < commaSeparatedMarkings.length; i += 2) {
                Integer weight = Integer.valueOf(commaSeparatedMarkings[i + 1].replace("@", ","));
                String tokenName = commaSeparatedMarkings[i];
                tokenWeights.put(tokenName, weight);
            }
        }
        return tokenWeights;
    }

    /**
     * @param tokenName token to find in {@link this.tokens}
     * @return token if exists
     * @throws RuntimeException if token does not exist
     */
    private Token getTokenIfExists(String tokenName) {
        if (!tokens.containsKey(tokenName)) {
            throw new RuntimeException("No " + tokenName + " token exists!");
        }
        return tokens.get(tokenName);
    }
}
