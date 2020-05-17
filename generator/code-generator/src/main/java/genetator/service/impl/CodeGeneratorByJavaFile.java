package genetator.service.impl;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import genetator.service.CodeGenerator;
import genetator.util.GenUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;


public class CodeGeneratorByJavaFile implements CodeGenerator {

    public void start() throws Exception {
        Configuration config = GenUtils.getConfig();
        String javaFilePath = config.getString("javaFilePath");
        List<String> beanNames = new ArrayList<String>();
        this.getAllFileName(javaFilePath, beanNames);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String beanName : beanNames) {
//            Map<String, String> tableDetail = queryTableDetailByTable(dbName, table);
            //查询列信息
//            List<Map<String, String>> columns = queryColumns(dbName, table);
            //生成代码
            String template = config.getString("template");
            GenUtils.generatorCodeByFileName(beanName,zip, template);
        }
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
     *      * 获取一个文件夹下的所有文件全路径
     *      * @param path
     *      * @param listFileName
     */
    public void getAllFileName(String path, List<String> beanNames) throws Exception {
        File file = new File(path);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {//如果文件夹下有子文件夹，获取子文件夹下的所有文件全路径。
                getAllFileName(f.getAbsolutePath() + "\\", beanNames);
            } else {
                this.creatClass(f, beanNames);
            }
        }
    }

    private void creatClass(File file, List<String> parent) throws Exception {
        String fileName = file.getName();
        if (!fileName.endsWith(".java")) {
            return;
        }
        int end = fileName.length() - 5;
        String substring = fileName.substring(0, end);
        parent.add(substring);
    }
}
