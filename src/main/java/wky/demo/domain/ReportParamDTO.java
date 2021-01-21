package wky.demo.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ReportParamDTO implements Serializable {


    private String url;


    private String format = "pdf";

    /**
     * 这个list里面必须是key-value 的一个对象
     */

    private List fields;


    private Map<String,Object> param;

    public ReportParamDTO(String url, String format, List<Object> fields, Map<String, Object> param) {
        this.url = url;
        this.format = format;
        this.fields = fields;
        this.param = param;
    }

    public ReportParamDTO() {
    }
}
