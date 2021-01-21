package wky.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wky.demo.domain.ReportParamDTO;
import wky.demo.util.IreportUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/a")
public class Test {
    @GetMapping("/aa")
    public void fun(){
        ReportParamDTO dto = new ReportParamDTO();
        dto.setFormat("aaaa");
        dto.setUrl("aaa");
        Map<String,Object> param = new HashMap<>();

        param.put("empName", "insuEmpInfo.getEmpName()");
        param.put("psnName", "staffPaymentPrintDTO.getPsnName()");
        param.put("certno", "staffPaymentPrintDTO.getCertno()");
        param.put("cashymBegn", "staffPaymentPrintDTO.getAccrymBegn() == null ? : staffPaymentPrintDTO.getAccrymBegn()");
        param.put("cashymEnd", "staffPaymentPrintDTO.getAccrymEnd() == null ?  : staffPaymentPrintDTO.getAccrymEnd()");
        param.put("agentName", "agentInfo.getEmpOpterName()");
        //param.put("empNo", 123);

        List<Object> list = new ArrayList<>();
        list.add(111);
        dto.setFields(null);
        dto.setParam(param);
        IreportUtil.createReport("yb_dwjfmxdy",dto);
    }
}
