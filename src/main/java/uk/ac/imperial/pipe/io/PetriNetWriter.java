package uk.ac.imperial.pipe.io;

import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import javax.xml.bind.JAXBException;

import java.io.IOException;
import java.io.Writer;

/**
 * API for writing Petri nets
 */
public interface PetriNetWriter {

    /**
     * Write the petri net to the given path
     * @param path
     * @param petriNet
     * @throws IOException 
     */
    void writeTo(String path, PetriNet petriNet) throws JAXBException, IOException;

    /**
     * Write the Petri net to the given stream
     * @param stream
     * @param petriNet
     */
    void writeTo(Writer stream, PetriNet petriNet) throws JAXBException;

}
