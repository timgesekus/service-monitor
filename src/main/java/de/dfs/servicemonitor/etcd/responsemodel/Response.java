package de.dfs.servicemonitor.etcd.responsemodel;

import java.util.Optional;

public class Response
{
  public String action;
  public Node node;
  public Optional< Node > prevNode = Optional.empty();
}
