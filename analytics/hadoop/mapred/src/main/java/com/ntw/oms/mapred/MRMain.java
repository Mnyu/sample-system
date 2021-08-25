package com.ntw.oms.mapred;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by anurag on 19/06/17.
 */
public class MRMain {

    private static final Logger logger = LoggerFactory.getLogger(MRMain.class);

    public static void main(String[] args) throws Exception {

        String driverName = null;
        String inputDir = null;
        String outputDir = null;
        if (args.length > 0) {
            driverName = args[0];
            inputDir = args[1];
            outputDir = args[2];
        } else {
            logger.error("No arguments provided");
            System.exit(1);
        }

        MRDriver driver = MRDriverFactory.createNewDriver(driverName);
        driver.setDriverName(driverName);
        driver.setInputDir(inputDir);
        driver.setOutputDir(outputDir);

        boolean success = driver.doMapReduce();
        logger.info("*******************************************");
        logger.info("{} driver returned {}", driverName, success);
        logger.info("*******************************************");

        System.exit(success ? 0 : 1);
    }

}
