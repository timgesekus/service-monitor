package de.dfs.servicemonitor.etcd.responsemodel;

import java.util.Optional;

public class Response
{
  public String action;
  public Node node = new Node();
  public Optional< Node > prevNode = Optional.empty();
}
