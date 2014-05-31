package uk.ac.imperial.pipe.io.adapters.model;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.ArcPointAdapter;
import uk.ac.imperial.pipe.io.adapters.valueAdapter.StringAttributeValueAdaptor;
import uk.ac.imperial.pipe.models.petrinet.ArcPoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedArc {
    @XmlAttribute
    private String id;

    @XmlAttribute
    private String source;

    @XmlAttribute
    private String target;

    @XmlElement(name = "arcpath")
    @XmlJavaTypeAdapter(ArcPointAdapter.class)
    private List<ArcPoint> arcPoints = new ArrayList<>();

    @XmlElement
    @XmlJavaTypeAdapter(StringAttributeValueAdaptor.class)
    private String type = "normal";

    private Inscription inscription = new Inscription();

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final String getSource() {
        return source;
    }

    public final void setSource(String source) {
        this.source = source;
    }

    public final String getTarget() {
        return target;
    }

    public final void setTarget(String target) {
        this.target = target;
    }

    public final String getType() {
        return type;
    }

    public final void setType(String type) {
        this.type = type;
    }

    public final Inscription getInscription() {
        return inscription;
    }

    public final void setInscription(Inscription inscription) {
        this.inscription = inscription;
    }

    public final List<ArcPoint> getArcPoints() {
        return arcPoints;
    }

    public final void setArcPoints(List<ArcPoint> arcPoints) {
        this.arcPoints = arcPoints;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Inscription {
        @XmlElement(name = "value")
        private String tokenCounts = "";

        private OffsetGraphics graphics;

        public String getTokenCounts() {
            return tokenCounts;
        }

        public void setTokenCounts(String tokenCounts) {
            this.tokenCounts = tokenCounts;
        }

        public OffsetGraphics getGraphics() {
            return graphics;
        }

        public void setGraphics(OffsetGraphics graphics) {
            this.graphics = graphics;
        }
    }
}
