package com.yumaofei.ods;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import java.util.Properties;

public class OdsShareCertificateCalendar {
    public static void main(String[] args) throws Exception {
        // 示例：创建一个 Flink Job，使用 CustomSource 作为数据源
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setRuntimeMode(RuntimeExecutionMode.BATCH);
//        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        String URL = "http://api.mairui.club/hslt/new/164fc966f182e8c4c";
        String shareCertificateCalendarJson = HttpUtil.doGet(URL);
        JSONArray shareCertificateCalendarJsonArray = JSONArray.parseArray(shareCertificateCalendarJson);

        DataStreamSource<Object> shareCertificateCalendarObj = env.fromCollection(shareCertificateCalendarJsonArray);

        SingleOutputStreamOperator<String> shareCertificateCalendar = shareCertificateCalendarObj.map(x -> x.toString());

        shareCertificateCalendar.print();

        SingleOutputStreamOperator<RowData> rowDataSingleOutputStreamOperator = shareCertificateCalendar.map(json -> {
                    JSONObject obj = JSONObject.parseObject(json);
                    GenericRowData rowData = new GenericRowData(22);
                    // 获取当前日期，默认格式（yyyy-MM-dd）
                    LocalDate currentDate = LocalDate.now(); // 获取当前日期，不包含具体时间
                    // 如果需要自定义格式输出
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String ext_date = currentDate.format(formatter);
                    rowData.setField(0, StringData.fromString(ext_date));
                    rowData.setField(1, StringData.fromString(obj.getString("zqdm")));
                    rowData.setField(2, StringData.fromString(obj.getString("zqjc")));
                    rowData.setField(3, StringData.fromString(obj.getString("sgdm")));
                    rowData.setField(4, StringData.fromString(obj.getString("sgrq")));
                    rowData.setField(5, StringData.fromString(obj.getString("zqgbrq")));
                    rowData.setField(6, StringData.fromString(obj.getString("zqjkrq")));
                    rowData.setField(7, StringData.fromString(obj.getString("ssrq")));
                    rowData.setField(8, StringData.fromString(obj.getString("zyyw")));
                    rowData.setField(9, StringData.fromString(obj.getString("fxsl")));
                    rowData.setField(10, StringData.fromString(obj.getString("swfxsl")));
                    rowData.setField(11, StringData.fromString(obj.getString("sgsx")));
                    rowData.setField(12, StringData.fromString(obj.getString("dgsz")));
                    rowData.setField(13, StringData.fromString(obj.getString("fxjg")));
                    rowData.setField(14, StringData.fromString(obj.getString("zxj")));
                    rowData.setField(15, StringData.fromString(obj.getString("srspj")));
                    rowData.setField(16, StringData.fromString(obj.getString("syl")));
                    rowData.setField(17, StringData.fromString(obj.getString("hysyl")));
                    rowData.setField(18, StringData.fromString(obj.getString("wszql")));
                    rowData.setField(19, StringData.fromString(obj.getString("yzbsl")));
                    rowData.setField(20, StringData.fromString(obj.getString("zf")));
                    rowData.setField(21, StringData.fromString(obj.getString("yqhl")));
                    return rowData;
                }
        );

        rowDataSingleOutputStreamOperator.print();

        //doris sink option
        DorisSink.Builder<RowData> builder = DorisSink.builder();
        DorisOptions.Builder dorisBuilder = DorisOptions.builder();
        dorisBuilder.setFenodes("192.168.0.107:8030")
                .setTableIdentifier("ods.ods_share_certificate_calendar")
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
        String[] fields = {"ext_date", "zqdm", "zqjc", "sgdm", "sgrq", "zqgbrq", "zqjkrq", "ssrq", "zyyw", "fxsl", "swfxsl", "sgsx", "dgsz", "fxjg", "zxj", "srspj", "syl", "hysyl", "wszql", "yzbsl", "zf", "yqhl"};
        DataType[] types = {DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(2000), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255), DataTypes.VARCHAR(255)};

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
