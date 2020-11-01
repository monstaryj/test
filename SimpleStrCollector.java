package com.yjq.springbootdemoprometheus.colletor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author monstar
 * @create 2020/10/30 21:35
 */
public abstract class SimpleStrCollector<Child> extends StrCollector {
    protected final String fullname;
    protected final String help;
    protected final List<String> labelNames;
    protected final ConcurrentMap<List<String>, Child> children = new ConcurrentHashMap<>();
    protected Child noLabelsChild;

    public Child labels(String... labelValues) {
        if (labelValues.length != this.labelNames.size()) {
            throw new IllegalArgumentException("Incorrect number of labels.");
        } else {
            String[] var2 = labelValues;
            int var3 = labelValues.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String label = var2[var4];
                if (label == null) {
                    throw new IllegalArgumentException("Label cannot be null.");
                }
            }

            List<String> key = Arrays.asList(labelValues);
            Child c = this.children.get(key);
            if (c != null) {
                return c;
            } else {
                Child c2 = this.newChild();
                Child tmp = this.children.putIfAbsent(key, c2);
                return tmp == null ? c2 : tmp;
            }
        }
    }

    public void remove(String... labelValues) {
        this.children.remove(Arrays.asList(labelValues));
        this.initializeNoLabelsChild();
    }

    public void clear() {
        this.children.clear();
        this.initializeNoLabelsChild();
    }

    protected void initializeNoLabelsChild() {
        if (this.labelNames.size() == 0) {
            this.noLabelsChild = this.labels();
        }

    }

    public <T extends StrCollector> T setChild(Child child, String... labelValues) {
        if (labelValues.length != this.labelNames.size()) {
            throw new IllegalArgumentException("Incorrect number of labels.");
        } else {
            this.children.put(Arrays.asList(labelValues), child);
            return (T) this;
        }
    }

    protected abstract Child newChild();

    protected List<MetricFamilySamples> familySamplesList(Type type, List<MetricFamilySamples.Sample> samples) {
        MetricFamilySamples mfs = new MetricFamilySamples(this.fullname, type, this.help, samples);
        List<MetricFamilySamples> mfsList = new ArrayList<>(1);
        mfsList.add(mfs);
        return mfsList;
    }

    protected SimpleStrCollector(Builder<StrMetric.Builder, StrMetric> b) {
        if (b.name.isEmpty()) {
            throw new IllegalStateException("Name hasn't been set.");
        } else {
            String name = b.name;
            if (!b.subsystem.isEmpty()) {
                name = b.subsystem + '_' + name;
            }

            if (!b.namespace.isEmpty()) {
                name = b.namespace + '_' + name;
            }

            this.fullname = name;
            checkMetricName(this.fullname);
            if (b.help.isEmpty()) {
                throw new IllegalStateException("Help hasn't been set.");
            } else {
                this.help = b.help;
                this.labelNames = Arrays.asList(b.labelNames);
                Iterator<String> var3 = this.labelNames.iterator();

                while(var3.hasNext()) {
                    String n = var3.next();
                    checkMetricLabelName(n);
                }

                if (!b.dontInitializeNoLabelsChild) {
                    this.initializeNoLabelsChild();
                }

            }
        }
    }

    public abstract static class Builder<B extends Builder<B, C>, C extends SimpleStrCollector> {
        String namespace = "";
        String subsystem = "";
        String name = "";
        String fullname = "";
        String help = "";
        String[] labelNames = new String[0];
        boolean dontInitializeNoLabelsChild;

        public Builder() {
        }

        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        public B subsystem(String subsystem) {
            this.subsystem = subsystem;
            return (B) this;
        }

        public B namespace(String namespace) {
            this.namespace = namespace;
            return (B) this;
        }

        public B help(String help) {
            this.help = help;
            return (B) this;
        }

        public B labelNames(String... labelNames) {
            this.labelNames = labelNames;
            return (B) this;
        }

        public abstract C create();

        public C register() {
            return this.register(StrCollectorRegistry.defaultRegistry);
        }

        public C register(StrCollectorRegistry registry) {
            C sc = this.create();
            registry.register(sc);
            return sc;
        }
    }
}
