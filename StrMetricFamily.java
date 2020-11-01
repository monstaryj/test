package com.yjq.springbootdemoprometheus.colletor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author monstar
 * @create 2020/11/1 15:53
 */
public class StrMetricFamily extends StrCollector.MetricFamilySamples {
    private final List<String> labelNames;

    public StrMetricFamily(String name, String help, String value) {
        super(name, StrCollector.Type.Str, help, new ArrayList());
        this.labelNames = Collections.emptyList();
        this.samples.add(new Sample(name, this.labelNames, Collections.emptyList(), value));
    }

    public StrMetricFamily(String name, String help, List<String> labelNames) {
        super(name, StrCollector.Type.Str, help, new ArrayList());
        this.labelNames = labelNames;
    }

    public StrMetricFamily addMetric(List<String> labelValues, String value) {
        if (labelValues.size() != this.labelNames.size()) {
            throw new IllegalArgumentException("Incorrect number of labels.");
        } else {
            this.samples.add(new Sample(this.name, this.labelNames, labelValues, value));
            return this;
        }
    }
}

