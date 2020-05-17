package genetator.util;

import genetator.config.Constant;
import genetator.entity.ColumnDO;
import genetator.entity.TableDO;
import genetator.exception.BDException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器   工具类
 */
@SuppressWarnings("AlibabaRemoveCommentedCode")
public class GenUtils {

    private static List<String> templates = new ArrayList<String>();
    /**
     * 获取模板
     * @param dir
     * @return
     */
    public static List<String> getTemplates(String dir) {
        int size = templates.size();
        if (size > 0) {
            return templates;
        }
        URL resource = GenUtils.class.getClassLoader().getResource(dir);
        String path = resource.getPath();
        /**
         * 将目标目录封装成 File 对象。
         */
        File file = new File(path);
        /**
         * 获取目录下的所有文件和文件夹,resources
         */
        String[] names = file.list();
        for (String name : names) {
            templates.add(dir + "/" + name);
            System.out.println(name);
        }
        return templates;
    }

    /**
     * 生成代码
     * @param dir 模板名称
     */
    public static void generatorCodeByDB(Map<String, String> table,
                                     List<Map<String, String>> columns, ZipOutputStream zip, String dir) throws IOException {
        //配置信息
        Configuration config = getConfig();
        //表信息
        TableDO tableDO = new TableDO();
        tableDO.setTableName(table.get("tableName"));
        tableDO.setComments(table.get("tableComment"));

        //表名转换成Java类名
        String className = tableToJava(tableDO.getTableName(), config.getString("tablePrefix"), config.getString("autoRemovePre"));
        tableDO.setClassName(className);
        tableDO.setClassname(StringUtils.uncapitalize(className));

        //列信息
        List<ColumnDO> columsList = new ArrayList<ColumnDO>();
        for (Map<String, String> column : columns) {
            ColumnDO columnDO = new ColumnDO();
            columnDO.setColumnName(column.get("columnName"));
            columnDO.setDataType(column.get("dataType"));
            columnDO.setComments(column.get("columnComment"));
            columnDO.setExtra(column.get("extra"));
            //列名转换成Java属性名
            String attrName = columnToJava(columnDO.getColumnName());
            columnDO.setAttrName(attrName);
            columnDO.setAttrname(StringUtils.uncapitalize(attrName));
            //列的数据类型，转换成Java类型
            String attrType = config.getString(columnDO.getDataType(), "unknowType");
            columnDO.setAttrType(attrType);
            //是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableDO.getPk() == null) {
                tableDO.setPk(columnDO);
                columsList.add(columnDO);
            } else {
                columsList.add(columnDO);
            }
        }
        tableDO.setColumns(columsList);
        //没主键，则第一个字段为主键
        if (tableDO.getPk() == null) {
            tableDO.setPk(tableDO.getColumns().get(0));
        }
        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        //封装模板数据
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put("tableName", tableDO.getTableName());
        map.put("comments", tableDO.getComments());
        map.put("pk", tableDO.getPk());
        map.put("className", tableDO.getClassName());
        map.put("classname", tableDO.getClassname());
        map.put("pathName", config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1));
        map.put("columns", tableDO.getColumns());
        map.put("package", config.getString("package"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        //通过map信息,创建 ** 对象,
        VelocityContext context = new VelocityContext(map);
        //获取模板列表
        List<String> templates = getTemplates(dir);
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            //写入到模板
            tpl.merge(context, sw);
            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableDO.getClassname(), tableDO.getClassName(), config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1))));
                //io工具
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new BDException("渲染模板失败，表名：" + tableDO.getTableName(), e);
            }
        }
    }

    //通过文件名称生成代码
    public static void generatorCodeByFileName(String beanName, ZipOutputStream zip, String dir) {
        //配置信息
        Configuration config = getConfig();
        //表信息
        TableDO tableDO = new TableDO();
        //表名转换成Java类名
        tableDO.setClassName(beanName);
        tableDO.setClassname(StringUtils.uncapitalize(beanName));
        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        //封装模板数据
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put("tableName", tableDO.getTableName());
        map.put("comments", tableDO.getComments());
        map.put("pk", tableDO.getPk());
        map.put("className", tableDO.getClassName());
        map.put("classname", tableDO.getClassname());
        map.put("pathName", config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1));
        map.put("columns", tableDO.getColumns());
        map.put("package", config.getString("package"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        VelocityContext context = new VelocityContext(map);
        //获取模板列表
        List<String> templates = getTemplates(dir);
        for (String template : templates) {
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableDO.getClassname(), tableDO.getClassName(), config.getString("package").substring(config.getString("package").lastIndexOf(".") + 1))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new BDException("渲染模板失败，表名：" + tableDO.getTableName(), e);
            }
        }
    }

    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix, String autoRemovePre) {
        if (Constant.AUTO_REOMVE_PRE.equals(autoRemovePre)) {
            tableName = tableName.substring(tableName.indexOf("_") + 1);
        }
        if (StringUtils.isNotBlank(tablePrefix)) {
            tableName = tableName.replace(tablePrefix, "");
        }

        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new BDException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String classname, String className, String packageName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        //String modulesname=config.getString("packageName");
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }
        int i = template.indexOf("/")+1;
        String substring = template.substring(i);
        String[] split = substring.split("\\.");
        String lowerCase = split[0].toLowerCase();
        StringBuilder builder = new StringBuilder();
        if ("java".equals(split[1])) {
            if ("entity".equals(lowerCase)) {
                builder.append(packagePath).append(lowerCase).append(File.separator);
                return builder.append(className).append(".java").toString();
            } else {
                if (lowerCase.contains("impl")) {
                    builder.append(packagePath).append("service").append(File.separator);
                    return builder.append("impl").append(File.separator).append(className).append("ServiceImpl.java").toString();
                } else {
                    builder.append(packagePath).append(lowerCase).append(File.separator);
                    return builder.append(className).append(split[0]).append(".java").toString();
                }
            }
        }
        builder.append("main").append(File.separator);
        if ("xml".equals(split[1])) {
            return builder.append("resources").append(File.separator).append("mapper").append(File.separator).append(className).append("Mapper.xml").toString();
        }
        if ("jsp".equals(split[1])) {
            builder.append("webapp").append(File.separator).append("admin").append(File.separator
            ).append(packageName).append(File.separator).append(classname).append(File.separator).append(classname);
            if ("list".equals(lowerCase)) {
                return builder.append(".jsp").toString();
            } else {
                return builder.append(className).append(split[0]).append(".jsp").toString();
            }
        }
        return null;
    }

}
