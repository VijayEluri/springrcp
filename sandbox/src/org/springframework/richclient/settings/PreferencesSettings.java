/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.richclient.settings;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Settings implementation using the J2SE Preferences API. <br>
 * Not using the PreferenceChangeListener to implement PropertyChangeListener
 * support, because we also need the old value.
 * 
 * @author Peter De Bruycker
 */
public class PreferencesSettings extends AbstractSettings {

    private Preferences prefs;

    private Map children = new HashMap();

    /**
     * Create the root.
     */
    public PreferencesSettings(String name) {
        this(null, name);
    }

    /**
     * Create a child with the given name.
     */
    public PreferencesSettings(PreferencesSettings parent, String name) {
        super(parent, name);
        if (parent != null) {
            prefs = parent.getPreferences().node(name);
        } else {
            prefs = Preferences.userRoot().node(name);
        }
    }

    /**
     * @param preferences
     */
    public PreferencesSettings(Preferences preferences) {
        super(null, preferences.name());
        this.prefs = preferences;
    }

    public Preferences getPreferences() {
        return prefs;
    }

    public String getString(String key) {
        return prefs.get(key, getDefaultString(key));
    }

    public void save() throws IOException {
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            IOException ioe = new IOException("Unable to save settings");
            ioe.initCause(e);
            throw ioe;
        }
    }

    public void load() throws IOException {
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            IOException ioe = new IOException("Unable to save settings");
            ioe.initCause(e);
            throw ioe;
        }
    }

    protected String internalGet(String key) {
        if (prefs.get(key, "").equals("")) {
            return null;
        }
        return prefs.get(key, "");
    }

    public Settings getSettings(String name) {
        Settings result = (Settings) children.get(name);
        if (result == null) {
            result = new PreferencesSettings(this, name);
            children.put(name, result);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.richclient.settings.AbstractSettings#internalSet(java.lang.String,
     *      java.lang.String)
     */
    protected void internalSet(String key, String value) {
        prefs.put(key, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.richclient.settings.AbstractSettings#getKeys()
     */
    public String[] getKeys() {
        try {
            return prefs.keys();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.richclient.settings.AbstractSettings#internalRemove(java.lang.String)
     */
    protected void internalRemove(String key) {
        prefs.remove(key);
    }

    public boolean internalContains(String key) {
        try {
            return Arrays.asList(prefs.keys()).contains(key);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
}