/*
 * Copyright (c) 2023 Li Wan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package works.lifeops.observe.prom4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ComponentScan(basePackages = { Prom4jApiConfiguration.API_PACKAGE })
public class Prom4jApiConfiguration {
  public static final String API_PACKAGE = "works.lifeops.observe.prom4j.api";
  @Autowired
  private RequestMappingHandlerMapping requestMappingHandlerMapping;

  public Prom4jApiConfiguration() {
    // Do nothing, we only need the @ComponentScan to work here instead of using it at the Prom4jApplication
    log.info("Loading generated service stubs under package {}", Prom4jApiConfiguration.API_PACKAGE);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void listApi() {
    requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
        .map(RequestMappingInfo::toString)
        .sorted()
        .forEach(mapping -> log.info("Endpoint {}", mapping));
  }
}
