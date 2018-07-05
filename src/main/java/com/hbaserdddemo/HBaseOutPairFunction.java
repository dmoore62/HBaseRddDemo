package com.hbaserdddemo;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.security.MessageDigest;
import java.util.concurrent.ThreadLocalRandom;

public class HBaseOutPairFunction implements PairFunction<EmployeeBean, ImmutableBytesWritable, Put> {

    @Override
    public Tuple2<ImmutableBytesWritable, Put> call(EmployeeBean e) {
        Put put = new Put(Bytes.toBytes(e.getId() + ThreadLocalRandom.current().nextInt(1, 11)));
        try {
            put.addColumn(Bytes.toBytes("randomhash"), Bytes.toBytes("fname-hash"), Bytes.toBytes(MessageDigest.getInstance("MD5").digest(e.getFirstName().getBytes()).toString()));
            put.addColumn(Bytes.toBytes("randomhash"), Bytes.toBytes("lname-hash"), Bytes.toBytes(MessageDigest.getInstance("MD5").digest(e.getLastName().getBytes()).toString()));
            put.addColumn(Bytes.toBytes("randomhash"), Bytes.toBytes("city-hash"), Bytes.toBytes(MessageDigest.getInstance("MD5").digest(e.getCity().getBytes()).toString()));
        } catch (Exception ex) {
            System.out.println("Error hashing " + ex.getMessage());
        }
        return new Tuple2<ImmutableBytesWritable, Put>(new ImmutableBytesWritable(), put);
    }
}
