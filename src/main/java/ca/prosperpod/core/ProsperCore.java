package ca.prosperpod.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProsperCore {

    private Logger logger = LogManager.getLogger(this.getClass());
    private static final ProsperCore sharedInstance = new ProsperCore();

    private Pod[] pods = new Pod[10];

    private ProsperCore() {
        for (int i = 0; i < 10; i++) {
            pods[i] = new Pod(i + 1);
        }
    }

    public static ProsperCore getInstance() {
        return sharedInstance;
    }

    public Pod[] getPods() {
        return this.pods;
    }

}
