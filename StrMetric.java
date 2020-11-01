package com.yjq.springbootdemoprometheus.colletor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author monstar
 * @create 2020/11/1 15:49
 */
public class StrMetric extends SimpleStrCollector<StrMetric.Child> implements StrCollector.Describable {
    StrMetric(Builder b) {
        super(b);
    }

    public static Builder build(String name, String help) {
        return (Builder)((Builder)(new Builder()).name(name)).help(help);
    }

    public static Builder build() {
        return new Builder();
    }

    protected Child newChild() {
        return new Child();
    }

    public void set(String value){
        ((Child)this.noLabelsChild).set(value);
    }

    public String get() {
        return ((Child)this.noLabelsChild).get();
    }

    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples.Sample> samples = new ArrayList(this.children.size());
        Iterator var2 = this.children.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<List<String>, Child> c = (Map.Entry)var2.next();
            samples.add(new MetricFamilySamples.Sample(this.fullname, this.labelNames, (List)c.getKey(), ((Child)c.getValue()).get()));
        }

        return this.familySamplesList(Type.Str, samples);
    }

    public List<MetricFamilySamples> describe() {
        return Collections.singletonList(new StrMetricFamily(this.fullname, this.help, this.labelNames));
    }

    public static class Child {
        private final StringBuilder value = new StringBuilder();

        public Child() {
        }
        public void set(String value){
            if(value.isEmpty()){
                throw new IllegalArgumentException("Amount to increment must be non-negative.");
            }else{
                this.value.append(value);
            }
        }

        public String get(){
            return this.value.toString();
        }

    }

    public static class Builder extends SimpleStrCollector.Builder<Builder, StrMetric> {
        public Builder() {
        }

        public StrMetric create() {
            return new StrMetric(this);
        }
    }
}

