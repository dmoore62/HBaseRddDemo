package com.hbaserdddemo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapred.TableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;

public class HBaseRddDriver implements Serializable {
    public static void main(String[] args){
        System.out.println("Running");

        /**
         * Configure Spark and HBase
         */
        SparkConf sparkConf = new SparkConf()
                .setMaster("local[*]")
                .setAppName("HBase RDD Demo");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.addResource("hbase-site.xml");
        hbaseConf.set(TableInputFormat.INPUT_TABLE, "iemployee");
        hbaseConf.set(TableOutputFormat.OUTPUT_TABLE, "iemployeeout");
        //This is required to write to output table
        hbaseConf.set("mapreduce.outputformat.class", "org.apache.hadoop.hbase.mapreduce.TableOutputFormat");

        try {
            HBaseAdmin.checkHBaseAvailable(hbaseConf);
            System.out.println("Connected to HBase!");

            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);
            //TODO: need to set start and end scan rows

            //TODO: need to set column qualifier to row mappings

            //submit to hbase
            hbaseConf.set(TableInputFormat.SCAN, convertScanToString(scan));

            //Scan KVs into pair RDD
            JavaPairRDD<ImmutableBytesWritable, Result> rdd = sc
                    .newAPIHadoopRDD(hbaseConf, TableInputFormat.class,
                            ImmutableBytesWritable.class, Result.class);

            //Map Pair RDD into serialized objects RDD
            JavaRDD<EmployeeBean> employeeRDD = rdd.map(
                    new Function<Tuple2<ImmutableBytesWritable, Result>, EmployeeBean>() {
                        @Override
                        public EmployeeBean call(Tuple2<ImmutableBytesWritable, Result> tuple) throws Exception {
                            EmployeeBean e = new EmployeeBean();
                            Result r = tuple._2;
                            e.setId(Bytes.toInt(tuple._2.getRow()));
                            e.setFirstName(Bytes.toString(r.getValue(Bytes.toBytes("personal"), Bytes.toBytes("fname"))));
                            e.setLastName(Bytes.toString(r.getValue(Bytes.toBytes("personal"), Bytes.toBytes("lname"))));
                            e.setCity(Bytes.toString(r.getValue(Bytes.toBytes("personal"), Bytes.toBytes("city"))));
                            return e;
                        }
                    }
            );

            //Sanity Check
            employeeRDD.foreach((line) -> System.out.println("Print Row -- " + line));

            //output needs to be converted to pairRDD
            JavaPairRDD<ImmutableBytesWritable, Put> hbasePuts = employeeRDD.mapToPair(
                    new HBaseOutPairFunction()
            );

            //Sanity Check
            hbasePuts.foreach((line) -> System.out.println("Print out -- " + line));

            System.out.println("Putting HBase....");
            /**
             * This API allows for a bulk put into HBase.
             * Writing sequential puts is not recommended
             */
            hbasePuts.saveAsNewAPIHadoopDataset(hbaseConf);
        } catch (Exception ex){
            System.out.println("Cannot connect to HBase");
            System.out.println(ex.toString());
        }
    }

    static String convertScanToString(Scan scan) throws IOException {
        ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
        return Base64.encodeBytes(proto.toByteArray());
    }
}
