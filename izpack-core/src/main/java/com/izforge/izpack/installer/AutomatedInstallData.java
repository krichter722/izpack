/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.installer;

import com.izforge.izpack.Info;
import com.izforge.izpack.data.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.rules.RulesEngine;

import java.io.Serializable;
import java.util.*;
import java.util.zip.ZipOutputStream;

/**
 * Encloses information about the install process. This implementation is not thread safe.
 *
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstallData implements Serializable {

    // --- Static members -------------------------------------------------
    public static final String MODIFY_INSTALLATION = "modify.izpack.install";
    public static final String INSTALLATION_INFORMATION = ".installationinformation";

    /**
     * Names of the custom actions types with which they are stored in the installer jar file. These
     * names are also used to identify the type of custom action in the customData map. Slashes as
     * first char are needed to use the names as "file" name in the installer jar.
     */
    // Attention !! Do not change the existent names and the order.
    // Add a / as first char at new types. Add new type handling in
    // Unpacker.
    static final String[] CUSTOM_ACTION_TYPES = new String[]{"/installerListeners",
            "/uninstallerListeners", "/uninstallerLibs", "/uninstallerJars"};

    public static final int INSTALLER_LISTENER_INDEX = 0;

    public static final int UNINSTALLER_LISTENER_INDEX = 1;

    public static final int UNINSTALLER_LIBS_INDEX = 2;

    public static final int UNINSTALLER_JARS_INDEX = 3;

    // --- Instance members -----------------------------------------------

    private RulesEngine rules;

    /**
     * The language code.
     */
    private String localeISO3;

    /**
     * The used locale.
     */
    private Locale locale;

    /**
     * The language pack.
     */
    private LocaleDatabase langpack;

    /**
     * The uninstaller jar stream.
     */
    private ZipOutputStream uninstallOutJar;

    /**
     * The inforamtions.
     */
    private Info info;

    /**
     * The complete list of packs.
     */
    private List<Pack> allPacks;

    /**
     * The available packs.
     */
    private List<Pack> availablePacks;

    /**
     * The selected packs.
     */
    private List<Pack> selectedPacks;

    /**
     * The panels list.
     */
    private List<IzPanel> panels;

    /**
     * The panels order.
     */
    private List<Panel> panelsOrder;

    /**
     * The current panel.
     */
    private int curPanelNumber;

    /**
     * Can we close the installer ?
     */
    private boolean canClose = false;

    /**
     * Did the installation succeed ?
     */
    private boolean installSuccess = true;

    /**
     * Is a reboot necessary to complete the installation ?
     */
    private boolean rebootNecessary = false;

    /**
     * The xmlData for automated installers.
     */
    private IXMLElement xmlData;

    /**
     * Custom data.
     */
    private Map<String, List> customData;

    /**
     * Maps the variable names to their values
     */
    private Properties variables;

    /**
     * The attributes used by the panels
     */
    private Map<String, Object> attributes;

    /**
     * This class should be a singleton. Therefore
     * the one possible object will be stored in this
     * static member.
     */
    private static AutomatedInstallData self = null;

    /**
     * Returns the one possible object of this class.
     *
     * @return the one possible object of this class
     */
    public static AutomatedInstallData getInstance() {
        return (self);
    }

    /**
     * Constructs a new instance of this class.
     * Only one should be possible, at a scound call a RuntimeException
     * will be raised.
     */
    public AutomatedInstallData() {
        setAvailablePacks(new ArrayList<Pack>());
        setSelectedPacks(new ArrayList());
        setPanels(new ArrayList<IzPanel>());
        setPanelsOrder(new ArrayList<Panel>());
        setXmlData(new XMLElementImpl("AutomatedInstallation"));
        setVariables(new Properties());
        setAttributes(new HashMap<String, Object>());
        setCustomData(new HashMap<String, List>());
        if (self != null) {
            throw new RuntimeException("Panic!! second call of the InstallData Ctor!!");
        }
        self = this;
    }

    /**
     * Returns the map of variable values. Modifying this will directly affect the current value of
     * variables.
     *
     * @return the map of variable values
     */
    public Properties getVariables() {
        return variables;
    }

    /**
     * Sets a variable to the specified value. This is short hand for
     * <code>getVariables().setProperty(var, val)</code>.
     *
     * @param var the name of the variable
     * @param val the new value of the variable
     * @see #getVariable
     */
    public void setVariable(String var, String val) {
        getVariables().setProperty(var, val);
    }

    /**
     * Returns the current value of the specified variable. This is short hand for
     * <code>getVariables().getProperty(var)</code>.
     *
     * @param var the name of the variable
     * @return the value of the variable or null if not set
     * @see #setVariable
     */
    public String getVariable(String var) {
        return getVariables().getProperty(var);
    }

    /**
     * Sets the install path.
     *
     * @param path the new install path
     * @see #getInstallPath
     */
    public void setInstallPath(String path) {
        setVariable(ScriptParser.INSTALL_PATH, path);
    }

    /**
     * Returns the install path.
     *
     * @return the current install path or null if none set yet
     * @see #setInstallPath
     */
    public String getInstallPath() {
        return getVariable(ScriptParser.INSTALL_PATH);
    }

    /**
     * Returns the value of the named attribute.
     *
     * @param attr the name of the attribute
     * @return the value of the attribute or null if not set
     * @see #setAttribute
     */
    public Object getAttribute(String attr) {
        return getAttributes().get(attr);
    }

    /**
     * Sets a named attribute. The panels and other IzPack components can attach custom attributes
     * to InstallData to communicate with each other. For example, a set of co-operating custom
     * panels do not need to implement a common data storage but can use InstallData singleton. The
     * name of the attribute should include the package and class name to prevent name space
     * collisions.
     *
     * @param attr the name of the attribute to set
     * @param val  the value of the attribute or null to unset the attribute
     * @see #getAttribute
     */
    public void setAttribute(String attr, Object val) {
        if (val == null) {
            getAttributes().remove(attr);
        } else {
            getAttributes().put(attr, val);
        }

    }


    public RulesEngine getRules() {
        return rules;
    }


    public void setRules(RulesEngine rules) {
        this.rules = rules;
    }

    public String getLocaleISO3() {
        return localeISO3;
    }

    public void setLocaleISO3(String localeISO3) {
        this.localeISO3 = localeISO3;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public LocaleDatabase getLangpack() {
        return langpack;
    }

    public void setLangpack(LocaleDatabase langpack) {
        this.langpack = langpack;
    }

    public ZipOutputStream getUninstallOutJar() {
        return uninstallOutJar;
    }

    public void setUninstallOutJar(ZipOutputStream uninstallOutJar) {
        this.uninstallOutJar = uninstallOutJar;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public List<Pack> getAllPacks() {
        return allPacks;
    }

    public void setAllPacks(List<Pack> allPacks) {
        this.allPacks = allPacks;
    }

    public List<Pack> getAvailablePacks() {
        return availablePacks;
    }

    public void setAvailablePacks(List<Pack> availablePacks) {
        this.availablePacks = availablePacks;
    }

    public List<Pack> getSelectedPacks() {
        return selectedPacks;
    }

    public void setSelectedPacks(List<Pack> selectedPacks) {
        this.selectedPacks = selectedPacks;
    }

    public List<IzPanel> getPanels() {
        return panels;
    }

    public void setPanels(List<IzPanel> panels) {
        this.panels = panels;
    }

    public List<Panel> getPanelsOrder() {
        return panelsOrder;
    }

    public void setPanelsOrder(List<Panel> panelsOrder) {
        this.panelsOrder = panelsOrder;
    }

    public int getCurPanelNumber() {
        return curPanelNumber;
    }

    public void setCurPanelNumber(int curPanelNumber) {
        this.curPanelNumber = curPanelNumber;
    }

    public boolean isCanClose() {
        return canClose;
    }

    public void setCanClose(boolean canClose) {
        this.canClose = canClose;
    }

    public boolean isInstallSuccess() {
        return installSuccess;
    }

    public void setInstallSuccess(boolean installSuccess) {
        this.installSuccess = installSuccess;
    }

    public boolean isRebootNecessary() {
        return rebootNecessary;
    }

    public void setRebootNecessary(boolean rebootNecessary) {
        this.rebootNecessary = rebootNecessary;
    }

    public IXMLElement getXmlData() {
        return xmlData;
    }

    public void setXmlData(IXMLElement xmlData) {
        this.xmlData = xmlData;
    }

    public Map<String, List> getCustomData() {
        return customData;
    }

    public void setCustomData(Map<String, List> customData) {
        this.customData = customData;
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
