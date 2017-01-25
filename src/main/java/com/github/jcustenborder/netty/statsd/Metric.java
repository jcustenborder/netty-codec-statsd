/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
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
package com.github.jcustenborder.netty.statsd;

import java.net.InetSocketAddress;

public interface Metric {
  /**
   * Name of the metric
   * @return Name of the metric
   */
  String name();

  /**
   * Value for the metric
   * @return Value for the metric
   */
  Double value();

  /**
   * Sample rate for the metric. Only valid for counters.
   * @return Sample rate for the metric. Only valid for counters.
   */
  Double sampleRate();

  /**
   * Type of metric
   * @return Type of metric
   */
  MetricType type();

  /**
   * Remote address for the machine that sent the metric.
   * @return Remote address for the machine that sent the metric.
   */
  InetSocketAddress sender();

  /**
   * Address for the machine that received the metric.
   * @return Address for the machine that received the metric.
   */
  InetSocketAddress recipient();

  enum MetricType {
    GUAGE,
    COUNTER,
    TIMER,
    HISTOGRAM,
    METER,
    UNKNOWN
  }

}
