package uk.ac.imperial.pipe.models.petrinet;

import org.apache.commons.collections.CollectionUtils;
import uk.ac.imperial.pipe.exceptions.InvalidRateException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.component.AbstractPetriNetComponent;
import uk.ac.imperial.pipe.models.component.Connectable;
import uk.ac.imperial.pipe.models.component.PetriNetComponent;
import uk.ac.imperial.pipe.models.component.annotation.Annotation;
import uk.ac.imperial.pipe.models.component.arc.Arc;
import uk.ac.imperial.pipe.models.component.arc.InboundArc;
import uk.ac.imperial.pipe.models.component.arc.OutboundArc;
import uk.ac.imperial.pipe.models.component.place.Place;
import uk.ac.imperial.pipe.models.component.rate.NormalRate;
import uk.ac.imperial.pipe.models.component.rate.Rate;
import uk.ac.imperial.pipe.models.component.rate.RateParameter;
import uk.ac.imperial.pipe.models.component.token.Token;
import uk.ac.imperial.pipe.models.component.transition.Transition;
import uk.ac.imperial.pipe.models.petrinet.name.PetriNetName;
import uk.ac.imperial.pipe.parsers.FunctionalResults;
import uk.ac.imperial.pipe.parsers.FunctionalWeightParser;
import uk.ac.imperial.pipe.parsers.PetriNetWeightParser;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

import javax.xml.bind.annotation.XmlTransient;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static java.lang.Math.floor;

public class PetriNet {
    public static final String PETRI_NET_NAME_CHANGE_MESSAGE = "nameChange";

    /**
     * Message fired when an annotation is added to the Petri net
     */
    public static final String NEW_ANNOTATION_CHANGE_MESSAGE = "newAnnotation";

    /**
     * Message fired when a place is deleted from the Petri net
     */
    public static final String DELETE_PLACE_CHANGE_MESSAGE = "deletePlace";

    /**
     * Message fired when an arc is deleted from the Petri net
     */
    public static final String DELETE_ARC_CHANGE_MESSAGE = "deleteArc";

    /**
     * Message fired when a transition is deleted from the Petri net
     */
    public static final String DELETE_TRANSITION_CHANGE_MESSAGE = "deleteTransition";

    /**
     * Message fired when an annotation is deleted from the Petri net
     */
    public static final String DELETE_ANNOTATION_CHANGE_MESSAGE = "deleteAnnotation";

    /**
     * Message fired when a Place is added to the Petri net
     */
    public static final String NEW_PLACE_CHANGE_MESSAGE = "newPlace";

    /**
     * Message fired when a transition is added to the Petri net
     */
    public static final String NEW_TRANSITION_CHANGE_MESSAGE = "newTransition";

    /**
     * Message fired when an arc is added to the Petri net
     */
    public static final String NEW_ARC_CHANGE_MESSAGE = "newArc";

    /**
     * Message fired when a token is added to the Petri net
     */
    public static final String NEW_TOKEN_CHANGE_MESSAGE = "newToken";

    /**
     * Message fired when a token is deleted
     */
    public static final String DELETE_TOKEN_CHANGE_MESSAGE = "deleteToken";

    /**
     * Message fired when a rate parameter is added
     */
    public static final String NEW_RATE_PARAMETER_CHANGE_MESSAGE = "newRateParameter";

    /**
     * Message fired when a rate parameter is deleted
     */
    public static final String DELETE_RATE_PARAMETER_CHANGE_MESSAGE = "deleteRateParameter";

    protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Functional weight parser
     */
    private final FunctionalWeightParser<Double> functionalWeightParser = new PetriNetWeightParser(this);

    //TODO: CYCLIC DEPENDENCY BETWEEN CREATING THIS AND PETRINET/
    private final PetriNetComponentVisitor deleteVisitor = new PetriNetComponentRemovalVisitor(this);

    private final Map<String, Transition> transitions = new HashMap<>();

    private final Map<String, Place> places = new HashMap<>();

    private final Map<String, Token> tokens = new HashMap<>();

    private final Map<String, InboundArc> inboundArcs = new HashMap<>();

    private final Map<String, OutboundArc> outboundArcs = new HashMap<>();

    private final Map<String, RateParameter> rateParameters = new HashMap<>();

    private final Map<String, Annotation> annotations = new HashMap<>();

    /**
     * A tokens that will contain the maps specified above.
     * It's ID is the class type to tokens
     * Sadly need to cast to get the exact tokens back out of it. If you know of a better way to
     * do this then please change it. It is used to easily get a Petrinet component of type T
     * by id.
     */
    private final Map<Class<? extends PetriNetComponent>, Map<String, ? extends PetriNetComponent>> componentMaps =
            new HashMap<>();

    private final PetriNetComponentVisitor addVisitor = new PetriNetComponentAddVisitor(this);

    public String pnmlName = "";


    private PetriNetName petriNetName;

    private boolean validated = false;


    public PetriNet(PetriNetName name) {
        this();
        this.petriNetName = name;
    }

    //TODO: INITIALSE NAME?
    public PetriNet() {
        initialiseIdMap();
    }

    private void initialiseIdMap() {
        componentMaps.put(Place.class, places);
        componentMaps.put(Transition.class, transitions);
        componentMaps.put(InboundArc.class, inboundArcs);
        componentMaps.put(OutboundArc.class, outboundArcs);
        componentMaps.put(Token.class, tokens);
        componentMaps.put(RateParameter.class, rateParameters);
        componentMaps.put(Annotation.class, annotations);
    }

    @Override
    public int hashCode() {
        int result = transitions.hashCode();
        result = 31 * result + places.hashCode();
        result = 31 * result + tokens.hashCode();
        result = 31 * result + inboundArcs.hashCode();
        result = 31 * result + outboundArcs.hashCode();
        result = 31 * result + annotations.hashCode();
        result = 31 * result + rateParameters.hashCode();
        result = 31 * result + (petriNetName != null ? petriNetName.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PetriNet)) {
            return false;
        }

        PetriNet petriNet = (PetriNet) o;


        if (!CollectionUtils.isEqualCollection(annotations.values(), petriNet.annotations.values())) {
            return false;
        }
        if (!CollectionUtils.isEqualCollection(inboundArcs.values(), petriNet.inboundArcs.values())) {
            return false;
        }
        if (!CollectionUtils.isEqualCollection(outboundArcs.values(), petriNet.outboundArcs.values())) {
            return false;
        }
        if (petriNetName != null ? !petriNetName.equals(petriNet.petriNetName) : petriNet.petriNetName != null) {
            return false;
        }
        if (!CollectionUtils.isEqualCollection(places.values(), petriNet.places.values())) {
            return false;
        }
        if (!CollectionUtils.isEqualCollection(rateParameters.values(), petriNet.rateParameters.values())) {
            return false;
        }
        if (!CollectionUtils.isEqualCollection(tokens.values(), petriNet.tokens.values())) {
            return false;
        }
        if (!CollectionUtils.isEqualCollection(transitions.values(), petriNet.transitions.values())) {
            return false;
        }

        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    @XmlTransient
    public String getPnmlName() {
        return pnmlName;
    }

    public void setPnmlName(String pnmlName) {
        this.pnmlName = pnmlName;
    }

    @XmlTransient
    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public void resetPNML() {
        pnmlName = null;
    }

    /**
     * Adds place to the Petri net
     *
     * @param place place to add to Petri net
     */
    public void addPlace(Place place) {
        if (!places.containsValue(place)) {
            places.put(place.getId(), place);
            place.addPropertyChangeListener(new NameChangeListener<>(place, places));
            changeSupport.firePropertyChange(NEW_PLACE_CHANGE_MESSAGE, null, place);
        }
    }

    /**
     * @return all Places currently in the Petri net
     */
    public Collection<Place> getPlaces() {
        return places.values();
    }

    /**
     * Removes the place and all arcs connected to the place from the
     * Petri net
     *
     * @param place to remove from Petri net
     */
    public void removePlace(Place place) {
        this.places.remove(place.getId());
        for (InboundArc arc : outboundArcs(place)) {
            removeArc(arc);
        }
        changeSupport.firePropertyChange(DELETE_PLACE_CHANGE_MESSAGE, place, null);
    }

    /**
     * @param place
     * @return arcs that are outbound from place
     */
    public Collection<InboundArc> outboundArcs(Place place) {
        Collection<InboundArc> outbound = new LinkedList<>();
        for (InboundArc arc : inboundArcs.values()) {
            if (arc.getSource().equals(place)) {
                outbound.add(arc);
            }
        }
        return outbound;
    }

    /**
     * Removes the specified arc from the Petri net
     *
     * @param arc to remove from the Petri net
     */
    public void removeArc(InboundArc arc) {
        inboundArcs.remove(arc.getId());
        changeSupport.firePropertyChange(DELETE_ARC_CHANGE_MESSAGE, arc, null);
    }

    /**
     * Adds transition to the Petri net
     *
     * @param transition transition to add to the Petri net
     */
    public void addTransition(Transition transition) {
        if (!transitions.containsValue(transition)) {
            transitions.put(transition.getId(), transition);
            transition.addPropertyChangeListener(new NameChangeListener<>(transition, transitions));
            changeSupport.firePropertyChange(NEW_TRANSITION_CHANGE_MESSAGE, null, transition);
        }
    }

    /**
     * Removes transition from the petri net. Also removes any arcs connected
     * to this transition
     *
     * @param transition to remove
     */
    public void removeTransition(Transition transition) {
        this.transitions.remove(transition.getId());
        for (OutboundArc arc : outboundArcs(transition)) {
            removeArc(arc);
        }
        changeSupport.firePropertyChange(DELETE_TRANSITION_CHANGE_MESSAGE, transition, null);
    }

    /**
     * An outbound arc of a transition is any arc that starts at the transition
     * and connects elsewhere
     *
     * @param transition to find outbound arcs for
     * @return arcs that are outbound from transition
     */
    public Collection<OutboundArc> outboundArcs(Transition transition) {
        Collection<OutboundArc> outbound = new LinkedList<>();
        for (OutboundArc arc : outboundArcs.values()) {
            if (arc.getSource().equals(transition)) {
                outbound.add(arc);
            }
        }
        return outbound;
    }

    /**
     * Removes the specified arc from the Petri net
     *
     * @param arc to remove from the Petri net
     */
    public void removeArc(OutboundArc arc) {
        outboundArcs.remove(arc.getId());
        changeSupport.firePropertyChange(DELETE_ARC_CHANGE_MESSAGE, arc, null);
    }

    /**
     * @return all transitions in the Petri net
     */
    public Collection<Transition> getTransitions() {
        return transitions.values();
    }

    public void addArc(InboundArc inboundArc) {
        if (!inboundArcs.containsKey(inboundArc.getId())) {
            inboundArcs.put(inboundArc.getId(), inboundArc);
            inboundArc.addPropertyChangeListener(new NameChangeListener<>(inboundArc, inboundArcs));
            changeSupport.firePropertyChange(NEW_ARC_CHANGE_MESSAGE, null, inboundArc);
        }
    }

    public void addArc(OutboundArc outboundArc) {
        if (!outboundArcs.containsKey(outboundArc.getId())) {
            outboundArcs.put(outboundArc.getId(), outboundArc);
            outboundArc.addPropertyChangeListener(new NameChangeListener<>(outboundArc, outboundArcs));
            changeSupport.firePropertyChange(NEW_ARC_CHANGE_MESSAGE, null, outboundArc);
        }
    }

    /**
     * @return Petri net's collection of arcs
     */
    public Collection<Arc<? extends Connectable, ? extends Connectable>> getArcs() {
        Collection<Arc<? extends Connectable, ? extends Connectable>> arcs = new LinkedList<>();
        arcs.addAll(getOutboundArcs());
        arcs.addAll(getInboundArcs());
        return arcs;
    }

    public Collection<OutboundArc> getOutboundArcs() {
        return outboundArcs.values();
    }

    public Collection<InboundArc> getInboundArcs() {
        return inboundArcs.values();
    }

    /**
     * Adds the token to the Petri net
     *
     * @param token
     */
    public void addToken(Token token) {
        if (!tokens.containsValue(token)) {
            tokens.put(token.getId(), token);
            token.addPropertyChangeListener(new NameChangeListener<>(token, tokens));
            token.addPropertyChangeListener(new TokenNameChanger());
            changeSupport.firePropertyChange(NEW_TOKEN_CHANGE_MESSAGE, null, token);
        }
    }

    /**
     * Tries to remove the token
     *
     * @param token token to remove
     * @throws PetriNetComponentException if places or transitions reference this token!
     */
    public void removeToken(Token token) throws PetriNetComponentException {
        Collection<Place> referencedPlaces = getPlacesContainingToken(token);
        Collection<Transition> referencedTransitions = getTransitionsReferencingToken(token);
        if (referencedPlaces.isEmpty() && referencedTransitions.isEmpty()) {
            tokens.remove(token.getId());
            changeSupport.firePropertyChange(DELETE_TOKEN_CHANGE_MESSAGE, token, null);
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append("Cannot remove Default token");
        if (!referencedPlaces.isEmpty()) {
            message.append(" places: ");
            for (Place place : referencedPlaces) {
                message.append(place.getId());
            }
            message.append(" contain it\n");
        }
        if (!referencedTransitions.isEmpty()) {
            message.append(" transitions: ");
            for (Transition transition : referencedTransitions) {
                message.append(transition.getId());
            }
            message.append(" reference it\n");
        }

        throw new PetriNetComponentException(message.toString());
    }

    /**
     * @param token
     * @return collection of Places that contain 1 or more of these tokens
     */
    private Collection<Place> getPlacesContainingToken(Token token) {
        Collection<Place> result = new LinkedList<>();
        for (Place place : places.values()) {
            if (place.getTokenCount(token.getId()) > 0) {
                result.add(place);
            }
        }
        return result;
    }

    /**
     * @param token
     * @return list of transitions that reference the token in their rate expression
     */
    private Collection<Transition> getTransitionsReferencingToken(Token token) {
        Collection<Transition> result = new LinkedList<>();
        for (Transition transition : transitions.values()) {
            FunctionalResults<Double> results = functionalWeightParser.evaluateExpression(transition.getRateExpr());
            if (results.getComponents().contains(token.getId())) {
                result.add(transition);
            }
        }
        return result;
    }

    /**
     * @return Petri net's list of tokens
     */
    public Collection<Token> getTokens() {
        return tokens.values();
    }

    /**
     * Adds the annotation to the Petri net
     *
     * @param annotation
     */
    public void addAnnotation(Annotation annotation) {
        if (!annotations.containsKey(annotation.getId())) {
            annotations.put(annotation.getId(), annotation);
            annotation.addPropertyChangeListener(new NameChangeListener<>(annotation, annotations));
            changeSupport.firePropertyChange(NEW_ANNOTATION_CHANGE_MESSAGE, null, annotation);
        }
    }

    /**
     * Removes the specified annotation from the Petri net
     *
     * @param annotation annotation to remove
     */
    public void removeAnnotaiton(Annotation annotation) {
        annotations.remove(annotation.getId());
        changeSupport.firePropertyChange(DELETE_ANNOTATION_CHANGE_MESSAGE, annotation, null);
    }

    /**
     * @return annotations stored in the Petri net
     */
    public Collection<Annotation> getAnnotations() {
        return annotations.values();
    }

    /**
     * Adds the RateParameter to the Petri Net
     *
     * @param rateParameter to add to Petri net
     * @throws InvalidRateException if the rate is not parseable
     */
    public void addRateParameter(RateParameter rateParameter) throws InvalidRateException {
        if (!validFunctionalExpression(rateParameter.getExpression())) {
            throw new InvalidRateException(rateParameter.getExpression());
        }

        if (!rateParameters.containsValue(rateParameter)) {
            rateParameters.put(rateParameter.getId(), rateParameter);
            rateParameter.addPropertyChangeListener(new NameChangeListener<>(rateParameter, rateParameters));
            changeSupport.firePropertyChange(NEW_RATE_PARAMETER_CHANGE_MESSAGE, null, rateParameter);
        }
    }

    /**
     * Attempts to parse the expression of the rate
     *
     * @param expression functional expression to evaluate for Petri net
     * @return false if the rate's expression is invalid
     */
    public boolean validFunctionalExpression(String expression) {
        FunctionalResults<Double> result = functionalWeightParser.evaluateExpression(expression);
        return !result.hasErrors();
    }

    /**
     * Removes the rate parameter from the Petri net.
     * <p/>
     * Any transitions referencing this rate parameter will have their rates
     * set to the last value of the rate parameter
     *
     * @param parameter rate parameter to remove
     */
    public void removeRateParameter(RateParameter parameter) {
        removeRateParameterFromTransitions(parameter);
        rateParameters.remove(parameter.getId());
        changeSupport.firePropertyChange(DELETE_RATE_PARAMETER_CHANGE_MESSAGE, parameter, null);
    }

    /**
     * Removes the Rate Parameter from any transitions that refer to it
     * and replaces it with a {@link uk.ac.imperial.pipe.models.component.rate.NormalRate} with the
     * same value
     *
     * @param parameter to remove
     */
    private void removeRateParameterFromTransitions(RateParameter parameter) {
        for (Transition transition : transitions.values()) {
            if (transition.getRate().equals(parameter)) {
                Rate rate = new NormalRate(parameter.getExpression());
                transition.setRate(rate);
            }
        }
    }

    /**
     * @return rate parameters stored in the Petri net
     */
    public Collection<RateParameter> getRateParameters() {
        return rateParameters.values();
    }

    /**
     * Add any Petri net component to this Petri net
     *
     * @param component
     * @throws PetriNetComponentException
     */
    public void add(PetriNetComponent component) throws PetriNetComponentException {
        component.accept(addVisitor);
    }

    /**
     * Remove any Petri net component from the Petri net
     *
     * @param component component to remove
     * @throws PetriNetComponentException
     */
    public void remove(PetriNetComponent component) throws PetriNetComponentException {
        component.accept(deleteVisitor);
    }

    /**
     * @return true if the Petri net contains a default token
     */
    public boolean containsDefaultToken() {
        return tokens.containsKey("Default");
    }

    /**
     * @param id
     * @return true if any component in the Petri net has this id
     */
    public boolean containsComponent(String id) {
        for (Map<String, ? extends PetriNetComponent> map : componentMaps.values()) {
            if (map.containsKey(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param id    component name
     * @param clazz PetriNetComponent class
     * @param <T>   type of Petri net component required
     * @return component with the specified id if it exists in the Petri net
     * @throws PetriNetComponentNotFoundException if component does not exist in Petri net
     */
    public <T extends PetriNetComponent> T getComponent(String id, Class<T> clazz)
            throws PetriNetComponentNotFoundException {
        Map<String, T> map = getMapForClass(clazz);
        if (map.containsKey(id)) {
            return map.get(id);
        }
        throw new PetriNetComponentNotFoundException("No component " + id + " exists in Petri net.");
    }

    private <T extends PetriNetComponent> Map<String, T> getMapForClass(Class<T> clazz) {
        return (Map<String, T>) componentMaps.get(clazz);
    }

    //TODO: REplace with getComponent
    public RateParameter getRateParameter(String rateParameterId) throws PetriNetComponentNotFoundException {
        for (RateParameter rateParameter : rateParameters.values()) {
            if (rateParameter.getId().equals(rateParameterId)) {
                return rateParameter;
            }
        }
        throw new PetriNetComponentNotFoundException("No rate parameter " + rateParameterId + " exists in Petri net.");
    }

    /**
     * Calculates weights of connections from places to transitions for given token
     *
     * @param token calculates backwards incidence matrix for this token
     */
    public IncidenceMatrix getBackwardsIncidenceMatrix(String token) {
        IncidenceMatrix backwardsIncidenceMatrix = new IncidenceMatrix();
        for (InboundArc arc : inboundArcs.values()) {
            Transition transition = arc.getTarget();

            Place place = arc.getSource();
            int enablingDegree = transition.isInfiniteServer() ? getEnablingDegree(transition) : 0;


            String expression = arc.getWeightForToken(token);
            int weight = getEvaluatedExpressionAsInt(expression);
            int totalWeight = transition.isInfiniteServer() ? weight * enablingDegree : weight;

            backwardsIncidenceMatrix.put(place, transition, totalWeight);
        }
        return backwardsIncidenceMatrix;
    }

    /**
     * A Transition is enabled if all its input places are marked with at least one token
     * This method calculates the minimium number of tokens needed in order for a transition to be enabeld
     * <p/>
     * The enabling degree is the number of times that a transition is enabled
     *
     * @param transition to work out enabling degree of
     * @return the transitions enabling degree
     */
    public int getEnablingDegree(Transition transition) {
        int enablingDegree = Integer.MAX_VALUE;

        for (Arc<Place, Transition> arc : inboundArcs(transition)) {
            Place place = arc.getSource();
            Map<String, String> arcWeights = arc.getTokenWeights();
            for (Map.Entry<String, String> entry : arcWeights.entrySet()) {
                String arcToken = entry.getKey();
                String arcTokenExpression = entry.getValue();

                //TODO: SHOULD WE FLOOR?
                int result = getEvaluatedExpressionAsInt(arcTokenExpression);
                int requiredTokenCount = (int) floor(result);
                if (requiredTokenCount == 0) {
                    enablingDegree = 0;
                } else {
                    int placeTokenCount = place.getTokenCount(arcToken);
                    int currentDegree = (int) floor(placeTokenCount / requiredTokenCount);
                    if (currentDegree < enablingDegree) {
                        enablingDegree = currentDegree;
                    }
                }
            }
        }
        return enablingDegree;
    }

    //TODO: SHOULD WE BE CATCHING THE ERROR?
    private int getEvaluatedExpressionAsInt(String expression) {
        FunctionalResults<Double> result = functionalWeightParser.evaluateExpression(expression);
        return (int) result.getResult().doubleValue();
    }

    /**
     * @param transition to calculate inbound arc for
     * @return arcs that are inbound to transition, that is arcs that come into the transition
     */
    public Collection<InboundArc> inboundArcs(Transition transition) {
        Collection<InboundArc> outbound = new LinkedList<>();
        for (InboundArc arc : inboundArcs.values()) {
            if (arc.getTarget().equals(transition)) {
                outbound.add(arc);
            }
        }
        return outbound;
    }

    /**
     * Calculates weights of connections from transitions to places for given token
     *
     * @param token token to calculate incidence matrix for
     * @return forwards incidence matrix for the specified token. That is the
     * token weights needed in order to fire an arc from transition to place
     */
    public IncidenceMatrix getForwardsIncidenceMatrix(String token) {

        IncidenceMatrix forwardsIncidenceMatrix = new IncidenceMatrix();
        for (OutboundArc arc : outboundArcs.values()) {
            Place place = arc.getTarget();
            Transition transition = arc.getSource();

            String expression = arc.getWeightForToken(token);
            int weight = getEvaluatedExpressionAsInt(expression);
            forwardsIncidenceMatrix.put(place, transition, weight);
        }
        return forwardsIncidenceMatrix;
    }

    @XmlTransient
    public PetriNetName getName() {
        return petriNetName;
    }

    public void setName(PetriNetName name) {
        PetriNetName old = this.petriNetName;
        this.petriNetName = name;
        changeSupport.firePropertyChange(PETRI_NET_NAME_CHANGE_MESSAGE, old, name);
    }

    public String getNameValue() {
        return petriNetName.getName();
    }

    public FunctionalResults<Double> parseExpression(String expr) {
        return functionalWeightParser.evaluateExpression(expr);
    }

    /**
     * Listener for changing a components name in the set it is referenced by
     * @param <T>
     */
    private static class NameChangeListener<T extends PetriNetComponent> implements PropertyChangeListener {
        private final T component;

        private final Map<String, T> componentMap;

        public NameChangeListener(T component, Map<String, T> componentMap) {
            this.component = component;
            this.componentMap = componentMap;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(AbstractPetriNetComponent.ID_CHANGE_MESSAGE)) {
                String oldId = (String) evt.getOldValue();
                String newId = (String) evt.getNewValue();
                componentMap.remove(oldId);
                componentMap.put(newId, component);
            }

        }
    }

    private class TokenNameChanger implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(AbstractPetriNetComponent.ID_CHANGE_MESSAGE)) {
                String oldId = (String) evt.getOldValue();
                String newId = (String) evt.getNewValue();
                changePlaceTokens(oldId, newId);
                changeArcTokens(oldId, newId);
            }
        }


        /**
         *
         * Changes references of token counts in place containing old id to new id
         *
         * @param oldId old token id
         * @param newId new token id
         */
        private void changePlaceTokens(String oldId, String newId) {
            for (Place place : getPlaces()) {
                int count = place.getTokenCount(oldId);
                place.removeAllTokens(oldId);
                place.setTokenCount(newId, count);
            }
        }

        /**
         *
         * Changes references of token weights in arcs from old id to new id
         *
         * @param oldId old token id
         * @param newId new token id
         */
        private void changeArcTokens(String oldId, String newId) {
            for (Arc<? extends Connectable, ? extends Connectable> arc : getArcs()) {
                if (arc.getTokenWeights().containsKey(oldId)) {
                    String weight = arc.getWeightForToken(oldId);
                    arc.removeAllTokenWeights(oldId);
                    arc.setWeight(newId, weight);
                }
            }
        }
    }
}
