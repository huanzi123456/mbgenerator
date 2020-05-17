package com.demo.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.demo.constant.ExcelConstant;
import com.demo.utils.PageHelper;
import com.demo.utils.ResultVO;
import com.demo.utils.SysSystemVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class ExcelController {


    /**
     * 数据量适中（100W以内）： 一个SHEET分批查询导出
     *
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/export")
    public ResultVO<Void> exportSysSystemExcel1(HttpServletResponse response) throws Exception {
        SysSystemVO sysSystemVO;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);

            // 设置EXCEL名称
            String fileName = new String(("SystemExcel").getBytes(), "UTF-8");

            // 设置SHEET名称
            Sheet sheet = new Sheet(1, 0);
            sheet.setSheetName("系统列表sheet1");

            // 设置标题
            Table table = new Table(1);
            List<List<String>> titles = new ArrayList<List<String>>();
            titles.add(Arrays.asList("系统名称"));
            titles.add(Arrays.asList("系统标识"));
            titles.add(Arrays.asList("描述"));
            titles.add(Arrays.asList("状态"));
            titles.add(Arrays.asList("创建人"));
            titles.add(Arrays.asList("创建时间"));
            table.setHead(titles);

            // 查数据写EXCEL
            List<List<String>> dataList = new ArrayList<>();
            List<SysSystemVO> sysSystemVOList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(sysSystemVOList)) {
                sysSystemVOList.forEach(eachSysSystemVO -> {
                    dataList.add(Arrays.asList(
                            eachSysSystemVO.getSystemName(),
                            eachSysSystemVO.getSystemKey(),
                            eachSysSystemVO.getDescription(),
                            eachSysSystemVO.getState().toString(),
                            eachSysSystemVO.getCreateUid(),
                            eachSysSystemVO.getCreateTime().toString()
                    ));
                });
            }
            writer.write0(dataList, sheet, table);

            // 下载EXCEL
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            writer.finish();
            out.flush();

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ResultVO.getSuccess("导出系统列表EXCEL成功");
    }

    /**
     * 数据里很大（几百万都行）： 多个SHEET分批查询导出
     *
     * @param sysSystemVO
     * @param response
     * @return
     * @throws Exception
     */
    public ResultVO<Void> exportSysSystemExcel(SysSystemVO sysSystemVO, HttpServletResponse response) throws Exception {

        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);

            // 设置EXCEL名称
            String fileName = new String(("SystemExcel").getBytes(), "UTF-8");

            // 设置SHEET名称
            String sheetName = "系统列表sheet";

            // 设置标题
            Table table = new Table(1);
            List<List<String>> titles = new ArrayList<List<String>>();
            titles.add(Arrays.asList("系统名称"));
            titles.add(Arrays.asList("系统标识"));
            titles.add(Arrays.asList("描述"));
            titles.add(Arrays.asList("状态"));
            titles.add(Arrays.asList("创建人"));
            titles.add(Arrays.asList("创建时间"));
            table.setHead(titles);

            // 查询总数并封装相关变量(这块直接拷贝就行了不要改)
            Integer totalRowCount = 1;
            Integer perSheetRowCount = ExcelConstant.PER_SHEET_ROW_COUNT;
            Integer pageSize = ExcelConstant.PER_WRITE_ROW_COUNT;
            Integer sheetCount = totalRowCount % perSheetRowCount == 0 ? (totalRowCount / perSheetRowCount) : (totalRowCount / perSheetRowCount + 1);
            Integer previousSheetWriteCount = perSheetRowCount / pageSize;
            Integer lastSheetWriteCount = totalRowCount % perSheetRowCount == 0 ?
                    previousSheetWriteCount :
                    (totalRowCount % perSheetRowCount % pageSize == 0 ? totalRowCount % perSheetRowCount / pageSize : (totalRowCount % perSheetRowCount / pageSize + 1));


            for (int i = 0; i < sheetCount; i++) {

                // 创建SHEET
                Sheet sheet = new Sheet(i, 0);
                sheet.setSheetName(sheetName + i);

                // 写数据 这个j的最大值判断直接拷贝就行了，不要改动
                for (int j = 0; j < (i != sheetCount - 1 ? previousSheetWriteCount : lastSheetWriteCount); j++) {
                    List<List<String>> dataList = new ArrayList<>();

                    // 此处查询并封装数据即可 currentPage, pageSize这俩个变量封装好的 不要改动
                    PageHelper.startPage(j + 1 + previousSheetWriteCount * i, pageSize);
                    List<SysSystemVO> sysSystemVOList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(sysSystemVOList)) {
                        sysSystemVOList.forEach(eachSysSystemVO -> {
                            dataList.add(Arrays.asList(
                                    eachSysSystemVO.getSystemName(),
                                    eachSysSystemVO.getSystemKey(),
                                    eachSysSystemVO.getDescription(),
                                    eachSysSystemVO.getState().toString(),
                                    eachSysSystemVO.getCreateUid(),
                                    eachSysSystemVO.getCreateTime().toString()
                            ));
                        });
                    }
                    writer.write0(dataList, sheet, table);
                }
            }
            // 下载EXCEL
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            writer.finish();
            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ResultVO.getSuccess("导出系统列表EXCEL成功");
    }


}
