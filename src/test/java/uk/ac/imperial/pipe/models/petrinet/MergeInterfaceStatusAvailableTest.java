package uk.ac.imperial.pipe.models.petrinet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentException;
import uk.ac.imperial.pipe.models.petrinet.name.NormalPetriNetName;

//TODO break out tests for AddPlaceStatusCommand
public class MergeInterfaceStatusAvailableTest {

	private IncludeHierarchy includes;
	private PetriNet net;
	private Place place;
	private PlaceStatus status;
	private IncludeHierarchyCommand<Place> command;
	private IncludeHierarchy include2;
	
	@Before
	public void setUp() throws Exception {
		net = buildNet(1); 
		includes = new IncludeHierarchy(net, "top"); 
		place = new DiscretePlace("P10");
		status = new PlaceStatusInterface(place, includes); 
		place.setStatus(status); 
		PetriNet net2 = buildNet(2); 
		includes.include(net2, "a"); 
		include2 = includes.getInclude("a"); 
		net2.addPlace(place); 
		MergeInterfaceStatusHome mergeStatus = new MergeInterfaceStatusHome(place, status); 
		status.setMergeInterfaceStatus(mergeStatus); // normally automatic
		Result<InterfacePlaceAction> result = mergeStatus.add(include2); 
	}
	@Test
	public void cantRemoveAvailableStatusFromInterfacePlaces() throws Exception {
		Place place = includes.getInterfacePlace("a.P10"); 
		MergeInterfaceStatus status = place.getStatus().getMergeInterfaceStatus();
		assertTrue(status instanceof MergeInterfaceStatusAvailable);
		assertFalse(status.canRemove()); 
		Result<InterfacePlaceAction> result = status.remove(includes); 
		assertTrue(result.hasResult()); 
		assertEquals("MergeInterfaceStatusAvailable.remove: not supported for Available status.  " +
				"Must be issued by MergeInterfaceStatusHome against the home include hierarchy.", result.getMessage()); 
	}
	//TODO verifyReturnsResultIfIncludeHierarchyIsDifferentFromOneToWhichThisStatusBelongs
	
	protected PetriNet buildNet(int i) throws PetriNetComponentException {
		PetriNet net = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
    			APlace.withId("P1")).and(AnImmediateTransition.withId("T0")).andFinally(
    			ANormalArc.withSource("P0").andTarget("T0"));
		net.setName(new NormalPetriNetName("net"+i));
		return net;
	}

}
