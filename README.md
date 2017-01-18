## Introduction

This project is an implementation of the StatsD protocol for [Netty](http://netty.io/). Currently there is only support for
the UDP protocol. More than one entry can be sent by separating each entry with a new line.


## Usage

```
b = new Bootstrap();
b.group(bossGroup)
    .channel(NioDatagramChannel.class)
    .handler(new ChannelInitializer<DatagramChannel>() {
      @Override
      protected void initChannel(DatagramChannel datagramChannel) throws Exception {
        ChannelPipeline channelPipeline = datagramChannel.pipeline();
        channelPipeline.addLast(
            new LoggingHandler("StatsD", LogLevel.TRACE),
            new StatsDRequestDecoder(),
            new StatsDRequestHandler(config, records, time)
        );
      }
    });
```

Each metric that is found will be passed along in the pipeline.

```
class StatsDRequestHandler extends SimpleChannelInboundHandler<Metric> {
  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, Metric metric) throws Exception {
  
  }
}
```