package wky.demo.util;

import com.itextpdf.text.pdf.parser.*;
import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import wky.demo.SealXYUtil;
import wky.demo.domain.ReportParamDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * 盖章工具类
 */
@Slf4j
@Component
public class IreportUtil {
    private final static String ZHANG_URL = "C:\\b.jpg";
    private final static String temPDFPath = "C:\\Users\\Administrator\\Downloads\\";
    // 指定关键字
    public static String KEY_WORD = "经办机构";
    // PDF当前页数
    public static int curPage = 0;


    /**
     * @param reportName     报表名称
     * @param reportParamDTO 报表入参dto
     */
    @SneakyThrows
    public static void createReport(String reportName, ReportParamDTO reportParamDTO) {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        //获取文件流
        ClassPathResource resource = new ClassPathResource("jaspers" + File.separator + reportName + ".jasper");

        JasperReport jasperReport = null;
        // 报表数据源
        List fields = reportParamDTO.getFields();
        //数据填充
        JRDataSource jrDataSource = new JRBeanCollectionDataSource(fields);
        Map<String, Object> param = reportParamDTO.getParam();

        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "inline;");


        try (final OutputStream outputStream = response.getOutputStream();
             InputStream jasperStream = resource.getInputStream();) {
            //加载模板
            jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
            //报表填充
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, param, jrDataSource);

            //临时文件随机命名
            String temPath = temPDFPath + UUID.randomUUID().toString().replace("-", "") + ".pdf";

            //PDF临时存放
            File file = new File(temPath);
            if (!file.exists()) {
                file.createNewFile();
            }


            //PDF数据填充，此时还未盖章，临时存放template.pdf
            JasperExportManager.exportReportToPdfFile(jasperPrint, temPath);


            Document doc = new Document();


            com.itextpdf.text.pdf.PdfReader reader = new com.itextpdf.text.pdf.PdfReader(new FileInputStream(temPath));
            int pageNum = reader.getNumberOfPages();


            //图片盖章
            //读取PDF文件
            //PdfReader reader = new PdfReader(temPDFPath);
            //com.itextpdf.text.pdf.PdfStamper stamp = new com.itextpdf.text.pdf.PdfStamper(reader, outputStream);
            String pDFContent = null;

            for (int i = 1; i <= pageNum; i++) {
                pDFContent += PdfTextExtractor.getTextFromPage(reader, i);//读取第i页的文档内容
            }
            boolean existDep = pDFContent.contains("经办机构");
            byte[] content = pDFContent.getBytes();

            //获取到经办机构的坐标和页码
            float[] keyInfo = SealXYUtil.findKeyInfo("经办机构");


            //有经办机构字眼
            if (existDep) {
                //List<float[]> locationInfo = identifyKey(content, "经办机构");
                PdfReader pdfReader = new PdfReader(new FileInputStream(temPath));
                PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(temPath));

                Image image = Image.getInstance(ZHANG_URL);
                PdfContentByte under1 = pdfStamper.getOverContent((int) keyInfo[0]);
                image.scaleAbsolute(100, 100);
                image.setAbsolutePosition((int) keyInfo[1] + 150, (int) keyInfo[2] + 50);
                under1.addImage(image);


                pdfStamper.close();
                pdfReader.close();


                //以PDF方式响应给浏览器
                //JasperExportManager.exportReportToPdfStream(new FileInputStream(temPDFPath), outputStream);
                OutputStream out = response.getOutputStream();
                InputStream in = new FileInputStream(temPath);
                byte[] bytes = new byte[1024];
                while ((in.read(bytes)) != -1) {
                    out.write(bytes);
                }
                in.close();
                out.close();

                //删除PDF临时文件
                file.delete();


                //没有经办机构字眼
            } else {
                //没有经办机构字眼,需要确定字眼写入位置
                //找出打印人跟打印信息这两处字眼的坐标，然后去X坐标和的均值作为经办机构字眼X坐标
                float[] dayinPer = SealXYUtil.findKeyInfo("打印人");
                float[] dayinDate = SealXYUtil.findKeyInfo("打印日期");
                PdfReader pdfReader = new PdfReader(new FileInputStream(temPath));

                PdfWriter pdfWriter = PdfWriter.getInstance(doc, new FileOutputStream(temPath));
                doc.open();
                PdfContentByte contentByte = pdfWriter.getDirectContentUnder();

                //打印人X轴坐标
                float one = dayinPer[1];

                //打印日期X坐标
                float two = dayinDate[1];

                //确定经办机构字眼坐标
                float wordXLocation = (one + two) / 2;
                float wordYLocation = dayinDate[2];

                //中文字体
                BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);

                //添加字眼
                contentByte.beginText();
                contentByte.setFontAndSize(bfChinese, 12);
                contentByte.setTextMatrix(wordXLocation, wordYLocation);
                contentByte.showText("经办机构");
                contentByte.endText();

//                //确定经办机构所在坐标和页码
                PdfReader pdfReader1 = new PdfReader(new FileInputStream(temPath));
                PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(temPath));
                //获取公章
                Image image = Image.getInstance(ZHANG_URL);
                // 要操作的页面
                PdfContentByte under1 = pdfStamper.getOverContent((int) dayinDate[2]);
                // 根据域的大小缩放图片
                image.scaleAbsolute(100, 100);
                // 添加图片
                image.setAbsolutePosition((int) wordXLocation, (int) wordYLocation);
                under1.addImage(image);


                pdfStamper.close();
                pdfReader1.close();

                //以PDF方式响应给浏览器
                //JasperExportManager.exportReportToPdfStream(new FileInputStream(temPDFPath), outputStream);

                //以PDF方式响应给浏览器
                //JasperExportManager.exportReportToPdfStream(new FileInputStream(temPDFPath), outputStream);
                OutputStream out = response.getOutputStream();
                InputStream in = new FileInputStream(temPath);
                int len = 1;
                byte[] bytes = new byte[1024];
                while ((len = in.read(bytes)) != -1) {
                    out.write(bytes);
                }
                in.close();
                out.close();

                //删除PDF临时文件
                file.delete();

            }
            //删除PDF临时文件
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
