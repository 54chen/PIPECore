package uk.ac.imperial.pipe.models.petrinet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArcTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    Place mockSource;

    @Mock
    Transition mockTarget;

    @Mock
    private PropertyChangeListener mockListener;

    Arc<Place, Transition> arc;

    @Before
    public void setUp() {
        when(mockSource.getId()).thenReturn("source");
        when(mockTarget.getId()).thenReturn("target");
        when(mockSource.getArcEdgePoint(anyDouble())).thenReturn(new Point2D.Double(0, 0));
        when(mockTarget.getArcEdgePoint(anyDouble())).thenReturn(new Point2D.Double(0, 0));
        when(mockSource.getStatus()).thenReturn(new PlaceStatusNormal(mockSource));
    }

    @Test
    public void gettingStartReturnsEndPoint() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(65, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.getStartPoint();
        verify(mockSource).getArcEdgePoint(Math.toRadians(0));
    }

    @Test
    public void calculatesCorrectAngleTargetRightOfSource() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(65, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.getEndPoint();
        verify(mockTarget).getArcEdgePoint(Math.toRadians(0));
    }

    @Test
    public void calculatesCorrectAngleTargetLeftOfSource() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(65, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.getEndPoint();
        verify(mockTarget).getArcEdgePoint(Math.toRadians(180));
    }

    @Test
    public void calculatesCorrectAngleTargetBelowSource() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 65));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.getEndPoint();
        verify(mockTarget).getArcEdgePoint(Math.toRadians(90));
    }

    @Test
    public void calculatesCorrectAngleTargetAboveSource() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.getEndPoint();
        verify(mockTarget).getArcEdgePoint(Math.toRadians(-90));
    }

    @Test
    public void returnsTokenWeightForToken() {
        String weight = "cap(P0)";
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.setWeight("Default", weight);
        String actualWeight = arc.getWeightForToken("Default");
        assertEquals(weight, actualWeight);
    }

    @Test
    public void returnsZeroWeightForNonExistentToken() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());
        String actualWeight = arc.getWeightForToken("Default");
        assertEquals("0", actualWeight);
    }

    @Test
    public void returnTrueIfHasFunctionalWeight() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.setWeight("Default", "2");
        arc.setWeight("Red", "cap(P0)");

        assertTrue(arc.hasFunctionalWeight());
    }

    @Test
    public void returnFalseIfNoFunctionalWeight() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        arc.setWeight("Default", "2");
        arc.setWeight("Red", "4");

        assertFalse(arc.hasFunctionalWeight());
    }

    @Test
    public void createsId() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());
        assertEquals("source TO target", arc.getId());
    }

    @Test
    public void sourceReturnsTargetAsNextIfNoIntermediatePoints() {
        Point2D.Double center = mock(Point2D.Double.class);
        when(mockSource.getCentre()).thenReturn(center);
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(0, 0));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(0, 0));

        Point2D.Double targetEnd = mock(Point2D.Double.class);
        when(mockTarget.getArcEdgePoint(anyDouble())).thenReturn(targetEnd);
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        ArcPoint point = new ArcPoint(center, false);
        ArcPoint actualPoint = arc.getNextPoint(point);
        ArcPoint expectedPoint = new ArcPoint(targetEnd, false);
        assertEquals(expectedPoint, actualPoint);
    }

    @Test
    public void sourceReturnsFirstIntermediatePoint() {
        Point2D.Double center = mock(Point2D.Double.class);
        when(mockSource.getCentre()).thenReturn(center);
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        ArcPoint point = new ArcPoint(center, false);
        ArcPoint intermediate = new ArcPoint(new Point2D.Double(1, 5), false);
        arc.addIntermediatePoint(intermediate);
        ArcPoint actualPoint = arc.getNextPoint(point);
        assertEquals(intermediate, actualPoint);
    }

    @Test
    public void intermediateReturnsNextIntermediatePoint() {
        Point2D.Double center = mock(Point2D.Double.class);
        when(mockSource.getCentre()).thenReturn(center);
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        ArcPoint intermediate = new ArcPoint(new Point2D.Double(1, 5), false);
        ArcPoint intermediate2 = new ArcPoint(new Point2D.Double(5, 6), true);
        arc.addIntermediatePoint(intermediate);
        arc.addIntermediatePoint(intermediate2);
        ArcPoint actualPoint = arc.getNextPoint(intermediate);
        assertEquals(intermediate2, actualPoint);
    }

    @Test
    public void lastIntermediateReturnsTarget() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(65, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        Point2D.Double targetEnd = mock(Point2D.Double.class);
        when(mockTarget.getArcEdgePoint(anyDouble())).thenReturn(targetEnd);
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        ArcPoint intermediate = new ArcPoint(new Point2D.Double(1, 1), false);
        arc.addIntermediatePoint(intermediate);
        ArcPoint actualPoint = arc.getNextPoint(intermediate);
        ArcPoint expectedPoint = new ArcPoint(targetEnd, false);
        assertEquals(expectedPoint, actualPoint);
    }

    @Test
    public void deletesCorrectArcPoint() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        ArcPoint intermediate = new ArcPoint(new Point2D.Double(1, 1), false);
        arc.addIntermediatePoint(intermediate);
        ArcPoint intermediate2 = new ArcPoint(new Point2D.Double(2, 2), false);
        arc.addIntermediatePoint(intermediate2);

        arc.removeIntermediatePoint(intermediate);
        assertThat(arc.getArcPoints()).doesNotContain(intermediate);
        assertThat(arc.getArcPoints()).contains(intermediate2);
    }

    @Test
    public void throwsExceptionIfNoNextPoint() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(15, 65));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("No next point");
        ArcPoint point = new ArcPoint(new Point2D.Double(20, 15), false);
        arc.getNextPoint(point);
    }

    @Test
    public void notifiesObserverOnWeightChange() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(65, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());
        arc.addPropertyChangeListener(mockListener);
        arc.setWeight("default", "2");
        verify(mockListener).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnSourceChange() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(65, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());
        arc.addPropertyChangeListener(mockListener);
        arc.setSource(new DiscretePlace("P0")); // also forces ID to be rebuilt
        verify(mockListener, times(2)).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void notifiesObserverOnTargetChange() {
        when(mockSource.getCentre()).thenReturn(new Point2D.Double(65, 15));
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());
        arc.addPropertyChangeListener(mockListener);
        arc.setTarget(new DiscreteTransition("T0")); // also forces ID to be rebuilt
        verify(mockListener, times(2)).propertyChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void intermediatePointHasListener() {
        Point2D.Double center = mock(Point2D.Double.class);
        when(mockSource.getCentre()).thenReturn(center);
        when(mockTarget.getCentre()).thenReturn(new Point2D.Double(15, 15));
        arc = new InboundNormalArc(mockSource, mockTarget, new HashMap<String, String>());

        ArcPoint point = new ArcPoint(center, false);
        ArcPoint intermediate = new ArcPoint(new Point2D.Double(1, 5), false);
        arc.addIntermediatePoint(intermediate);
        assertEquals(0, point.changeSupport.getPropertyChangeListeners().length);
        assertEquals(1, intermediate.changeSupport.getPropertyChangeListeners().length);
        assertEquals("uk.ac.imperial.pipe.models.petrinet.AbstractArc$ArcPointChangeListener", intermediate.changeSupport
                .getPropertyChangeListeners()[0].getClass().getName());
    }

}
