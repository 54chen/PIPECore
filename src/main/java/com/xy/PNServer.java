package com.xy;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.imperial.pipe.runner.FiringWriter;
import uk.ac.imperial.pipe.runner.PetriNetRunner;
import uk.ac.imperial.pipe.runner.ThreadedPetriNetRunner;
import uk.ac.imperial.pipe.runner.TimedPetriNetRunner;

public class PNServer {
  protected static Logger logger = LogManager.getLogger(PNServer.class);

  public static void main(String[] args) throws InterruptedException {
    logger.debug("Hello, world!");
    
    String TASK = "task1";
    TimedPetriNetRunner runner = new TimedPetriNetRunner("/Users/chenzhen/Downloads/davis.xml");
    runner.setFiringLimit(10);
    runner.setPlaceReporterParameters(true, true, 10);
    runner.setSeed(123l);
    runner.addPropertyChangeListener(getFiringWriter("/Users/chenzhen/Downloads/fire.csv"));
    // ThreadedPetriNetRunner threadRunner = new ThreadedPetriNetRunner(runner);
    // Thread thread = new Thread(threadRunner, TASK);
    // thread.start();
    runner.run();
    // runner.setSeed(1234l);
    // TASK = "task2";
    // ThreadedPetriNetRunner threadRunner2 = new ThreadedPetriNetRunner(runner);
    // Thread thread2 = new Thread(threadRunner2, TASK);
    // Thread.sleep(500);
    // thread2.start();

    Thread.sleep(2000);

    logger.debug("place report:" + runner.getPlaceReport());
  }

  private static FiringWriter getFiringWriter(String filename) {
    FiringWriter writer = null;
    try {
      writer = new FiringWriter(filename);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return writer;
  }
}
