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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

public class Prom4jSpringHelper implements ApplicationContextAware {
  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    Prom4jSpringHelper.applicationContext = applicationContext;
  }

  public static <T> T getBean(Class<T> type) {
    return Prom4jSpringHelper.applicationContext.getBean(type);
  }

  public static <T> T getBean(String name, Class<T> type) {
    return Prom4jSpringHelper.applicationContext.getBean(name, type);
  }

  public static UriBuilder getProm4jUriBuilder() {
    return getBean("prom4jUriBuilderFactory", UriBuilderFactory.class).builder();
  }
}
