package com.example.demo.controller;

import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.security.provider.MD5;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
public class IndexController {

    private static class encrypt {

        static String md5(String s) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(s.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        }
    }

    private String sign(Map<String, String> map, String key) throws NoSuchAlgorithmException {
        // 排序
        ArrayList<String> sorted = new ArrayList<>(map.keySet());
        Collections.sort(sorted);

        String sign = "";
        for (String x : sorted) {
            if (x.equals("sign")) continue;
            if (map.get(x).isEmpty()) continue;

            sign += x + "=" + map.get(x) + "&";
        }
        System.out.println(sign);

        try {
            return encrypt.md5(sign + "key=" + key);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
    }

    private String key = ""; // 密钥

    @RequestMapping("/")
    public String index() {
        HashMap<String, String> params = new HashMap();
        params.put("mch_id", "100xxx"); // 商户ID
        params.put("out_trade_no", System.currentTimeMillis() + "");
        params.put("pay_type", "1"); // 支付方式：1微信扫码2支付宝扫码 3...
        params.put("amount", "500.00"); // 金额 单位元
        params.put("version", "2"); // 固定值 2
        params.put("notify_url", "http://127.0.0.1:8080/notify");
        params.put("time", (System.currentTimeMillis() / 1000) + ""); // 时间戳
//        params.put("mobile", 1); // 客户端，移动端1，0PC
//        params.put("ip", "127.0.0.1"); // IP
//        params.put("return_url", "http://127.0.0.1:8080/callback");
        try {
            params.put("sign", sign(params, key));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String out = "<form action=\"https://txcpay.com/api/v2/index/index\" name=\"form1\" method=\"POST\">";
        for (String x : params.keySet()) {
            out += "<input type=\"hidden\" name=\"" + x + "\" value=\"" + params.get(x) + "\" >";
        }
        out += "<input type=\"submit\"></form>";
        out += "<script>document.getElementById('form1').submit();</script>";

        return out;
    }

    @RequestMapping("/notify")
    public String notify(@RequestParam Map<String, String> params) {
        String out_trade_no = params.get("out_trade_no");
        try {
            // 验签
            if (params.get("sign").equals(sign(params, key))) return "fail";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "success";
    }

    @RequestMapping("/query")
    public String query() {
        HashMap<String, String> params = new HashMap();
        params.put("mch_id", "100xxx"); // 商户ID
        params.put("out_trade_no", System.currentTimeMillis() + "");
        params.put("time", (System.currentTimeMillis() / 1000) + ""); // 时间戳
        try {
            params.put("sign", sign(params, key));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Http.Post/Get
        // https://txcpay.com/api/v2/index/query
        // params

        return "ok";
    }

    @RequestMapping("/callback")
    public String callback(@RequestParam Map<String, Object> params) {
        return "success";
    }
}
