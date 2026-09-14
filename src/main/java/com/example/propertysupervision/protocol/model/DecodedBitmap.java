package com.example.propertysupervision.protocol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DecodedBitmap {
    private final List<Integer> fieldNos;
    private final boolean hasNext;

    public DecodedBitmap(List<Integer> fieldNos, boolean hasNext) {
        if (fieldNos == null) {
            throw new IllegalArgumentException("Field 编号集合不能为空");
        }

        this.fieldNos = Collections.unmodifiableList(new ArrayList<>(fieldNos));
        this.hasNext = hasNext;
    }

    public List<Integer> getFieldNos() {
        return fieldNos;
    }

    public boolean hasNext() {
        return hasNext;
    }
}
