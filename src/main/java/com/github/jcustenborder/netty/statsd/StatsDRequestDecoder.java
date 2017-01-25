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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatsDRequestDecoder extends MessageToMessageDecoder<DatagramPacket> {
  static final Pattern PATTERN = Pattern.compile("^(?<metricName>.+):(?<value>[-\\.\\d]+)\\|(?<metricType>[^@]+)(@(?<sampleRate>[\\d\\.]+))?$");
  private static final Logger log = LoggerFactory.getLogger(StatsDRequestDecoder.class);
  private static final Charset CHARSET = Charset.forName("UTF-8");
  private final MetricFactory metricFactory;

  public StatsDRequestDecoder(MetricFactory metricFactory) {
    this.metricFactory = metricFactory;
  }

  public StatsDRequestDecoder() {
    this((name, value, sampleRate, type, sender, recipient) -> new Metric() {
      @Override
      public String name() {
        return name;
      }

      @Override
      public Double value() {
        return value;
      }

      @Override
      public Double sampleRate() {
        return sampleRate;
      }

      @Override
      public MetricType type() {
        return type;
      }

      @Override
      public InetSocketAddress sender() {
        return sender;
      }

      @Override
      public InetSocketAddress recipient() {
        return recipient;
      }
    });
  }

  Metric parseMetric(String input, InetSocketAddress sender, InetSocketAddress recipient) {
    log.trace("input = '{}'", input);

    Matcher matcher = PATTERN.matcher(input);
    if (!matcher.matches()) {
      if (log.isTraceEnabled()) {
        log.trace("input '{}' does not match PATTERN '{}'.", input, PATTERN.pattern());
      }
      return null;
    }

    String metricName = matcher.group("metricName");
    Double value = Double.parseDouble(matcher.group("value"));
    Metric.MetricType metricType;

    switch (matcher.group("metricType")) {
      case "g":
        metricType = Metric.MetricType.GUAGE;
        break;
      case "c":
        metricType = Metric.MetricType.COUNTER;
        break;
      case "ms":
        metricType = Metric.MetricType.TIMER;
        break;
      case "h":
        metricType = Metric.MetricType.HISTOGRAM;
        break;
      case "m":
        metricType = Metric.MetricType.METER;
        break;
      default:
        metricType = Metric.MetricType.UNKNOWN;
        break;
    }

    Double sampleRate;
    String sampleRateString = matcher.group("sampleRate");
    if (Metric.MetricType.COUNTER == metricType && null != sampleRateString) {
      sampleRate = Double.parseDouble(sampleRateString);
    } else {
      sampleRate = null;
    }

    if (log.isTraceEnabled()) {
      log.trace("metricName = '{}' type = '{}' value = {} sampleRate = {}", metricName, metricType, value, sampleRate);
    }

    return this.metricFactory.create(metricName, value, sampleRate, metricType, sender, recipient);
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
    ByteBuf s = datagramPacket.content();

    if (null == s || s.readableBytes() == 0) {
      return;
    }

    try (InputStream inputStream = new ByteBufInputStream(s)) {
      try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, CHARSET)) {
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
          String line;

          while (null != (line = reader.readLine())) {
            Metric metric = parseMetric(line, datagramPacket.sender(), datagramPacket.recipient());
            list.add(metric);
          }
        }
      }
    }
  }
}
