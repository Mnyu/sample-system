package com.ntw.oms.mapred;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;


public class ImportDriver extends MRDriver {

    private static final Logger logger = LoggerFactory.getLogger(ImportDriver.class);

    public static final String LOG_TABLE = "log_data";
    public static final String LOG_CF = "data";

    @Override
    public void setup() {

    }

    @Override
    public boolean doMapReduce() throws Exception {

        Configuration conf =  HBaseConfiguration.create();
        // Following not needed for localhost
        //conf.set("hbase.zookeeper.quorum", "192.168.56.61");
        //conf.set("hbase.zookeeper.property.clientPort", "2181");

        Job job = Job.getInstance(conf, "Error Analyser");

        job.setJarByClass(ImportDriver.class);
        job.setMapperClass(ImportMapper.class);

        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(Put.class);

        job.setNumReduceTasks(0);

        FileInputFormat.setInputDirRecursive(job, true);
        FileInputFormat.addInputPath(job, new Path(getInputDir()));

        TableMapReduceUtil.initTableReducerJob(LOG_TABLE, null, job);
        // The above function does the following: ******************
        // job.setOutputFormatClass(TableOutputFormat.class);
        // job.setReducerClass(reducer); // null
        // conf.set("hbase.mapred.outputtable", table); // LOG_TABLE
        // job.setOutputKeyClass(ImmutableBytesWritable.class);
        // job.setOutputValueClass(Writable.class);
        // job.setNumReduceTasks(regions); *************************

        job.setNumReduceTasks(0);

        return job.waitForCompletion(true);
    }

    public void checkHBaseAvailable() {
        try {
            // For connecting to remote hadoop - zookeeper acts as loadbalancer for hbase
            // conf.set("hbase.zookeeper.quorum", "server’s IP address");
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.zookeeper.quorum", "192.168.56.61");
            config.set("hbase.zookeeper.property.clientPort", "2181");
            config.set("hbase.master", "192.168.15.20:16010");
            //HBaseConfiguration config = HBaseConfiguration.create();
            //config.set("hbase.zookeeper.quorum", "localhost");  // Here we are running zookeeper locally
            HBaseAdmin.checkHBaseAvailable(config);
            System.out.println("HBase is running!");
        } catch (MasterNotRunningException e) {
            System.out.println("HBase is not running!");
            System.exit(1);
        } catch (Exception ce) {
            ce.printStackTrace();
        }
    }

}

class ImportMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        LogParser parser = new LogParser();
        Map<String, String> dataMap = parser.parseLog(value.toString());
        String dataKey = parser.getKey(key.get(), dataMap);
        Put put = new Put(Bytes.toBytes(dataKey));
        for (String colName : dataMap.keySet()) {
            put.addColumn(Bytes.toBytes(ImportDriver.LOG_CF),
                    Bytes.toBytes(colName),
                    Bytes.toBytes(dataMap.get(colName)));
        }
        context.write(new ImmutableBytesWritable(Bytes.toBytes(key.get())), put);
    }

}

