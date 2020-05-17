package genetator.service.impl;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import genetator.service.CodeGenerator;
import genetator.util.GenUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

public class CodeGeneratorByMySQLImpl implements CodeGenerator {
    //组合配置
    private Configuration config;

    public CodeGeneratorByMySQLImpl(Configuration config) {
        this.config = config;
    }


    public void start() throws IOException {
        this.run();
    }
    //驱动名称
    private static final String driverClassName = "com.mysql.jdbc.Driver";
    private static String url;
    private static String username;
    private static String password;
    private static Connection conn = null;

    static {
        try {
            //加载mysql驱动
            Class.forName(driverClassName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        url = config.getString("url");
        username = config.getString("username");
        password = config.getString("password");
        String dbName = config.getString("dbName");
        //查询数据库,获取所有表的名字()
        List<String> tables = queryAllTable(dbName);
        //转为数组
        String[] tableNames = (String[]) tables.toArray(new String[0]);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //输出流
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String table : tableNames) {
            //查询表细节
            Map<String, String> tableDetail = queryTableDetailByTable(dbName, table);
            //查询列信息
            List<Map<String, String>> columns = queryColumns(dbName, table);
            //获取模板名称
            String template = config.getString("template");
            //通过 表明细,列信息 io流 模板,
            GenUtils.generatorCodeByDB(tableDetail, columns, zip, template);
        }
        //释放资源
        zip.close();
        byte[] bytes = outputStream.toByteArray();
        String filePath = config.getString("filePath");
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.flush();
        IOUtils.write(bytes, fos);
        IOUtils.closeQuietly(fos);
        IOUtils.closeQuietly(outputStream);
        System.out.println("===============success=================");
    }

    /**
     *查询表的列
     */
    private static List<Map<String, String>> queryColumns(String contest, String table) {
        String sql = "select * from information_schema.columns"
                + " where table_name = ? and table_schema = ? order by ordinal_position";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> columnDetails = new ArrayList<Map<String, String>>();
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, table);
            stmt.setString(2, contest);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, String> columnMap = new HashMap<String, String>();
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                String columnComment = rs.getString("column_comment");
                String columnKey = rs.getString("column_key");
                String extra = rs.getString("extra");
                columnMap.put("columnName", columnName);
                columnMap.put("dataType", dataType);
                columnMap.put("columnComment", columnComment);
                columnMap.put("columnKey", columnKey);
                columnMap.put("extra", extra);
                columnDetails.add(columnMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseResource(conn, stmt, rs);
        }
        return columnDetails;
    }

    /**
     *  查询表的细节
     */
    private static Map<String, String> queryTableDetailByTable(String schema, String table) {
        String sql = "select table_name tableName, engine, table_comment tableComment, create_time createTime from information_schema.tables"
                + "	where table_schema = ? and table_name = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, String> tableDetail = new HashMap<String, String>();
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, schema);
            stmt.setString(2, table);

            System.out.println(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String tableName = rs.getString("tableName");
                String tableComment = rs.getString("tableComment");
                tableDetail.put("tableName", tableName);
                tableDetail.put("tableComment", tableComment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(conn, stmt, rs);
        }
        return tableDetail;
    }

    /**
     * 查询所有表信息
     */
    private static List<String> queryAllTable(String schema) {
        DatabaseMetaData dbMetaData = null;
        ResultSet rs = null;
        List<String> tables = new ArrayList<String>();
        try {
            //当该类加载的时候,已经注册驱动了。
            conn = DriverManager.getConnection(url, username, password);
            //获取关于整个数据库的综合信息。
            dbMetaData = conn.getMetaData();
            //表示数据库结果集的数据表，通常通过执行查询数据库的语句生成。
            rs = dbMetaData.getTables(null, schema, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tables.add(tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResource(conn, null, rs);
        }
        return tables;
    }

    /**
     * 释放资源
     * @param conn
     * @param stmt
     * @param rs
     */
    private static void releaseResource(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
