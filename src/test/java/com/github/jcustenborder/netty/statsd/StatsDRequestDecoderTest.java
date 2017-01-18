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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatsDRequestDecoderTest {
  private static final Logger log = LoggerFactory.getLogger(StatsDRequestDecoderTest.class);
  final static Charset charset = Charset.forName("UTF-8");
  StatsDRequestDecoder decoder;


  @Before
  public void before() {
    this.decoder = new StatsDRequestDecoder();
  }

  void assertMetric(final String input, final Metric expected) throws Exception {
    final byte[] buffer = input.getBytes(charset);
    final ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer);
    ChannelHandlerContext context = mock(ChannelHandlerContext.class);

    List<Object> list = new ArrayList<>();

    InetSocketAddress sender = new InetSocketAddress("8.8.8.8", 64321);
    when(expected.sender()).thenReturn(sender);
    InetSocketAddress recipient = new InetSocketAddress("8.8.4.4", 8125);
    when(expected.recipient()).thenReturn(recipient);

    DatagramPacket datagramPacket = new DatagramPacket(byteBuf, recipient, sender);

    this.decoder.decode(context, datagramPacket, list);
    assertFalse("list should not be empty", list.isEmpty());
    Object result = list.get(0);
    assertNotNull("result should not be null.", result);
    assertTrue("result should be a metric.", result instanceof Metric);
    final Metric actual = (Metric) result;
    assertEquals("expected.name() does not match", expected.name(), actual.name());
    assertEquals("expected.sampleRate() does not match", expected.sampleRate(), actual.sampleRate());
    assertEquals("expected.type() does not match", expected.type(), actual.type());
    assertEquals("expected.value() does not match", expected.value(), actual.value());
    assertEquals("expected.sender() does not match", expected.sender(), actual.sender());
    assertEquals("expected.recipient() does not match", expected.recipient(), actual.recipient());
  }

  @Test
  public void gauge() throws Exception {
    final Metric expected = mock(Metric.class);
    when(expected.name()).thenReturn("foo");
    when(expected.type()).thenReturn(Metric.MetricType.GUAGE);
    when(expected.value()).thenReturn(123451D);
    when(expected.sampleRate()).thenReturn(null);
    final String input = "foo:123451|g\n";
    assertMetric(input, expected);
  }

  @Test
  public void counter() throws Exception {
    final Metric expected = mock(Metric.class);
    when(expected.name()).thenReturn("foo");
    when(expected.type()).thenReturn(Metric.MetricType.COUNTER);
    when(expected.value()).thenReturn(-123451D);
    when(expected.sampleRate()).thenReturn(null);
    final String input = "foo:-123451|c\n";
    assertMetric(input, expected);
  }

  @Test
  public void counterWithSampleRate() throws Exception {
    final Metric expected = mock(Metric.class);
    when(expected.name()).thenReturn("foo");
    when(expected.type()).thenReturn(Metric.MetricType.COUNTER);
    when(expected.value()).thenReturn(-123451D);
    when(expected.sampleRate()).thenReturn(0.1D);
    final String input = "foo:-123451|c@0.1\n";
    assertMetric(input, expected);
  }

  @Test
  public void timer() throws Exception {
    final Metric expected = mock(Metric.class);
    when(expected.name()).thenReturn("foo");
    when(expected.type()).thenReturn(Metric.MetricType.TIMER);
    when(expected.value()).thenReturn(123451D);
    when(expected.sampleRate()).thenReturn(null);
    final String input = "foo:123451|ms\n";
    assertMetric(input, expected);
  }

  @Test
  public void histogram() throws Exception {
    final Metric expected = mock(Metric.class);
    when(expected.name()).thenReturn("foo");
    when(expected.type()).thenReturn(Metric.MetricType.HISTOGRAM);
    when(expected.value()).thenReturn(123451D);
    when(expected.sampleRate()).thenReturn(null);
    final String input = "foo:123451|h\n";
    assertMetric(input, expected);
  }

  @Test
  public void meter() throws Exception {
    final Metric expected = mock(Metric.class);
    when(expected.name()).thenReturn("foo");
    when(expected.type()).thenReturn(Metric.MetricType.METER);
    when(expected.value()).thenReturn(123451D);
    when(expected.sampleRate()).thenReturn(null);
    final String input = "foo:123451|m\n";
    assertMetric(input, expected);
  }

}
