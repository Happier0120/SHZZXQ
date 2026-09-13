package com.example.propertysupervision.protocol.model;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public final class ProtocolSegment {
    /**
     * 使用TreeMap的原因：
     *
     * 协议要求Field必须按照编号从小到大进行打包。
     */
    private final SortedMap<Integer, String> fields = new TreeMap<Integer, String>();

    public ProtocolSegment addField(int fieldNo, String value) {
        if (fieldNo < 1 || fieldNo > 127) {
            throw new IllegalArgumentException("非法Field编号: " + fieldNo);
        }

        if (value == null) {
            throw new IllegalArgumentException("Field " + fieldNo + " 的值不能为 null");
        }

        if (fields.containsKey(fieldNo)) {
            throw new IllegalArgumentException("Field " + fieldNo + " 重复添加");
        }

        fields.put(fieldNo, value);
        return this;
    }

    /**
     * 获取所有Field。
     *
     * 返回只读Map，防止外部直接修改内部数据。
     */
    public SortedMap<Integer, String> getFields() {
        return Collections.unmodifiableSortedMap(fields);
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }


}
