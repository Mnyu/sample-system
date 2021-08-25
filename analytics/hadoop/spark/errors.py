from pyspark import SparkConf, SparkContext

from operator import add
import sys

APP_NAME = "Server Load"


def getValue(key, line):
    words = map(lambda word: word.strip(), line.split('|'))
    filteredWords = list(filter(lambda word: word.startswith(key), words))
    value = None
    if len(filteredWords) > 0:
        keyValue = list(filteredWords)[0]
        value = keyValue[len(key):len(keyValue)]
    return value


def main(sc, fileName):
   textRDD = sc.textFile(fileName)
   filteredRDD = textRDD.filter(lambda line: getValue('level=', line) == 'ERROR')
   result = filteredRDD.collect()
   print('********************')
   for x in result:
       print(x)
   print('********************')


if __name__ == "__main__":

   # Configure Spark
   conf = SparkConf().setAppName(APP_NAME)
   conf = conf.setMaster("local[*]")
   sc   = SparkContext(conf=conf)
   filename = sys.argv[1]
   # Execute Main functionality
   main(sc, filename)
