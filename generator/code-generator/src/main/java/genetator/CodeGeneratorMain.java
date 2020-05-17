package genetator;

import org.apache.commons.configuration.Configuration;
import genetator.service.CodeGenerator;
import genetator.service.impl.CodeGeneratorByJavaFile;
import genetator.service.impl.CodeGeneratorByMySQLImpl;
import genetator.util.GenUtils;


/**
 * 主函数
 */
public class CodeGeneratorMain {

    public static void main(String[] args) throws Exception {
        //获取generator.properties配置文件
        Configuration config = GenUtils.getConfig();
        //获取定义字段: 是够通过数据库驱动生成代码
        boolean isStartByDB = config.getBoolean("isStartByDB");
        CodeGenerator codeGenerator;
        if (isStartByDB) {
            codeGenerator = new CodeGeneratorByMySQLImpl(config);
        } else {
            codeGenerator = new CodeGeneratorByJavaFile();
        }
        codeGenerator.start();
    }
}