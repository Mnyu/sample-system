package com.ntw.oms.mapred;

/**
 * Created by anurag on 19/06/17.
 */
public abstract class MRDriver {

    private String driverName;
    private String inputDir;
    private String outputDir;

    public abstract void setup();

    public abstract boolean doMapReduce() throws Exception;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
