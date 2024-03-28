package com.yumaofei.ods;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.yumaofei.util.HttpUtil;
import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.sink.DorisSink;
import org.apache.doris.flink.sink.writer.RowDataSerializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.types.DataType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class OdsIndexIndustryConcept {
    public static void main(String[] args) throws Exception {
        // 示例：创建一个 Flink Job，使用 CustomSource 作为数据源
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setRuntimeMode(RuntimeExecutionMode.BATCH);
//        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        String URL = "http://api.mairui.club/hszg/list/164fc966f182e8c4c";
        String apiJsonData = HttpUtil.doGet(URL);
        JSONArray apiJsonArray = JSONArray.parseArray(apiJsonData);

        DataStreamSource<Object> apiJsonObj = env.fromCollection(apiJsonArray);

        SingleOutputStreamOperator<String> apiJson = apiJsonObj.map(x -> x.toString());

        apiJson.print();

        SingleOutputStreamOperator<RowData> rowDataSingleOutputStreamOperator = apiJson.map(json -> {
                    JSONObject obj = JSONObject.parseObject(json);
                    GenericRowData rowData = new GenericRowData(8);
                    rowData.setField(0, StringData.fromString(obj.getString("code")));
                    rowData.setField(1, StringData.fromString(obj.getString("name")));
                    rowData.setField(2, StringData.fromString(obj.getString("type1")));
                    rowData.setField(3, StringData.fromString(obj.getString("type2")));
                    rowData.setField(4, StringData.fromString(obj.getString("level")));
                    rowData.setField(5, StringData.fromString(obj.getString("pcode")));
                    rowData.setField(6, StringData.fromString(obj.getString("pname")));
                    rowData.setField(7, StringData.fromString(obj.getString("isleaf")));
                    return rowData;
                }
        );

        rowDataSingleOutputStreamOperator.print();

        //doris sink option
        DorisSink.Builder<RowData> builder = DorisSink.builder();
        DorisOptions.Builder dorisBuilder = DorisOptions.builder();
        dorisBuilder.setFenodes("192.168.0.107:8030")
                .setTableIdentifier("ods.ods_index_industry_concept")
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
        String[] fields = {"code", "name", "type1", "type2", "level", "pcode", "pname", "isleaf"};
        DataType[] types = {DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255)};

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
