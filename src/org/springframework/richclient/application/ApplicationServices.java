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
package org.springframework.richclient.application;

import java.awt.Image;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.Icon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.FormPropertyFaceDescriptorSource;
import org.springframework.binding.form.support.MessageSourceFormPropertyFaceDescriptorSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.richclient.application.config.ApplicationObjectConfigurer;
import org.springframework.richclient.application.config.DefaultApplicationObjectConfigurer;
import org.springframework.richclient.application.support.BeanFactoryViewDescriptorRegistry;
import org.springframework.richclient.application.support.DefaultPropertyEditorRegistry;
import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.config.CommandConfigurer;
import org.springframework.richclient.command.config.DefaultCommandConfigurer;
import org.springframework.richclient.factory.ComponentFactory;
import org.springframework.richclient.factory.DefaultComponentFactory;
import org.springframework.richclient.form.binding.BinderSelectionStrategy;
import org.springframework.richclient.form.binding.swing.SwingBinderSelectionStrategy;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.FormComponentInterceptorFactory;
import org.springframework.richclient.image.AwtImageResource;
import org.springframework.richclient.image.DefaultIconSource;
import org.springframework.richclient.image.DefaultImageSource;
import org.springframework.richclient.image.IconSource;
import org.springframework.richclient.image.ImageSource;
import org.springframework.rules.RulesSource;
import org.springframework.rules.support.DefaultRulesSource;

/**
 * A singleton service locator for common rich client application services.
 * 
 * @author Keith Donald
 */
public class ApplicationServices implements ApplicationObjectConfigurer, CommandConfigurer, ResourceLoader,
        MessageSource, ImageSource, IconSource, FormComponentInterceptorFactory, ApplicationContextAware {

    public static final String IMAGE_SOURCE_BEAN_ID = "imageSource";

    public static final String ICON_SOURCE_BEAN_ID = "iconSource";

    public static final String COMPONENT_FACTORY_BEAN_ID = "componentFactory";

    public static final String RULES_SOURCE_BEAN_ID = "rulesSource";

    public static final String PROPERTY_EDITOR_REGISTRY_BEAN_ID = "propertyEditorRegistry";

    public static final String OBJECT_CONFIGURER_BEAN_ID = "applicationObjectConfigurer";

    public static final String COMMAND_CONFIGURER_BEAN_ID = "commandConfigurer";

    public static final String LOOK_AND_FEEL_CONFIGURER_BEAN_ID = "lookAndFeelConfigurer";

    public static final String FORM_INTERCEPTOR_FACTORY_BEAN_ID = "formComponentInterceptorFactory";

    private static final String FORM_PROPERTY_FACE_DESCRIPTOR_SOURCE_BEAN_ID = "formPropertyFaceDescriptorSource";
    
    private static final String BINDER_SELECTION_STRATEGY_BEAN_ID = "binderSelectionStrategy";

    private final Log logger = LogFactory.getLog(getClass());

    private ComponentFactory componentFactory;

    private FormComponentInterceptorFactory formComponentInterceptorFactory;

    private ApplicationObjectConfigurer objectConfigurer;

    private CommandConfigurer commandConfigurer;

    private ImageSource imageSource;

    private IconSource iconSource;

    private ViewDescriptorRegistry viewRegistry;

    private RulesSource rulesSource;

    private PropertyEditorRegistry propertyEditorRegistry;

    private boolean lazyInit = true;

    private ApplicationContext applicationContext;

    private MessageSourceAccessor messageSourceAccessor;

    private FormPropertyFaceDescriptorSource formPropertyFaceDescriptorSource;

    private BinderSelectionStrategy binderSelectionStrategy;

    

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        if (this.applicationContext != null) {
            this.messageSourceAccessor = new MessageSourceAccessor(this.applicationContext);
        }
    }

    public ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = new StaticApplicationContext();
            messageSourceAccessor = new MessageSourceAccessor(applicationContext);
        }
        return applicationContext;
    }

    public ComponentFactory getComponentFactory() {
        if (componentFactory == null) {
            initComponentFactory();
        }
        return componentFactory;
    }

    private void initComponentFactory() {
        try {
            this.componentFactory = (ComponentFactory)getApplicationContext().getBean(COMPONENT_FACTORY_BEAN_ID,
                    ComponentFactory.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No component factory bean found under name " + COMPONENT_FACTORY_BEAN_ID
                    + "; creating using existing image source.");
            this.componentFactory = new DefaultComponentFactory();
        }
        catch (IllegalArgumentException e) {
            this.componentFactory = new DefaultComponentFactory();
        }
    }

    public void setComponentFactory(ComponentFactory factory) {
        this.componentFactory = factory;
    }

    public FormComponentInterceptorFactory getFormComponentInterceptorFactory() {
        if (formComponentInterceptorFactory == null) {
            initFormComponentInterceptorFactory();
        }
        return formComponentInterceptorFactory;
    }

    private void initFormComponentInterceptorFactory() {
        try {
            this.formComponentInterceptorFactory = (FormComponentInterceptorFactory)getApplicationContext().getBean(
                    FORM_INTERCEPTOR_FACTORY_BEAN_ID, FormComponentInterceptorFactory.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No bean named " + FORM_INTERCEPTOR_FACTORY_BEAN_ID + " found. Using empty interceptor.");
            this.formComponentInterceptorFactory = new FormComponentInterceptorFactory() {
                public FormComponentInterceptor getInterceptor(FormModel formModel) {
                    return null;
                }
            };
        }
    }

    public ApplicationObjectConfigurer getObjectConfigurer() {
        if (objectConfigurer == null) {
            initObjectConfigurer();
        }
        return objectConfigurer;
    }

    private void initObjectConfigurer() {
        try {
            this.objectConfigurer = (ApplicationObjectConfigurer)getApplicationContext().getBean(
                    OBJECT_CONFIGURER_BEAN_ID, ApplicationObjectConfigurer.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No object configurer found in context under name '" + OBJECT_CONFIGURER_BEAN_ID
                    + "'; configuring defaults.");
            this.objectConfigurer = new DefaultApplicationObjectConfigurer(getApplicationContext(), getImageSource(),
                    getIconSource());
        }
    }

    public void setObjectConfigurer(ApplicationObjectConfigurer objectConfigurer) {
        this.objectConfigurer = objectConfigurer;
    }

    public CommandConfigurer getCommandConfigurer() {
        if (commandConfigurer == null) {
            initCommandConfigurer();
        }
        return commandConfigurer;
    }

    private void initCommandConfigurer() {
        try {
            this.commandConfigurer = (CommandConfigurer)getApplicationContext().getBean(COMMAND_CONFIGURER_BEAN_ID,
                    CommandConfigurer.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No command configurer found in context under name '" + COMMAND_CONFIGURER_BEAN_ID
                    + "'; configuring defaults.");
            this.commandConfigurer = new DefaultCommandConfigurer();
        }
    }

    public void setCommandConfigurer(CommandConfigurer commandConfigurer) {
        this.commandConfigurer = commandConfigurer;
    }

    public ImageSource getImageSource() {
        if (imageSource == null) {
            initImageSource();
        }
        return imageSource;
    }

    private void initImageSource() {
        try {
            this.imageSource = (ImageSource)getApplicationContext().getBean(IMAGE_SOURCE_BEAN_ID, ImageSource.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No image source bean found in context under name '" + IMAGE_SOURCE_BEAN_ID
                    + "'; configuring defaults.");
            this.imageSource = new DefaultImageSource(new HashMap());
        }
    }

    public void setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    public IconSource getIconSource() {
        if (iconSource == null) {
            initIconSource();
        }
        return iconSource;
    }

    public void setIconSource(IconSource iconSource) {
        this.iconSource = iconSource;
    }

    private void initIconSource() {
        try {
            this.iconSource = (IconSource)getApplicationContext().getBean(ICON_SOURCE_BEAN_ID, IconSource.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No icon source bean found under name " + ICON_SOURCE_BEAN_ID
                    + "; creating using existing image source.");
            this.iconSource = new DefaultIconSource(getImageSource());
        }
    }

    public ViewDescriptorRegistry getViewDescriptorRegistry() {
        if (viewRegistry == null) {
            initViewRegistry();
        }
        return viewRegistry;
    }

    private void initViewRegistry() {
        BeanFactoryViewDescriptorRegistry r = new BeanFactoryViewDescriptorRegistry();
        r.setApplicationContext(getApplicationContext());
        this.viewRegistry = r;
    }

    public void setViewRegistry(ViewDescriptorRegistry registry) {
        this.viewRegistry = registry;
    }

    public RulesSource getRulesSource() {
        if (rulesSource == null) {
            initRulesSource();
        }
        return rulesSource;
    }

    private void initRulesSource() {
        try {
            this.rulesSource = (RulesSource)getApplicationContext().getBean(RULES_SOURCE_BEAN_ID, RulesSource.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No rule source found in context under name '" + RULES_SOURCE_BEAN_ID
                    + "'; configuring defaults.");
            this.rulesSource = new DefaultRulesSource();
        }
        catch (IllegalArgumentException e) {
            this.rulesSource = new DefaultRulesSource();
        }
    }

    public void setRulesSource(RulesSource rulesSource) {
        this.rulesSource = rulesSource;
    }

    public PropertyEditorRegistry getPropertyEditorRegistry() {
        if (propertyEditorRegistry == null) {
            initPropertyEditorRegistry();
        }
        return propertyEditorRegistry;
    }

    private void initPropertyEditorRegistry() {
        try {
            this.propertyEditorRegistry = (PropertyEditorRegistry)getApplicationContext().getBean(
                    PROPERTY_EDITOR_REGISTRY_BEAN_ID, PropertyEditorRegistry.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No rule source found in context under name '" + PROPERTY_EDITOR_REGISTRY_BEAN_ID
                    + "'; configuring defaults.");
            this.propertyEditorRegistry = new DefaultPropertyEditorRegistry();
        }
    }

    public void setPropertyEditorRegistry(PropertyEditorRegistry preReg) {
        this.propertyEditorRegistry = preReg;
    }
    
    public BinderSelectionStrategy getBinderSelectionStrategy() {
        if (binderSelectionStrategy == null) {
            initBinderSelectionStrategy();
        }
        return binderSelectionStrategy;
    }
    
    private void initBinderSelectionStrategy() {
        try {
            this.binderSelectionStrategy = (BinderSelectionStrategy)getApplicationContext().getBean(
                    BINDER_SELECTION_STRATEGY_BEAN_ID, BinderSelectionStrategy.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No bean named " + BINDER_SELECTION_STRATEGY_BEAN_ID
                    + " found; configuring defaults.");
            this.binderSelectionStrategy = new SwingBinderSelectionStrategy();
        }
    }
    
    public void setBinderSelectionStrategy(BinderSelectionStrategy binderSelectionStrategy) {
        this.binderSelectionStrategy = binderSelectionStrategy;
    }

    public FormComponentInterceptor getInterceptor(FormModel formModel) {
        return getFormComponentInterceptorFactory().getInterceptor(formModel);
    }

    public FormPropertyFaceDescriptorSource getFormPropertyFaceDescriptorSource() {
        if (formPropertyFaceDescriptorSource == null) {
            initFormPropertyFaceDescriptorSource();
        }
        return formPropertyFaceDescriptorSource;
    }

    private void initFormPropertyFaceDescriptorSource() {
        try {
            this.formPropertyFaceDescriptorSource = (FormPropertyFaceDescriptorSource)getApplicationContext().getBean(
                    FORM_PROPERTY_FACE_DESCRIPTOR_SOURCE_BEAN_ID, FormPropertyFaceDescriptorSource.class);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No bean named " + FORM_PROPERTY_FACE_DESCRIPTOR_SOURCE_BEAN_ID
                    + " found; configuring defaults.");
            this.formPropertyFaceDescriptorSource = new MessageSourceFormPropertyFaceDescriptorSource();
        }
    }
    
    public void setFormPropertyFaceDescriptorSource(FormPropertyFaceDescriptorSource formPropertyFaceDescriptorSource) {
        this.formPropertyFaceDescriptorSource = formPropertyFaceDescriptorSource;
    }

    protected void initApplicationContext() throws BeansException {
        if (!lazyInit) {
            initStandardServices();
        }
        initLookAndFeelConfigurer();
    }

    public void initStandardServices() {
        getComponentFactory();
        getObjectConfigurer();
        getCommandConfigurer();
        getImageSource();
        getIconSource();
        getViewDescriptorRegistry();
        getRulesSource();
        getPropertyEditorRegistry();
    }

    public void initLookAndFeelConfigurer() {
        try {
            getApplicationContext().getBean(LOOK_AND_FEEL_CONFIGURER_BEAN_ID);
        }
        catch (NoSuchBeanDefinitionException e) {
            logger.info("No look and feel configurer found in context under name '" + LOOK_AND_FEEL_CONFIGURER_BEAN_ID
                    + "'; configuring defaults.");
        }
    }

    public boolean containsBean(String beanName) {
        return getApplicationContext().containsBean(beanName);
    }

    public Object getBean(String beanName) {
        return getApplicationContext().getBean(beanName);
    }

    public Object getBean(String beanName, Class requiredType) {
        return getApplicationContext().getBean(beanName, requiredType);
    }

    public BeanFactory getBeanFactory() {
        return getApplicationContext();
    }

    protected MessageSourceAccessor getMessageSourceAccessor() {
        return messageSourceAccessor;
    }

    public MessageSourceAccessor getMessages() {
        return getMessageSourceAccessor();
    }

    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        return getApplicationContext().getMessage(resolvable, locale);
    }

    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return getApplicationContext().getMessage(code, args, locale);
    }

    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return getApplicationContext().getMessage(code, args, defaultMessage, locale);
    }

    public Image getImage(String key) {
        return getImageSource().getImage(key);
    }

    public AwtImageResource getImageResource(String key) {
        return getImageSource().getImageResource(key);
    }

    public Icon getIcon(String key) {
        return getIconSource().getIcon(key);
    }

    public Resource getResource(String location) {
        if (location.startsWith(AwtImageResource.RESOURCE_PREFIX)) {
            return getImageResource(location.substring(AwtImageResource.RESOURCE_PREFIX.length()));
        }
        else {
            return getApplicationContext().getResource(location);
        }
    }

    public Object configure(Object applicationObject, String objectName) {
        return getObjectConfigurer().configure(applicationObject, objectName);
    }

    public AbstractCommand configure(AbstractCommand command) {
        return getCommandConfigurer().configure(command);
    }


}