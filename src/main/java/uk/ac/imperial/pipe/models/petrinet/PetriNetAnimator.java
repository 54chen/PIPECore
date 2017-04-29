package uk.ac.imperial.pipe.models.petrinet;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import uk.ac.imperial.state.State;


/**
 * Contains methods to help with animating the Petri net and performs
 * in place modifications to the Petri net.
 */
public final class PetriNetAnimator implements Animator {

//	private static Logger logger = LogManager.getLogger(PetriNetAnimator.class);  
    /**
     * Executable Petri net to animate
     */
    private ExecutablePetriNet executablePetriNet;
    /**
     * Underlying animation logic, which returns logic as Markov Chain states
     */
    private final PetriNetAnimationLogic animationLogic;

    /**
     * Saved State of the underlying
     * Petri net so that it can be reapplied to the Petri net at any time
     */
    private State savedState;

    public PetriNetAnimator(ExecutablePetriNet executablePetriNet) {
    	this.executablePetriNet = executablePetriNet;
    	animationLogic = new PetriNetAnimationLogic(executablePetriNet);
    	saveState();
    	
	}

	/**
     * Save the Petri net state into the saved state map
     */
    @Override
    public void saveState() {
    	savedState = executablePetriNet.getState(); 
    }

    /**
     * Reset the Petri net by applying the saved state back onto the Petri net
     */
    @Override
    public void reset() {
    	executablePetriNet.setState(savedState);
    	animationLogic.stopAnimation();  
    }

    /**
     *
     * @return a random transition which is enabled given the Petri nets current state
     */
    @Override
    public Transition getRandomEnabledTransition() {
    	return animationLogic.getRandomEnabledTransition(executablePetriNet.getState());
    }

    /**
     * @deprecated use {@link #getRandomEnabledTransition()}
     * @return all enabled transitions for the Petri nets current underlying state
     */
    @Deprecated
    @Override
    public Set<Transition> getEnabledTransitions() {
        return animationLogic.getEnabledImmediateOrTimedTransitions();
//        return animationLogic.getEnabledImmediateOrTimedTransitions(executablePetriNet.getState());
    }

   /**
    *
    * Fires the transition if it is enabled in the Petri net for the current underlying state
    *
    * @param transition transition to fire
    */
    public void fireTransition(Transition transition) {
    	animationLogic.getFiredState(transition);  
    }

    /**
     * Undo the firing of the transition
     * @param transition transition to fire backwards
     */
    // TODO: Has to be moved to ExecutablePetriNet - or later better TimedState?.
    @Override
    public void fireTransitionBackwards(Transition transition) {
        TimingQueue timingQueue = executablePetriNet.getTimingQueue();
        // TODO: Move time backward!? = put transition back onto stack
        //Increment previous places
        for (Arc<Place, Transition> arc : executablePetriNet.inboundArcs(transition)) {
            Place place = arc.getSource();
            adjustCount(timingQueue, arc, place, false);
        }
        //Decrement new places
        for (Arc<Transition, Place> arc : executablePetriNet.outboundArcs(transition)) {
            Place place = arc.getTarget(); 
            adjustCount(timingQueue, arc, place, true);
        }
    }
	protected void adjustCount(TimingQueue timingQueue,
			Arc<? extends Connectable,? extends Connectable> arc, Place place, boolean decrement) {
		for (Map.Entry<String, String> entry : arc.getTokenWeights().entrySet()) {
		    String tokenId = entry.getKey();
		    double weight = getWeight(timingQueue, entry);
		    int currentCount = place.getTokenCount(tokenId);
		    int adjust = (decrement) ? -1 : 1; 
		    int newCount = currentCount + adjust * ((int) weight);
		    place.setTokenCount(tokenId, newCount);
		}
	}

	protected double getWeight(TimingQueue timingQueue,
			Map.Entry<String, String> entry) {
		String functionalWeight = entry.getValue();
		return executablePetriNet.getArcWeight(functionalWeight, timingQueue );
	}

	/**
	 * Fire all currently enabled immediate transitions
	 * and afterwards the enabled timed transitions which are due to fire.
	 */
	//FIXME:  halting problem :/
	public void fireAllCurrentEnabledTransitions() {
		Transition nextTransition = animationLogic.getRandomEnabledTransition( );
		if (nextTransition != null) {
			executablePetriNet.fireTransition(nextTransition);
			fireAllCurrentEnabledTransitions();
		}
	}

	/**
	 * Fire all currently enabled immediate transitions
	 * and afterwards the enabled timed transitions which are due to fire.
	 * 
	 * @param state against which transitions are to be fired 
	 */
	//FIXME:  halting problem :/
	public void fireAllCurrentEnabledTransitions(State state) {
		Transition nextTransition = animationLogic.getRandomEnabledTransition( state );
		if (nextTransition != null) {
			State nextState = executablePetriNet.fireTransition(nextTransition, state);
			// updateState = false? 
			executablePetriNet.getTimingQueue().dequeueAndRebuild(nextTransition, nextState); 
			fireAllCurrentEnabledTransitions(state);
		}
	}
    
    /**
     * Advance current time of the Petri Network.
     * Fire all immediate transitions and afterwards step through time
     * always firing the timed transitions that become due to fire.
     * @param newTime to advance the net to
     */
    public void advanceNetToTime(long newTime) {
    	TimingQueue timingQueue = executablePetriNet.getTimingQueue(); 
    	if (newTime > executablePetriNet.getCurrentTime() ) {
    		fireAllCurrentEnabledTransitions();
    		while ( timingQueue.hasUpcomingTimedTransition() ) {
    			long nextFiringTime = timingQueue.getNextFiringTime();
    			if (nextFiringTime < newTime) {
    				executablePetriNet.setCurrentTime(timingQueue.getNextFiringTime());
    				fireAllCurrentEnabledTransitions();
    			} else {
    				timingQueue.setCurrentTime(newTime);
    				break;
    			}
    		}
    		timingQueue.setCurrentTime(newTime);
    	}
    }
    /**
     * Advance current time of the Petri Network.
     * Fire all immediate transitions and afterwards step through time
     * always firing the timed transitions that become due to fire.
     * @param state against which to calculate
     * @param newTime to advance the net to
     */
    public void advanceNetToTime(State state, long newTime) {
    	TimingQueue timingQueue = executablePetriNet.getTimingQueue(); 
    	if (newTime > executablePetriNet.getCurrentTime() ) {
    		fireAllCurrentEnabledTransitions(state);
    		while ( timingQueue.hasUpcomingTimedTransition() ) {
    			long nextFiringTime = timingQueue.getNextFiringTime();
    			if (nextFiringTime < newTime) {
    				executablePetriNet.setCurrentTime(timingQueue.getNextFiringTime());
    				fireAllCurrentEnabledTransitions(state);
    			} else {
    				timingQueue.setCurrentTime(newTime);
    				break;
    			}
    		}
    		timingQueue.setCurrentTime(newTime);
    	}
    }
    
    @Override
    public void setRandom(Random random) {
		animationLogic.setRandom(random);
	}

	@Override
	public AnimationLogic getAnimationLogic() {
		return animationLogic	;
	}

	@Override
	public void startAnimation() {
		saveState();
		animationLogic.startAnimation(); 
	}
}
