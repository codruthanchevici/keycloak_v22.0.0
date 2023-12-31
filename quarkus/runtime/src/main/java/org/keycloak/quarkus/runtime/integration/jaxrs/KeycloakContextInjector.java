/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.jaxrs;

import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.jboss.resteasy.spi.ContextInjector;
import org.keycloak.common.util.Resteasy;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.integration.web.QuarkusRequestFilter;

/**
 * <p>This {@link ContextInjector} allows injecting {@link KeycloakSession} to JAX-RS resources.
 *
 * <p>Due to the latest changes in Quarkus, the context map is cleared prior to dispatching to JAX-RS resources, so we need
 * to delegate to the {@link ResteasyVertxProvider} provider the lookup of Keycloak contextual objects.
 *
 * @see QuarkusRequestFilter
 * @see ResteasyVertxProvider
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@Provider
public class KeycloakContextInjector implements ContextInjector<KeycloakSession, KeycloakSession> {
    @Override
    public KeycloakSession resolve(Class rawType, Type genericType, Annotation[] annotations) {
        return Resteasy.getContextData(KeycloakSession.class);
    }
}
