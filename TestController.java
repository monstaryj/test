package com.yjq.springbootdemoprometheus.controller;

import com.yjq.springbootdemoprometheus.colletor.StrCollectorRegistry;
import com.yjq.springbootdemoprometheus.colletor.StrMetric;
import io.prometheus.client.CollectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author monstar
 * @create 2020/11/1 17:07
 */
@RestController
public class TestController {
    @Autowired
    StrCollectorRegistry strCollectorRegistry;

    @Autowired
    CollectorRegistry collectorRegistry;

    @GetMapping("/add")
    public String add(){
        StrMetric builder=new StrMetric.Builder().help("test")
                .name("test")
                .register(strCollectorRegistry);
        System.out.println(builder);
        System.out.println("add");
        builder.set("test01");
        builder.set("test02");
        builder.set("test03");
        builder.set("test04");
        System.out.println(builder.get());
        return "add";
    }

    @GetMapping("/adda")
    public String addTest(){
        StrMetric builder=new StrMetric.Builder().help("test1")
                .name("test1")
                .register(strCollectorRegistry);
        System.out.println(builder);
        System.out.println("add1");
        builder.set("testa01");
        builder.set("testa02");
        builder.set("testa03");
        builder.set("testa04");
        System.out.println(builder.get());
        return "adda";
    }
}
