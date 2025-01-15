package com.likc.tool.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.ama.recharge.excel.handler.DateConstrainHandler;
import com.ama.recharge.excel.handler.DropDownConstrainHandler;
import com.ama.recharge.excel.handler.ExcelMergeHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ExcelUtils {

    /**
     * 动态头写入
     */
    public static void writeDynamicHead(HttpServletResponse response, String fileName, Set<String> includeColumnNames, Set<String> excludeColumnFieldNames, Boolean isMerge,
                                        List<Integer> dateVerifyRowList, Map<Integer, List<String>> selectParamMap, List<List<String>> headList, List<?> data) throws IOException {
        write(response, fileName, includeColumnNames, excludeColumnFieldNames, isMerge, dateVerifyRowList, selectParamMap, headList, data, null);
    }

    /**
     * 固定头写入
     */
    public static void writeFixationHead(HttpServletResponse response, String fileName, Set<String> includeColumnNames, Set<String> excludeColumnFieldNames, Boolean isMerge,
                                         List<Integer> dateVerifyRowList, Map<Integer, List<String>> selectParamMap, List<?> data, Class<?> clazz) throws IOException {
        write(response, fileName, includeColumnNames, excludeColumnFieldNames, isMerge, dateVerifyRowList, selectParamMap, null, data, clazz);
    }

    /**
     * 最简单写入
     */
    public static void easyWrite(HttpServletResponse response, String fileName, List<?> data, Class<?> clazz) throws IOException {
        writeFixationHead(response, fileName, null, null, null, null, null, data, clazz);
    }

    /**
     *
     * @param response
     * @param fileName 文件名
     * @param includeColumnNames 选择导出的字段
     * @param excludeColumnFieldNames 排除导出的字段
     * @param isMerge 是否合并
     * @param dateVerifyRowList 日期yyyy-MM-dd校验的列号列表
     * @param selectParamMap 下拉框数据
     * @param headList 动态表格头列表
     * @param data 数据
     * @param clazz 对象转表格头
     * @throws IOException
     */
    public static void write(HttpServletResponse response, String fileName, Set<String> includeColumnNames, Set<String> excludeColumnFieldNames, Boolean isMerge,
                             List<Integer> dateVerifyRowList, Map<Integer, List<String>> selectParamMap, List<List<String>> headList, List<?> data, Class<?> clazz) throws IOException {
        setExcelResponse(response, fileName);
        ExcelWriterBuilder writerBuilder = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE);

        if (CollectionUtils.isNotEmpty(includeColumnNames)) {
            writerBuilder.includeColumnFieldNames(includeColumnNames);
        }

        if (CollectionUtils.isNotEmpty(excludeColumnFieldNames)) {
            writerBuilder.excludeColumnFieldNames(excludeColumnFieldNames);
        }

        if (CollectionUtils.isNotEmpty(headList)) {
            writerBuilder.head(headList);
        }

        if (Objects.nonNull(clazz)) {
            writerBuilder.head(clazz);
        }

        if (isMerge) {
            writerBuilder.registerWriteHandler(new ExcelMergeHandler(clazz));
        }

        if (MapUtils.isNotEmpty(selectParamMap)) {
            writerBuilder.registerWriteHandler(new DropDownConstrainHandler(selectParamMap));
        }

        if (CollectionUtils.isNotEmpty(dateVerifyRowList)) {
            writerBuilder.registerWriteHandler(new DateConstrainHandler(dateVerifyRowList));
        }

        writerBuilder.registerConverter(new LongStringConverter())
                .registerWriteHandler(new HorizontalCellStyleStrategy(headWriteCellStyle(), contentWriteCellStyle()))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .excelType(ExcelTypeEnum.XLSX)
                .sheet(fileName)
                .doWrite(data);
    }

//    public static Map<Integer, String> initImageMap(MultipartFile file, String name) {
//        Workbook workBook = getWorkBook(file);
//        Map<Integer, String> imageMap;
//        if (workBook instanceof HSSFWorkbook) {
//            imageMap = ExcelUtils.initXlsImageMap(workBook, name);
//        } else {
//            imageMap = ExcelUtils.initXlsxImageMap(workBook, name);
//        }
//        return imageMap;
//    }
//
//    public static Map<Integer, String> initXlsxImageMap(Workbook workbook, String name) {
//        XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);
//        Map<Integer, String> map = new HashMap<>(16);
//        List<POIXMLDocumentPart> documentParts = sheet.getRelations();
//        for (POIXMLDocumentPart documentPart : documentParts) {
//            if (documentPart instanceof XSSFDrawing) {
//                XSSFDrawing drawing = (XSSFDrawing) documentPart;
//                List<XSSFShape> shapes = drawing.getShapes();
//                for (XSSFShape shape : shapes) {
//                    XSSFPicture picture = (XSSFPicture) shape;
//                    XSSFPictureData pictureData;
//                    File file;
//                    try {
//                        pictureData = picture.getPictureData();
//                        if (pictureData.getData() == null || pictureData.getData().length == 0) {
//                            continue;
//                        }
//                        String md5 = DigestUtils.md5DigestAsHex(pictureData.getData());
//                        file = FileUtils.createTempFile("upload_" + md5, ".jpeg");
//                    } catch (Exception e) {
//                        continue;
//                    }
//
//                    try (FileOutputStream fos = new FileOutputStream(file)) {
//                        fos.write(pictureData.getData());
//                    } catch (IOException e) {
//                        continue;
//                    }
//                    CosUtil.putObject(name + "/" + file.getName(), file);
//
//                    XSSFClientAnchor anchor;
//                    try {
//                        anchor = picture.getPreferredSize();
//                    } catch (Exception e) {
//                        continue;
//                    }
//                    CTMarker marker = anchor.getFrom();
//                    // key 行号，value 云服务器图片地址
//                    map.put(marker.getRow() + 1, String.format("https://doc-1307444343.cos.ap-guangzhou.myqcloud.com/ecommerce/%s", file.getName()));
//                }
//            }
//        }
//        return map;
//    }
//
//    public static Map<Integer, String> initXlsImageMap(Workbook workbook, String name) {
//        HSSFSheet sheet = (HSSFSheet) workbook.getSheetAt(0);
//        Map<Integer, String> map = new HashMap<>(16);
//        for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
//            HSSFPicture picture = (HSSFPicture) shape;
//            HSSFClientAnchor xssfClientAnchor = (HSSFClientAnchor) picture.getAnchor();
//            HSSFPictureData pictureData;
//            File file;
//            try {
//                pictureData = picture.getPictureData();
//                String md5 = DigestUtils.md5DigestAsHex(pictureData.getData());
//                file = FileUtils.createTempFile("upload_" + md5, ".jpeg");
//            } catch (Exception e) {
//                continue;
//            }
//
//            try (FileOutputStream fos = new FileOutputStream(file)) {
//                fos.write(pictureData.getData());
//            } catch (IOException e) {
//                continue;
//            }
//            CosUtil.putObject(name + "/" + file.getName(), file);
//            map.put(xssfClientAnchor.getRow1() + 1, String.format("https://doc-1307444343.cos.ap-guangzhou.myqcloud.com/ecommerce/%s", file.getName()));
//        }
//        return map;
//    }

//    public static Workbook getWorkBook(MultipartFile file) {
//        //获得文件名
//        String fileName = file.getOriginalFilename();
//        if (StringUtils.isBlank(fileName)) {
//            throw new BizException("文件名为空");
//        }
//        //创建Workbook工作薄对象，表示整个excel
//        Workbook workbook = null;
//        try {
//            //获取excel文件的io流
//            InputStream is = file.getInputStream();
//            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
//            if(fileName.endsWith("xls")){
//                //2003
//                workbook = new HSSFWorkbook(is);
//            }else if(fileName.endsWith("xlsx")){
//                //2007
//                workbook = new XSSFWorkbook(is);
//            }
//        } catch (IOException e) {
//            throw new BizException(e.getMessage());
//        }
//        return workbook;
//    }

    private static WriteCellStyle headWriteCellStyle() {
        // 创建一个写出的单元格样式对象
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 创建写出Excel的字体对象
        WriteFont headWriteFont = new WriteFont();
        // 粗体
        headWriteFont.setBold(true);
        // 设置字体样式
        headWriteFont.setFontName("宋体");
        // 设置字体大小
        headWriteFont.setFontHeightInPoints((short)14);
        headWriteCellStyle.setWriteFont(headWriteFont);

        //设置文字居中
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //设置文字垂直居中
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置单元格上边框为细线
        headWriteCellStyle.setBorderTop(BorderStyle.THIN);
        headWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        headWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        headWriteCellStyle.setBorderRight(BorderStyle.THIN);
        // 设置表头单元格背景颜色为灰色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return headWriteCellStyle;
    }

    private static WriteCellStyle contentWriteCellStyle() {
        // 创建一个写出的单元格样式对象
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();

        WriteFont contentWriteFont = new WriteFont();
        // 设置字体样式
        contentWriteFont.setFontName("宋体");
        // 设置字体大小
        contentWriteFont.setFontHeightInPoints((short)12);
        contentWriteCellStyle.setWriteFont(contentWriteFont);

        //设置文字水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //设置文字垂直居中
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置单元格上边框为细线
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        return contentWriteCellStyle;
    }

    public static void setExcelResponse(HttpServletResponse response, String rawFileName) throws UnsupportedEncodingException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(rawFileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

}
