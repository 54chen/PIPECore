package uk.ac.imperial.pipe.io.adapters.modelAdapter;

import com.google.common.base.Joiner;
import uk.ac.imperial.pipe.io.adapters.model.AdaptedArc;
import uk.ac.imperial.pipe.models.petrinet.*;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts arcs for writing to PNML.
 */
public class ArcAdapter extends XmlAdapter<AdaptedArc, Arc<? extends Connectable, ? extends Connectable>> {


    private final Map<String, Place> places;

    private final Map<String, Transition> transitions;

    private final Map<String, Token> tokens;

    /**
     * Empty constructor needed for marshalling. Since the method to marshall does not actually
     * use these fields it's ok to initialise them as empty/null.
     */
    public ArcAdapter() {
        places = new HashMap<>();
        transitions = new HashMap<>();
        tokens = new HashMap<>();
    }

    public ArcAdapter(Map<String, Place> places, Map<String, Transition> transitions, Map<String, Token> tokens) {
        this.places = places;
        this.transitions = transitions;
        this.tokens = tokens;
    }

    @Override
    public Arc<? extends Connectable, ? extends Connectable> unmarshal(AdaptedArc adaptedArc)  {
        Arc<? extends Connectable, ? extends Connectable> arc;
        String source = adaptedArc.getSource();
        String target = adaptedArc.getTarget();
        Map<String, String> weights = stringToWeights(adaptedArc.getInscription().getTokenCounts());
        if (adaptedArc.getType().equals("inhibitor")) {
            Place place = places.get(source);
            Transition transition = transitions.get(target);
            arc = new InboundInhibitorArc(place, transition);
        } else {
            if (places.containsKey(source)) {
                Place place = places.get(source);
                Transition transition = transitions.get(target);
                arc = new InboundNormalArc(place, transition, weights);
            } else {
                Place place = places.get(target);
                Transition transition = transitions.get(source);
                arc = new OutboundNormalArc(transition, place, weights);
            }
        }
        arc.setId(adaptedArc.getId());
        //TODO:
        arc.setTagged(false);

        setRealArcPoints(arc, adaptedArc);
        return arc;
    }

    private Map<String, String> stringToWeights(String weights) {
        Map<String, String> tokenWeights = new HashMap<>();
        if (weights.isEmpty()) {
            return tokenWeights;
        }
        String[] commaSeparatedMarkings = weights.split(",");
        if (commaSeparatedMarkings.length == 1) {
            String weight = commaSeparatedMarkings[0];
        } else {
            for (int i = 0; i < commaSeparatedMarkings.length; i += 2) {
                String weight = commaSeparatedMarkings[i + 1].replace("@", ",");
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

    /**
     * @return the default token to use if no token is specified in the
     * Arc weight XML.
     */
    private Token getDefaultToken() {
        return getTokenIfExists("Default");
    }

    /**
     * Sets the arc points in the arc based on the adapted.
     * Loses the source and end locations to just provide intermediate pints
     *
     * @param arc
     * @param adapted
     */
    private void setRealArcPoints(Arc<? extends Connectable, ? extends Connectable> arc, AdaptedArc adapted) {


        List<ArcPoint> arcPoints = adapted.getArcPoints();
        if (arcPoints.isEmpty()) {
            return;
        }

        // Save intermediate points into model
        for (int i = 1; i < arcPoints.size() - 1; i++) {
            arc.addIntermediatePoint(arcPoints.get(i));
        }

    }

    @Override
    public AdaptedArc marshal(Arc<? extends Connectable, ? extends Connectable> arc) {
        AdaptedArc adapted = new AdaptedArc();
        setArcPoints(arc, adapted);
        adapted.setId(arc.getId());
        adapted.setSource(arc.getSource().getId());
        adapted.setTarget(arc.getTarget().getId());
        adapted.getInscription().setTokenCounts(weightToString(arc.getTokenWeights()));
        adapted.setType(arc.getType().name().toLowerCase());
        return adapted;
    }

    private String weightToString(Map<String, String> weights) {
        return Joiner.on(",").withKeyValueSeparator(",").join(weights);
    }

    /**
     * Sets the arc points in adapted based on the arc.
     * Needs to save the source and end locations to be PNML compliant in this
     *
     * @param arc
     * @param adapted
     */
    private void setArcPoints(Arc<? extends Connectable, ? extends Connectable> arc, AdaptedArc adapted) {

        List<ArcPoint> arcPoints = adapted.getArcPoints();
        arcPoints.addAll(arc.getArcPoints());
    }

}
