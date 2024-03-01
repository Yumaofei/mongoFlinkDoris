package com.yumaofei.test1;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.hive.HiveCatalog;

/**
 * @program: mongoFlinkDoris
 * @description:
 * @author: Mr.YMF
 * @create: 2024-01-13 10:07
 **/



public class FlinkHiveMetastoreExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        EnvironmentSettings environmentSettings = EnvironmentSettings.inStreamingMode();
        TableEnvironment tableEnvironment = TableEnvironment.create(environmentSettings);

        String url = "D:\\Program Files\\JetBrains\\IntelliJ IDEA 2023.1\\MyProject\\mongoFlinkDoris\\src\\main\\resources";
        tableEnvironment.registerCatalog("hive", new HiveCatalog("default", "tmp", url));

        Table showDatabases = tableEnvironment.sqlQuery("show databases");
        showDatabases.execute().print();

        env.execute("Flink - Hive metadata query");

    }
}
