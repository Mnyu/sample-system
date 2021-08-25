package com.ntw.oms.mapred;

/**
 * Created by anurag on 19/06/17.
 */
public class MRDriverFactory {
    public static MRDriver createNewDriver(String driver) {
        return driver.equals("Load") ?
                new LoadDriver() : driver.equals("Error") ?
                new ErrorDriver() : driver.equals("Import") ?
                new ImportDriver() : driver.equals("Test") ?
                new TestDriver() : null;
    }
}
