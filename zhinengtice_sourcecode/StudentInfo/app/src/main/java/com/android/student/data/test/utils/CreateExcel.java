package com.android.student.data.test.utils;

import android.util.Log;

import java.io.File;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Created by hailong on 2016/11/13.
 */
public class CreateExcel {
    static CreateExcel createExcel;
    // 准备设置excel工作表的标题
    private WritableSheet sheet;
    /**
     * 创建Excel工作薄
     */
    private WritableWorkbook wwb;
    private String[] title;
    WritableCellFormat wc = new WritableCellFormat();
    WritableCellFormat wc1 = new WritableCellFormat();

    public static CreateExcel getInstance() {
        if (createExcel == null) {
            createExcel = new CreateExcel();
        }

        return createExcel;
    }


    public void excelCreate(String[] title, boolean isUsb, String targetPath,String fileName, String sheetName) {
        this.title = title;
        try {
            /**输出的excel文件的路径*/
            File tarFile = null;
            if (!isUsb) {
                //TODO
                tarFile = new File(/*Environment.getExternalStorageDirectory() + "/学生名单导出/"*/targetPath);
            } else {
//                String targetPath = LaunchActivity.USB_Datalist_path;
//                File tmp = new File(targetPath);
//                if(!tmp.exists()){
//                    targetPath = LaunchActivity.USB_Datalist_Extra_path0;
//                    tmp = new File(targetPath);
//                    if(!tmp.exists()){
//                        targetPath = LaunchActivity.USB_Datalist_Extra_path2;
//                        tmp = new File(targetPath);
//                        if(!tmp.exists()){
//                            targetPath = LaunchActivity.USB_Datalist_Extra_path3;
//                        }
//                    }
//                }
                tarFile = new File(targetPath/* + "/学生名单导出/"*/);
            }
            if (tarFile.exists()) {
                tarFile.delete();
            }
            tarFile.mkdirs();
            File file = new File(tarFile, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            wwb = Workbook.createWorkbook(file);
            /**添加第一个工作表并设置第一个Sheet的名字*/
            sheet = wwb.createSheet(sheetName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Label label;
        try {
            // 设置居中
            wc.setAlignment(Alignment.CENTRE);
            // 设置边框线
            WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            wc.setFont(bold);
            wc1.setAlignment(Alignment.CENTRE);
        } catch (Exception e) {

        }
        try {
            for (int i = 0; i < title.length; i++) {
                /**Label(x,y,z)其中x代表单元格的第x+1列，第y+1行, 单元格的内容是y
                 * 在Label对象的子对象中指明单元格的位置和内容
                 * */
                label = new Label(i, 0, title[i]);
                label.setCellFormat(wc);
                /**将定义好的单元格添加到工作表中*/
                sheet.addCell(label);
            }
        } catch (Exception e) {

        }
    }

    public void saveDataToExcel(int index, String[] content) throws Exception {

        /*
         * 把数据填充到单元格中
		 * 需要使用jxl.write.Number
		 * 路径必须使用其完整路径，否则会出现错误
		 */
        for (int i = 0; i < title.length; i++) {
            Label labeli = new Label(i, index, content[i]);
            labeli.setCellFormat(wc1);
            sheet.addCell(labeli);
        }

    }

    public void close() {
        try {
            // 写入数据
            wwb.write();
            // 关闭文件
            wwb.close();
        } catch (Exception e) {
            Log.d("hailong", " close Exception ");
        }
    }

}
