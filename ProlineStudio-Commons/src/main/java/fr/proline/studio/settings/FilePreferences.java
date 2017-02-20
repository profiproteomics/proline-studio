package fr.proline.studio.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences implementation that stores to a user-defined file.
 *
 * @author Initial version : David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferences.java 283 2009-06-18 17:06:58Z david $
 */
public class FilePreferences extends AbstractPreferences {
  
  private static final Logger log = LoggerFactory.getLogger("ProlineStudio.Commons");
  
  private Map<String, String> root;
  private Map<String, FilePreferences> children;
  private boolean isRemoved = false;
 
  private File m_file = null;
  
  public FilePreferences(File f, AbstractPreferences parent, String name)
  {
    super(parent, name);
 
    m_file = f;
    
    root = new TreeMap<>();
    children = new TreeMap<>();
 
    try {
      sync();
    }
    catch (BackingStoreException e) {
      log.error("Unable to sync on creation of node " + name, e);
    }
  }
 
    @Override
    public void put(String key, String value) {
        int size = value.length();
        if (size > Preferences.MAX_VALUE_LENGTH) {
            int cnt = 1;
            for (int idx = 0; idx < size; cnt++) {
                if ((size - idx) > Preferences.MAX_VALUE_LENGTH) {
                    super.put(key + "." + cnt, value.substring(idx, idx + Preferences.MAX_VALUE_LENGTH));
                    idx += Preferences.MAX_VALUE_LENGTH;
                } else {
                    super.put(key + "." + cnt, value.substring(idx));
                    idx = size;
                }
            }
        } else {
            super.put(key, value);
        }
    }
    
    public String get(String key, String def) {
        String value = super.get(key, null);
        if (value != null) {
            return value;
        }
        
        int cnt = 1;
        StringBuilder sb = new StringBuilder();
        while(true) {
            String s = super.get(key+"."+cnt, null);
            if (s == null) {
                break;
            }
            
            sb.append(s);
            
            cnt++;
        }
        
        if (sb.length()>0) {
            value = sb.toString();
        } else {
            value = def;
        }
        
        return value;
    }
  
  
    @Override
  protected void putSpi(String key, String value)
  {
    root.put(key, value);
    try {
      flush();
    }
    catch (BackingStoreException e) {
      log.error("Unable to flush after putting " + key, e);
    }
  }
 
    @Override
  protected String getSpi(String key)
  {
    return root.get(key);
  }
 
    @Override
  protected void removeSpi(String key)
  {
    root.remove(key);
    try {
      flush();
    }
    catch (BackingStoreException e) {
      log.error("Unable to flush after removing " + key, e);
    }
  }
 
    @Override
  protected void removeNodeSpi() throws BackingStoreException
  {
    isRemoved = true;
    flush();
  }
 
    @Override
  protected String[] keysSpi() throws BackingStoreException
  {
    return root.keySet().toArray(new String[root.keySet().size()]);
  }
 
    @Override
  protected String[] childrenNamesSpi() throws BackingStoreException
  {
    return children.keySet().toArray(new String[children.keySet().size()]);
  }
 
    @Override
  protected FilePreferences childSpi(String name)
  {
    FilePreferences child = children.get(name);
    if (child == null || child.isRemoved()) {
      child = new FilePreferences(m_file, this, name);
      children.put(name, child);
    }
    return child;
  }
 
 
    @Override
  protected void syncSpi() throws BackingStoreException
  {
    if (isRemoved()) return;
 

    if (!m_file.exists()) return;
 
    synchronized (m_file) {
      Properties p = new Properties();
      try {
        p.load(new FileInputStream(m_file));
 
        StringBuilder sb = new StringBuilder();
        getPath(sb);
        String path = sb.toString();
 
        final Enumeration<?> pnen = p.propertyNames();
        while (pnen.hasMoreElements()) {
          String propKey = (String) pnen.nextElement();
          if (propKey.startsWith(path)) {
            String subKey = propKey.substring(path.length());
            root.put(subKey, p.getProperty(propKey));
          }
        }
      }
      catch (IOException e) {
        throw new BackingStoreException(e);
      }
    }
  }
 
  private void getPath(StringBuilder sb)
  {
    final FilePreferences parent = (FilePreferences) parent();
    if (parent == null) return;
 
    parent.getPath(sb);
    sb.append(name()).append('.');
  }
 
  @Override
  protected void flushSpi() throws BackingStoreException
  {

    synchronized (m_file) {
      Properties p = new Properties();
      try {
 
        StringBuilder sb = new StringBuilder();
        getPath(sb);
        String path = sb.toString();
 
        if (m_file.exists()) {
          p.load(new FileInputStream(m_file));
 
          List<String> toRemove = new ArrayList<>();
 
          // Make a list of all direct children of this node to be removed
          final Enumeration<?> pnen = p.propertyNames();
          while (pnen.hasMoreElements()) {
            String propKey = (String) pnen.nextElement();
            if (propKey.startsWith(path)) {
              String subKey = propKey.substring(path.length());
              // Only do immediate descendants
              if (subKey.indexOf('.') == -1) {
                toRemove.add(propKey);
              }
            }
          }
 
          // Remove them now that the enumeration is done with
          for (String propKey : toRemove) {
            p.remove(propKey);
          }
        }
 
        // If this node hasn't been removed, add back in any values
        if (!isRemoved) {
          for (String s : root.keySet()) {
            p.setProperty(path + s, root.get(s));
          }
        }
 
        p.store(new FileOutputStream(m_file), "FilePreferences");
      }
      catch (IOException e) {
        throw new BackingStoreException(e);
      }
    }
  }
}