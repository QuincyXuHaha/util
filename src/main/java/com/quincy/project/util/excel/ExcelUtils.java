package com.quincy.project.util.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * excel导出工具
 *
 * @author xuguangquan
 * @date 2019/10/31 星期四
 */
@Slf4j
public class ExcelUtils {

    private static final int ROW_ACCESS_WINDOW_SIZE = 100;
    private static final int SHEET_MAX_ROW = 100000;

    /**
     * excel 导出
     *
     * @param response         响应
     * @param fileName         无后缀的文件名
     * @param list             数据内容
     * @param excelHeaderInfos 表头
     * @param formatInfo       数据内容格式信息
     */
    public static void sendHttpResponse(HttpServletResponse response,
                                        String fileName,
                                        List<Object> list,
                                        List<ExcelHeaderInfo> excelHeaderInfos,
                                        Map<String, ExcelFormat> formatInfo) {
        try {
            fileName = new String((fileName + System.currentTimeMillis() + ".xlsx").getBytes(), StandardCharsets.UTF_8);
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), StandardCharsets.UTF_8));
            response.addHeader("Param", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            OutputStream os = response.getOutputStream();
            Workbook workbook = getWorkbook(list, excelHeaderInfos, formatInfo);
            workbook.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取excel工作簿
     *
     * @param list             数据
     * @param excelHeaderInfos 表头
     * @param formatInfo       格式
     * @return excel工作簿
     */
    private static Workbook getWorkbook(List<Object> list, List<ExcelHeaderInfo> excelHeaderInfos, Map<String, ExcelFormat> formatInfo) {
        Workbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
        String[][] datas = transformData(list);
        Field[] fields = list.get(0).getClass().getDeclaredFields();
        CellStyle style = setCellStyle(workbook);
        int pageRowNum = 0;
        Sheet sheet = null;

        long startTime = System.currentTimeMillis();
        log.info("开始处理excel文件。。。");

        for (int i = 0; i < datas.length; i++) {
            // 如果是每个sheet的首行
            if (i % SHEET_MAX_ROW == 0) {
                // 创建新的sheet
                sheet = createSheet(workbook, i);
                // 行号重置为0
                pageRowNum = 0;
                for (int j = 0; j < getHeaderRowNum(excelHeaderInfos); j++) {
                    sheet.createRow(pageRowNum++);
                }
                createHeader(excelHeaderInfos, sheet, style);
            }
            // 创建内容
            Row row = sheet.createRow(pageRowNum++);
            createContent(formatInfo, row, style, datas, i, fields);
        }
        log.info("处理文本耗时" + (System.currentTimeMillis() - startTime) + "ms");
        return workbook;
    }

    /**
     * 创建表头
     *
     * @param excelHeaderInfos 表头
     * @param sheet            表
     * @param style            单元格样式
     */
    private static void createHeader(List<ExcelHeaderInfo> excelHeaderInfos, Sheet sheet, CellStyle style) {
        for (ExcelHeaderInfo excelHeaderInfo : excelHeaderInfos) {
            Integer lastRow = excelHeaderInfo.getLastRow();
            Integer firstRow = excelHeaderInfo.getFirstRow();
            Integer lastCol = excelHeaderInfo.getLastCol();
            Integer firstCol = excelHeaderInfo.getFirstCol();

            // 行距或者列距大于0才进行单元格融合
            if ((lastRow - firstRow) != 0 || (lastCol - firstCol) != 0) {
                sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
            }
            // 获取当前表头的首行位置
            Row row = sheet.getRow(firstRow);
            // 在表头的首行与首列位置创建一个新的单元格
            Cell cell = row.createCell(firstCol);
            // 赋值单元格
            cell.setCellValue(excelHeaderInfo.getTitle());
            cell.setCellStyle(style);
            sheet.setColumnWidth(firstCol, sheet.getColumnWidth(firstCol) * 17 / 12);
        }
    }

    /**
     * 填充第i行正文
     *
     * @param formatInfo 正文格式
     * @param row        行
     * @param style      样式
     * @param content    内容
     * @param i          行数
     * @param fields     字段名
     */
    private static void createContent(Map<String, ExcelFormat> formatInfo, Row row, CellStyle style, String[][] content, int i,
                                      Field[] fields) {
        List<String> columnNames = getBeanProperty(fields);
        for (int j = 0; j < columnNames.size(); j++) {
            // 如果格式化Map为空，默认为字符串格式
            if (formatInfo == null) {
                row.createCell(j).setCellValue(content[i][j]);
                continue;
            }
            if (formatInfo.containsKey(columnNames.get(j))) {
                switch (formatInfo.get(columnNames.get(j)).getValue()) {
                    case "DOUBLE":
                        row.createCell(j).setCellValue(Double.parseDouble(content[i][j]));
                        break;
                    case "INTEGER":
                        row.createCell(j).setCellValue(Integer.parseInt(content[i][j]));
                        break;
                    case "PERCENT":
                        style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00%"));
                        Cell cell = row.createCell(j);
                        cell.setCellStyle(style);
                        cell.setCellValue(Double.parseDouble(content[i][j]));
                        break;
                    case "DATE":
                        style.setDataFormat(HSSFDataFormat.getBuiltinFormat("yyyy-MM-dd HH:mm:ss"));
                        Cell cell1 = row.createCell(j);
                        cell1.setCellStyle(style);
                        row.createCell(j).setCellValue(parseDate(content[i][j]));
                        break;
                    default:
                        log.error("错误的类型:{}", formatInfo.get(columnNames.get(j)).getValue());
                }
            } else {
                row.createCell(j).setCellValue(content[i][j]);
            }
        }
    }

    /**
     * 将原始数据转成二维数组，因为表是一个二维维度，有行列之分
     *
     * @param list 内容
     * @return 二维数组
     */
    private static String[][] transformData(List<Object> list) {
        int dataSize = list.size();
        String[][] datas = new String[dataSize][];
        // 获取报表的列数
        Field[] fields = list.get(0).getClass().getDeclaredFields();
        // 获取实体类的字段名称数组
        List<String> columnNames = getBeanProperty(fields);
        for (int i = 0; i < dataSize; i++) {
            datas[i] = new String[fields.length];
            for (int j = 0; j < fields.length; j++) {
                try {
                    // 赋值
                    datas[i][j] = BeanUtils.getProperty(list.get(i), columnNames.get(j));
                } catch (Exception e) {
                    log.error("获取对象属性值失败");
                    e.printStackTrace();
                }
            }
        }
        return datas;
    }

    /**
     * 获取实体类的字段名称数组
     *
     * @param fields 内容对象的字段属性
     * @return 字段名称数组
     */
    private static List<String> getBeanProperty(Field[] fields) {
        List<String> columnNames = new ArrayList<>();
        for (Field field : fields) {
            String[] strings = field.toString().split("\\.");
            String columnName = strings[strings.length - 1];
            columnNames.add(columnName);
        }
        return columnNames;
    }

    /**
     * 新建表格
     *
     * @param workbook 工作簿
     * @param i        行号
     * @return 表格
     */
    private static Sheet createSheet(Workbook workbook, int i) {
        int sheetNum = i / SHEET_MAX_ROW;
        workbook.createSheet("sheet" + sheetNum);
        //动态指定当前的工作表
        return workbook.getSheetAt(sheetNum);
    }

    /**
     * 获取标题总行数
     *
     * @param headerInfos 表头信息
     * @return 标题总行数
     */
    private static int getHeaderRowNum(List<ExcelHeaderInfo> headerInfos) {
        Integer maxRowNum = 0;
        for (ExcelHeaderInfo excelHeaderInfo : headerInfos) {
            Integer tmpRowNum = excelHeaderInfo.getLastRow();
            if (tmpRowNum > maxRowNum) {
                maxRowNum = tmpRowNum;
            }
        }
        return maxRowNum + 1;
    }

    /**
     * 设置总体表格样式
     *
     * @param workbook 工作簿
     * @return 单元格样式
     */
    private static CellStyle setCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * 字符串转日期
     *
     * @param strDate 字符串
     * @return 日期
     */
    private static Date parseDate(String strDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(strDate);
        } catch (Exception e) {
            log.error("字符串转日期错误");
            e.printStackTrace();
        }
        return date;
    }
}
