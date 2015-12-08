package de.dfs.servicemonitor.etcd.responsemodel;

public class Node
{
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + createdIndex;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + modifiedIndex;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Node other = (Node)obj;
    if (createdIndex != other.createdIndex)
      return false;
    if (key == null)
    {
      if (other.key != null)
        return false;
    }
    else if ( ! key.equals(other.key))
      return false;
    if (modifiedIndex != other.modifiedIndex)
      return false;
    if (value == null)
    {
      if (other.value != null)
        return false;
    }
    else if ( ! value.equals(other.value))
      return false;
    return true;
  }
  public int createdIndex;
  public String key;
  public int modifiedIndex;
  public String value;
}
