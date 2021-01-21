package wky.demo;

import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class Test {
    public static void main(String[] args) throws Exception {


        readPdf();//直接读全PDF面

    }

    public static void readPdf()throws Exception{
        String pageContent = "";
        File file = new File("C:\\Users\\Administrator\\Downloads\\单位参保人员缴费查询打印.pdf");
        if (!file.exists()){
            file.createNewFile();
        }
        PdfDocument document = new PdfDocument();
        //PdfWriter writer = new PdfWriter(document,new FileOutputStream(file));
        try {
            com.itextpdf.text.pdf.PdfReader reader = new PdfReader(new FileInputStream("C:\\Users\\Administrator\\Downloads\\单位参保人员缴费查询打印.pdf"));
            int pageNum = reader.getNumberOfPages();
            for(int i=1;i<=pageNum;i++){
                pageContent += PdfTextExtractor.getTextFromPage(reader, i);//读取第i页的文档内容
            }



               System.out.println(pageContent);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
        }
    }


}
