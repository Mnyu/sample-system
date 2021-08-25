package com.ntw.oms.mapred;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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

public class ErrorDriver extends MRDriver {

    private static final Logger logger = LoggerFactory.getLogger(ErrorDriver.class);

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

        Job job = Job.getInstance(conf, "Error Analyser");

        job.setJarByClass(ErrorDriver.class);

        job.setMapperClass(ErrorMapper.class);
        job.setCombinerClass(ErrorReducer.class);
        job.setReducerClass(ErrorReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setNumReduceTasks(1);

        FileInputFormat.setInputDirRecursive(job, true);
        FileInputFormat.addInputPath(job, new Path(getInputDir()));
        FileOutputFormat.setOutputPath(job, new Path(getOutputDir()));

        return job.waitForCompletion(true);
    }

}

class ErrorMapper extends Mapper<Object, Text, Text, Text> {

    public void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {

        Map<String, String> dataMap = (new LogParser()).parseLog(value.toString());

        String hostName = dataMap.get(LogParser.HOSTNAME);
        String logLevel = dataMap.get(LogParser.LOGLEVEL);

        if (logLevel != null && hostName != null && logLevel.equalsIgnoreCase("ERROR")) {
            Text outKey = new Text();
            outKey.set(hostName);
            context.write(outKey, value);
        }
    }
}

class ErrorReducer extends Reducer<Text,Text,Text,Text> {

    public void reduce(Text key, Iterable<Text> values,
                       Context context) throws IOException, InterruptedException {
        StringBuilder result = new StringBuilder("\n");
        for (Text val : values) {
            result.append(val).append("\n");
        }
        context.write(key, new Text(result.toString()));
    }
}


