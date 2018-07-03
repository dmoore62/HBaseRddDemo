package com.hbaserdddemo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;

public class HBaseRddDriver {
    public static void main(String[] args){
        System.out.println("Running");

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("HBase RDD Demo");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.addResource("hbase-site.xml");
        hbaseConf.set(TableInputFormat.INPUT_TABLE, "iemployee");

        try {
            HBaseAdmin.checkHBaseAvailable(hbaseConf);
            System.out.println("Connented to HBase!");

            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);
            //TODO: need to set start and end scan rows

            //TODO: need to set column qualifier to row mappings

            //submit to hbase
            hbaseConf.set(TableInputFormat.SCAN, convertScanToString(scan));

            JavaPairRDD<ImmutableBytesWritable, Result> rdd = sc
                    .newAPIHadoopRDD(hbaseConf, TableInputFormat.class,
                            ImmutableBytesWritable.class, Result.class);

            rdd.foreach((line) -> System.out.println("Print Row -- " + line));
        } catch (Exception ex){
            System.out.println("Cannot connect to HBase");
        }

    }

    static String convertScanToString(Scan scan) throws IOException {
        ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
        return Base64.encodeBytes(proto.toByteArray());
    }
}
