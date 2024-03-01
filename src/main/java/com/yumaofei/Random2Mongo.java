package com.yumaofei;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.yumaofei.source.ApiSource;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import static org.apache.flink.streaming.api.windowing.time.Time.seconds;

/**
 * @program: mongoFlinkDoris
 * @description: 随机生成学生数据并写入mongo中
 * @author: Mr.YMF
 * @create: 2023-11-25 17:47
 **/

public class Random2Mongo {
    public static void main(String[] args) throws Exception {
        // 示例：创建一个 Flink Job，使用 CustomSource 作为数据源
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        String URL = "";

        DataStreamSource<String> stringDataStreamSource = env.addSource(new ApiSource(URL));

        SingleOutputStreamOperator<String> result = stringDataStreamSource.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                JSONObject jsonObject = JSONObject.parseObject(s);
                JSONArray data = jsonObject.getJSONArray("result");
                for (Object datum : data) {
                    collector.collect(datum.toString());
                }
            }
        });

        SingleOutputStreamOperator<String> map = result.map(x -> {
            JSONObject jsonObject = JSONObject.parseObject(x);
            String sku = jsonObject.getString("sku");
            return sku;
        });

        SingleOutputStreamOperator<Tuple2<String, Integer>> sum = map.map(new MapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> map(String s) throws Exception {
                        return new Tuple2(s, 1);
                    }
                }).keyBy(0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .sum(1);

        sum.print();

        env.execute("Custom Source Example");
    }
}
