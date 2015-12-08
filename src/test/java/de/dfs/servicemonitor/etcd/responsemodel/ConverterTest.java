package de.dfs.servicemonitor.etcd.responsemodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ConverterTest
{

  @Test
  public void convertToJsonMisingPrevNode () throws JsonParseException, JsonMappingException, IOException {
    String etcdResponse = "{\n" + 
        "    \"action\": \"get\",\n" + 
        "    \"node\": {\n" + 
        "        \"createdIndex\": 2,\n" + 
        "        \"key\": \"/message\",\n" + 
        "        \"modifiedIndex\": 2,\n" + 
        "        \"value\": \"Hello world\"\n" + 
        "    }\n" + 
        "}";
    Converter converter = new Converter();
    Response response = converter.fromJson(etcdResponse);
    assertThat(response.action, equalTo("get"));
    assertThat(response.node.createdIndex, equalTo(2));
    assertThat(response.node.key, equalTo("/message"));
    assertThat(response.node.modifiedIndex, equalTo(2));
    assertThat(response.node.value, equalTo("Hello world"));
    assertThat(response.prevNode, equalTo(Optional.empty()));
  }
  
  @Test
  public void convertToJson () throws JsonParseException, JsonMappingException, IOException {
    String etcdResponse = "{\n" + 
        "    \"action\": \"set\",\n" + 
        "    \"node\": {\n" + 
        "        \"createdIndex\": 3,\n" + 
        "        \"key\": \"/message\",\n" + 
        "        \"modifiedIndex\": 3,\n" + 
        "        \"value\": \"Hello etcd\"\n" + 
        "    },\n" + 
        "    \"prevNode\": {\n" + 
        "        \"createdIndex\": 2,\n" + 
        "        \"key\": \"/message\",\n" + 
        "        \"value\": \"Hello world\",\n" + 
        "        \"modifiedIndex\": 2\n" + 
        "    }\n" + 
        "}";
      
    Converter converter = new Converter();
    Response response = converter.fromJson(etcdResponse);
    assertThat(response.action, equalTo("set"));
    assertThat(response.node.createdIndex, equalTo(3));
    assertThat(response.node.key, equalTo("/message"));
    assertThat(response.node.modifiedIndex, equalTo(3));
    assertThat(response.node.value, equalTo("Hello etcd"));
    
    Node exprectedPrevNode = new Node();
    exprectedPrevNode.createdIndex = 2;
    exprectedPrevNode.key="/message";
    exprectedPrevNode.value="Hello world";
    exprectedPrevNode.modifiedIndex=2;
    assertTrue(response.prevNode.isPresent());
    assertThat(Optional.of(exprectedPrevNode), equalTo(response.prevNode));
  }
}
