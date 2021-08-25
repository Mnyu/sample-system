package com.ntw.oms.mapred;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by anurag on 19/06/17.
 */
public class TestDriver extends MRDriver {

    private static final Logger logger = LoggerFactory.getLogger(TestDriver.class);

    @Override
    public void setup() {

    }

    @Override
    public boolean doMapReduce()
            throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(conf);
        Path outputPath = new Path(getOutputDir());
        if (fs.exists(outputPath)) {
            fs.delete(outputPath, true);
        }

        Job job = Job.getInstance(conf, "Test Analyser"); // Required
        job.setJarByClass(TestDriver.class); // Required


        boolean setDefaultsAnyway = true;
        boolean setDefaultOverrides = true;

        if (setDefaultsAnyway) {
            job.setInputFormatClass(TextInputFormat.class); //Default

            job.setMapperClass(Mapper.class); // Default
            job.setMapOutputKeyClass(LongWritable.class); // Default
            job.setMapOutputValueClass(Text.class); // Default

            job.setPartitionerClass(HashPartitioner.class); // Default

            job.setNumReduceTasks(1); // Default
            job.setReducerClass(Reducer.class); // Default
            job.setOutputKeyClass(LongWritable.class); // Default
            job.setOutputValueClass(Text.class); // Default
        }

        if (setDefaultOverrides) {
            job.setMapperClass(TestMapper.class); // Mimics default Mapper class
            job.setCombinerClass(TestReducer.class); // Mimics default Reducer class
            job.setReducerClass(TestReducer.class); // Mimics default Reducer class
        }

        FileInputFormat.setInputDirRecursive(job, true); // Default is false
        FileInputFormat.addInputPath(job, new Path(getInputDir())); // Required
        FileOutputFormat.setOutputPath(job, new Path(getOutputDir())); // Required

        return job.waitForCompletion(true);
    }

}

class TestMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
            context.write(key, value);
    }
}

class TestReducer extends Reducer<LongWritable,Text,LongWritable,Text> {
    public void reduce(LongWritable key, Iterable<Text> values,
                       Context context) throws IOException, InterruptedException {
        for (Text value : values) {
            context.write(key, value);
        }
    }
}


