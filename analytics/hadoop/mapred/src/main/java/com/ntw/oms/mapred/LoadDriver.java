package com.ntw.oms.mapred;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class LoadDriver extends MRDriver {

    private static final Logger logger = LoggerFactory.getLogger(LoadDriver.class);

    @Override
    public void setup() {

    }

    @Override
    public boolean doMapReduce()
            throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(conf);
        Path outputPath = new Path(getOutputDir());
        if(fs.exists(outputPath)){ fs.delete(outputPath, true);}

        Job job = Job.getInstance(conf, "Log Analyser");

        job.setJarByClass(LoadDriver.class);

        job.setMapperClass(LoadMapper.class);
        job.setCombinerClass(LoadReducer.class);
        job.setReducerClass(LoadReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setNumReduceTasks(1);

        FileInputFormat.setInputDirRecursive(job, true);
        FileInputFormat.addInputPath(job, new Path(getInputDir()));
        FileOutputFormat.setOutputPath(job, new Path(getOutputDir()));

        return job.waitForCompletion(true);
    }

}

class LoadMapper extends Mapper<Object, Text, Text, IntWritable> {

    public void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {

        Map<String, String> dataMap = (new LogParser()).parseLog(value.toString());
        String hostName = dataMap.get(LogParser.HOSTNAME);

        if (hostName != null) {
            Text outKey = new Text();
            IntWritable one = new IntWritable(1);
            outKey.set(hostName);
            context.write(outKey, one);
        }

    }

}

class LoadReducer extends Reducer<Text,IntWritable,Text,IntWritable> {

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
    ) throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        IntWritable result = new IntWritable();
        result.set(sum);
        context.write(key, result);
    }
}

