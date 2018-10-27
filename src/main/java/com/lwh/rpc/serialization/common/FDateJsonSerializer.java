package com.lwh.rpc.serialization.common;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 用来定制java.util.Date类进行序列化格式的定制化输出
 */
public class FDateJsonSerializer extends JsonSerializer<Date> {

    @Override
    public void serialize(Date value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException, JsonProcessingException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        gen.writeString(value != null ? sdf.format(value) : "null");
    }
}
