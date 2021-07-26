package com.aslan.kafka.serializer;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class AvroSerializerBad <T extends SpecificRecordBase> implements Serializer<T> {
	private Logger logger = LoggerFactory.getLogger(AvroSerializerBad.class.getName());
	
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	    // do nothing
	}
	
	@Override
	public byte[] serialize(String topic, T payload) {
		byte[] bytes = null;
	    try {
	      if (payload != null) {
	          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	          BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
	          DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(payload.getSchema());
	          datumWriter.write(payload, binaryEncoder);
	          binaryEncoder.flush();
	          byteArrayOutputStream.close();
	          bytes = byteArrayOutputStream.toByteArray();
	          logger.info("AvroSerializer::serialize(): serialized payload='{}'", DatatypeConverter.printHexBinary(bytes));
	      }
	    } catch (Exception ex) {
	    	logger.error("AvroSerializer::serialize(): Unable to serialize payload {}", ex);
	    }
	    return bytes;
	}

	@Override
	public void close() {
	   // do nothing
	}

}
