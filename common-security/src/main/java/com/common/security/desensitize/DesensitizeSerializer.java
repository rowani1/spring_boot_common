package com.common.security.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 脱敏序列化器
 */
public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private DesensitizeType type;
    private int prefixLen;
    private int suffixLen;
    private char maskChar;

    public DesensitizeSerializer() {
    }

    public DesensitizeSerializer(DesensitizeType type, int prefixLen, int suffixLen, char maskChar) {
        this.type = type;
        this.prefixLen = prefixLen;
        this.suffixLen = suffixLen;
        this.maskChar = maskChar;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        String masked = desensitize(value);
        gen.writeString(masked);
    }

    private String desensitize(String value) {
        if (type == null) {
            return value;
        }

        switch (type) {
            case MOBILE:
                return DesensitizeStrategy.mobile(value);
            case ID_CARD:
                return DesensitizeStrategy.idCard(value);
            case BANK_CARD:
                return DesensitizeStrategy.bankCard(value);
            case EMAIL:
                return DesensitizeStrategy.email(value);
            case NAME:
                return DesensitizeStrategy.name(value);
            case ADDRESS:
                return DesensitizeStrategy.address(value);
            case PASSWORD:
                return DesensitizeStrategy.password(value);
            case CUSTOM:
                return DesensitizeStrategy.custom(value, prefixLen, suffixLen, maskChar);
            default:
                return value;
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return this;
        }

        Desensitize annotation = property.getAnnotation(Desensitize.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(Desensitize.class);
        }

        if (annotation != null) {
            return new DesensitizeSerializer(
                    annotation.type(),
                    annotation.prefixLen(),
                    annotation.suffixLen(),
                    annotation.maskChar()
            );
        }

        return this;
    }
}
