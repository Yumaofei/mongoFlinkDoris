package com.yumaofei.ods;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.yumaofei.util.HttpUtil;
import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.sink.DorisSink;
import org.apache.doris.flink.sink.writer.RowDataSerializer;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.types.DataType;

import java.util.Properties;

/**
 * @program: mongoFlinkDoris
 * @description: 股票列表表导入
 * @author: Mr.YMF
 * @create: 2024-02-28 14:55
 **/

public class OdsShareCertificateList {
    public static void main(String[] args) throws Exception {
        // 示例：创建一个 Flink Job，使用 CustomSource 作为数据源
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setRuntimeMode(RuntimeExecutionMode.BATCH);
//        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        String URL = "http://api.mairui.club/hslt/list/0e5e5198546de725f3";
        String shareCertificateListJson = HttpUtil.doGet(URL);
        JSONArray shareCertificateListJsonArray = JSONArray.parseArray(shareCertificateListJson);

        DataStreamSource<Object> shareCertificateListObj = env.fromCollection(shareCertificateListJsonArray);

        SingleOutputStreamOperator<String> shareCertificateList = shareCertificateListObj.map(x -> x.toString());

//        shareCertificateList.print();

        SingleOutputStreamOperator<RowData> rowDataSingleOutputStreamOperator = shareCertificateList.map(json -> {
                    JSONObject obj = JSONObject.parseObject(json);
                    GenericRowData rowData = new GenericRowData(4);
                    rowData.setField(0, obj.getIntValue("dm"));
                    rowData.setField(1, StringData.fromString(obj.getString("mc")));
                    rowData.setField(2, StringData.fromString(obj.getString("jys")));
                    String jys = obj.getString("jys");
                    String jysName = "未识别的交易所";
                    if (jys.equals("sh"))
                        jysName = "中国上海证券交易所";
                    else
                        jysName = "中国深圳证券交易所";
                    rowData.setField(3, StringData.fromString(jysName));
                    return rowData;
                }
        );

//        rowDataSingleOutputStreamOperator.print();

        //doris sink option
        DorisSink.Builder<RowData> builder = DorisSink.builder();
        DorisOptions.Builder dorisBuilder = DorisOptions.builder();
        dorisBuilder.setFenodes("192.168.0.107:8030")
                .setTableIdentifier("ods.ods_share_certificate_list")
                .setUsername("test")
                .setPassword("test");

        // json format to streamload
        Properties properties = new Properties();
        properties.setProperty("format", "json");
        properties.setProperty("read_json_by_line", "true");
        DorisExecutionOptions.Builder executionBuilder = DorisExecutionOptions.builder();
        executionBuilder.setLabelPrefix("label-doris") //streamload label prefix
                .setDeletable(false)
                .setStreamLoadProp(properties); //streamload params

        //flink rowdata‘s schema
        String[] fields = {"id", "name", "exchange_code", "exchange_name"};
        DataType[] types = {DataTypes.INT(), DataTypes.VARCHAR(20), DataTypes.VARCHAR(5), DataTypes.VARCHAR(100)};

        builder.setDorisReadOptions(DorisReadOptions.builder().build())
                .setDorisExecutionOptions(executionBuilder.build())
                .setSerializer(RowDataSerializer.builder()    //serialize according to rowdata
                        .setFieldNames(fields)
                        .setType("json")                      //json format
                        .setFieldType(types).build())
                .setDorisOptions(dorisBuilder.build());

        rowDataSingleOutputStreamOperator.sinkTo(builder.build());

        env.execute();
    }
}
