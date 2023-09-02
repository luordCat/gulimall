package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.common.constant.order.PaymentConstant.PayType;
import com.atguigu.common.vo.order.alipay.AliPayAsyncVO;
import com.atguigu.gulimall.order.config.AliPayConfig;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.impl.PayContextStrategy;
import com.atguigu.gulimall.order.service.impl.alipay.AliPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 订单支付成功监听器
 *
 */
@RestController
public class OrderPayedListener {

    @Autowired
    PayContextStrategy payContextStrategy;

    @Autowired
    AliPayConfig aliPayConfig;

    @Autowired
    AliPayServiceImpl aliPayService;

    /**
     * 支付宝支付异步通知
     * 只有支付成功会触发
     */
    @PostMapping(value = "/payed/test")
    public String test(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String key : parameterMap.keySet()) {
            String value = request.getParameter(key);
            System.out.println("参数名：" + key + "==》参数值" + value);
        }
        System.out.println("支付宝通知到位了。。。数据" + parameterMap);
        return "success";
    }

    //39674q16e1.goho.co/payed/ali/notify
    //hwhuss7667@sandbox.com沙箱账号

//    @PostMapping(value = "/payed/ali/notify")
//    public String handleAliNotify(HttpServletRequest request, AliPayAsyncVO asyncVo) throws AlipayApiException, UnsupportedEncodingException {
//        asyncVo.setPayCode(PayType.ALI_PAY.getCode());// 封装付款类型
//        Boolean result = payContextStrategy.notify(PayType.ALI_PAY, request, asyncVo);
//        if (result) {
//            return "success";// 返回success，支付宝将不再异步回调
//        }
//        return "error";
//    }

    //@PostMapping(value = "/pay/notify")
    //public String asyncNotify(@RequestBody String notifyData) {
    //    //异步通知结果
    //    return orderService.asyncNotify(notifyData);
    //}
    @PostMapping(value = "/payed/ali/notify")
    public String handleAliNotify(HttpServletRequest request, AliPayAsyncVO asyncVo) throws AlipayApiException, UnsupportedEncodingException {
//        Map<String, String[]> map = request.getParameterMap();
//        for (String key : map.keySet()) {
//            String value = request.getParameter(key);
//            System.out.println(key + " -> " + value);
//        }

        //防止别人伪造支付宝的数据

        //验签：是不是支付宝给我们返回的数据
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipay_public_key(), aliPayConfig.getCharset(), aliPayConfig.getSign_type()); //调用SDK验证签名
        if (signVerified) {
            System.out.println("签名验证ok");
            aliPayService.handlePayResult(asyncVo);
            return "success";
        } else {
            System.out.println("签名验证失败");
            return "error";
        }
    }


}

