/*
 * Copyright 2022. Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.models.map.storage.ldap;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.component.AmphibianProviderFactory;
import org.keycloak.models.map.common.AbstractEntity;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RoleModel;
import org.keycloak.models.map.common.SessionAttributesUtils;
import org.keycloak.models.map.storage.MapStorage;
import org.keycloak.models.map.storage.MapStorageProvider;
import org.keycloak.models.map.storage.MapStorageProviderFactory;
import org.keycloak.models.map.storage.ldap.config.LdapMapConfig;
import org.keycloak.models.map.storage.ldap.role.LdapRoleMapStorage;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

public class LdapMapStorageProviderFactory implements
        AmphibianProviderFactory<MapStorageProvider>,
        MapStorageProviderFactory,
        EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "ldap-map-storage";
    private final int factoryId = SessionAttributesUtils.grabNewFactoryIdentifier();

    private Config.Scope config;

    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, LdapRoleMapStorage.LdapRoleMapKeycloakTransactionFunction<KeycloakSession, Config.Scope, MapStorage>> MODEL_TO_STORE = new HashMap<>();
    static {
        MODEL_TO_STORE.put(RoleModel.class,            LdapRoleMapStorage::new);
    }

    public <M, V extends AbstractEntity> MapStorage<V, M> createMapStorage(KeycloakSession session, Class<M> modelType) {
        return MODEL_TO_STORE.get(modelType).apply(session, config);
    }

    @Override
    public MapStorageProvider create(KeycloakSession session) {
        return SessionAttributesUtils.createProviderIfAbsent(session, factoryId, LdapMapStorageProvider.class, session1 -> new LdapMapStorageProvider(session1, this, factoryId));
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
        LdapMapConfig cfg = new LdapMapConfig(config);
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.authentication", cfg.getConnectionPoolingAuthentication(), "none simple");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.initsize", cfg.getConnectionPoolingInitSize(), "1");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.maxsize", cfg.getConnectionPoolingMaxSize(), "1000");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.prefsize", cfg.getConnectionPoolingPrefSize(), "5");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.timeout", cfg.getConnectionPoolingTimeout(), "300000");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.protocol", cfg.getConnectionPoolingProtocol(), "plain ssl");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.debug", cfg.getConnectionPoolingDebug(), "off");
    }

    private static void checkSystemProperty(String name, String cfgValue, String defaultValue) {
        String value = System.getProperty(name);
        if(cfgValue != null) {
            value = cfgValue;
        }
        if(value == null) {
            value = defaultValue;
        }
        System.setProperty(name, value);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "LDAP Map Storage";
    }

    @Override
    public boolean isSupported() {
        return Profile.isFeatureEnabled(Profile.Feature.MAP_STORAGE);
    }

    @Override
    public void close() {
    }

}
