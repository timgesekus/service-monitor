package de.dfs.servicemonitor.etcd.responsemodel;

import java.io.IOException;

import org.zapodot.jackson.java8.JavaOptionalModule;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Converter
{
  private ObjectMapper objectMapper;


  public Converter()
  {
    objectMapper = new ObjectMapper().registerModule(new JavaOptionalModule());
  }


  public Response fromJson(String json) throws JsonParseException, JsonMappingException, IOException
  {
    return objectMapper.readValue(json, Response.class);
  }


  public String toJson(Response response) throws JsonProcessingException
  {
    return objectMapper.writeValueAsString(response);
  }
}
