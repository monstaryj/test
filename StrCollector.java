package com.yjq.springbootdemoprometheus.colletor;


import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author monstar
 * @create 2020/10/30 21:18
 */
public abstract class StrCollector{
    public static final double NANOSECONDS_PER_SECOND = 1.0E9D;
    public static final double MILLISECONDS_PER_SECOND = 1000.0D;
    private static final Pattern METRIC_NAME_RE = Pattern.compile("[a-zA-Z_:][a-zA-Z0-9_:]*");
    private static final Pattern METRIC_LABEL_NAME_RE = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern RESERVED_METRIC_LABEL_NAME_RE = Pattern.compile("__.*");
    private static final Pattern SANITIZE_PREFIX_PATTERN = Pattern.compile("^[^a-zA-Z_]");
    private static final Pattern SANITIZE_BODY_PATTERN = Pattern.compile("[^a-zA-Z0-9_]");

    public StrCollector() {
    }


    public abstract List<StrCollector.MetricFamilySamples> collect();

    public <T extends StrCollector> T register() {
        return this.register(StrCollectorRegistry.defaultRegistry);
    }

    public <T extends StrCollector> T register(StrCollectorRegistry registry) {
        registry.register(this);
        return (T) this;
    }

    protected static void checkMetricName(String name) {
        if (!METRIC_NAME_RE.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid metric name: " + name);
        }
    }

    public static String sanitizeMetricName(String metricName) {
        return SANITIZE_BODY_PATTERN.matcher(SANITIZE_PREFIX_PATTERN.matcher(metricName).replaceFirst("_")).replaceAll("_");
    }

    protected static void checkMetricLabelName(String name) {
        if (!METRIC_LABEL_NAME_RE.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid metric label name: " + name);
        } else if (RESERVED_METRIC_LABEL_NAME_RE.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid metric label name, reserved for internal use: " + name);
        }
    }

    public static String doubleToGoString(double d) {
        if (d == 1.0D / 0.0) {
            return "+Inf";
        } else if (d == -1.0D / 0.0) {
            return "-Inf";
        } else {
            return Double.isNaN(d) ? "NaN" : Double.toString(d);
        }
    }

    public interface Describable {
        List<MetricFamilySamples> describe();
    }

    public static class MetricFamilySamples {
        public final String name;
        public final Type type;
        public final String help;
        public final List<MetricFamilySamples.Sample> samples;

        public MetricFamilySamples(String name, Type type, String help, List<MetricFamilySamples.Sample> samples) {
            this.name = name;
            this.type = type;
            this.help = help;
            this.samples = samples;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof MetricFamilySamples)) {
                return false;
            } else {
                MetricFamilySamples other = (MetricFamilySamples)obj;
                return other.name.equals(this.name) && other.type.equals(this.type) && other.help.equals(this.help) && other.samples.equals(this.samples);
            }
        }

        public int hashCode() {
            int hash = 1;
            hash = 37 * hash + this.name.hashCode();
            hash = 37 * hash + this.type.hashCode();
            hash = 37 * hash + this.help.hashCode();
            hash = 37 * hash + this.samples.hashCode();
            return hash;
        }

        public String toString() {
            return "Name: " + this.name + " Type: " + this.type + " Help: " + this.help + " Samples: " + this.samples;
        }

        public static class Sample {
            public final String name;
            public final List<String> labelNames;
            public final List<String> labelValues;
            public final String value;
            public final Long timestampMs;

            public Sample(String name, List<String> labelNames, List<String> labelValues, String value, Long timestampMs) {
                this.name = name;
                this.labelNames = labelNames;
                this.labelValues = labelValues;
                this.value = value;
                this.timestampMs = timestampMs;
            }

            public Sample(String name, List<String> labelNames, List<String> labelValues, String value) {
                this(name, labelNames, labelValues, value, (Long)null);
            }

            public boolean equals(Object obj) {
                if (!(obj instanceof MetricFamilySamples.Sample)) {
                    return false;
                } else {
                    MetricFamilySamples.Sample other = (MetricFamilySamples.Sample)obj;
                    return other.name.equals(this.name) && other.labelNames.equals(this.labelNames) && other.labelValues.equals(this.labelValues) && other.value == this.value && (this.timestampMs == null && other.timestampMs == null || other.timestampMs != null && other.timestampMs.equals(this.timestampMs));
                }
            }

            public int hashCode() {
                int hash = 1;
                hash = 37 * hash + this.name.hashCode();
                hash = 37 * hash + this.labelNames.hashCode();
                hash = 37 * hash + this.labelValues.hashCode();
                long d = Double.doubleToLongBits(Double.parseDouble(this.value));
                hash = 37 * hash + (int)(d ^ d >>> 32);
                if (this.timestampMs != null) {
                    hash = 37 * hash + this.timestampMs.hashCode();
                }

                return hash;
            }

            public String toString() {
                return "Name: " + this.name + " LabelNames: " + this.labelNames + " labelValues: " + this.labelValues + " Value: " + this.value + " TimestampMs: " + this.timestampMs;
            }
        }
    }

    public static enum Type {
        Str;

        private Type() {
        }
    }
}
