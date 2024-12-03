/*
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.ui.model;

import fr.proline.mzscope.model.ExtractionRequest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author CB205360
 */
public class MzScopePreferences {

  public static String LAST_EXTRACTION_REQUEST = "LastExtractionRequest";

  public static MzScopePreferences getInstance() {
    return instance;
  }

  private static MzScopePreferences instance = new MzScopePreferences();

  private final float mzPPMTolerance = 10.0f;
  private final float fragmentMzPPMTolerance = 50.0f;

  private ExtractionRequest lastExtractionRequest;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private MzScopePreferences() {
  }

  public float getMzPPMTolerance() {
    return (lastExtractionRequest != null && lastExtractionRequest.getMzTolPPM() > 0) ? lastExtractionRequest.getMzTolPPM() : mzPPMTolerance;
  }

  public float getFragmentMzPPMTolerance() {
    return (lastExtractionRequest != null && lastExtractionRequest.getFragmentMzTolPPM() > 0) ? lastExtractionRequest.getFragmentMzTolPPM() : fragmentMzPPMTolerance;
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  public ExtractionRequest getLastExtractionRequest() {
    return lastExtractionRequest;
  }

  public void setLastExtractionRequest(ExtractionRequest lastExtractionRequest) {
    ExtractionRequest oldRequest = this.lastExtractionRequest;
    this.lastExtractionRequest = lastExtractionRequest;
    propertyChangeSupport.firePropertyChange(LAST_EXTRACTION_REQUEST, oldRequest, lastExtractionRequest);
  }
}
