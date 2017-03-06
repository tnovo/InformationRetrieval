package pt.ua.ri.index;

import com.google.common.base.Preconditions;

import java.text.DecimalFormat;
import java.util.Optional;

public abstract class AbstractDocumentInfo implements DocumentInfo {

    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.########");
    protected static final String SEPARATOR = ":";
    private final int docId;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") private Optional<Double> normalizedDocumentFrequency;

    protected AbstractDocumentInfo(final int docId) {
        this.docId = docId;
        normalizedDocumentFrequency = Optional.empty();
    }

    protected AbstractDocumentInfo(final int docId, final double normalizedDocumentFrequency) {
        this.docId = docId;
        setNormalizedDocumentFrequency(normalizedDocumentFrequency);
    }

    @Override public int getDocId() {
        return docId;
    }

    @Override public double getNormalizedDocumentFrequency() {
        return normalizedDocumentFrequency.orElse(0.0d);
    }

    @Override public void setDocumentLength(float documentLength) {
        setNormalizedDocumentFrequency(getLogTermFrequency() / documentLength);
    }

    private void setNormalizedDocumentFrequency(double normalizedDocumentFrequency) {
        Preconditions.checkArgument(normalizedDocumentFrequency >= 0.0d && normalizedDocumentFrequency <= 1.0d);
        this.normalizedDocumentFrequency = Optional.of(normalizedDocumentFrequency);
    }

    public double getLogTermFrequency() {
        int tf = getTermFrequency();
        return tf > 0 ? 1.0d + Math.log(tf) : 0.0d;
    }

    @Override public String toString() {
        return normalizedDocumentFrequency.map(df -> DECIMAL_FORMAT.format(df) + SEPARATOR).orElse("");
    }
}
