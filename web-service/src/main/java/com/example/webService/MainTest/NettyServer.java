package com.example.webService.MainTest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.InboundChannelAdapter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: ServerSocketChannel
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/9/26 21:46
 * @Version: 1.0.0
 * @Description: TODO
 */

@Slf4j
public class NettyServer {
    public static void main(String[] args) {
        // select 多路复用
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();
        //线程间通信 , 计数为0时，唤醒线程；
        //CountDownLatch countDownLatch = new CountDownLatch(1);
        //countDownLatch.countDown(); 计数减去1
        //countDownLatch.wait(); 线程挂起
        //
        ServerBootstrap serverBootstrap;
        ChannelFuture channelFuture = null;
        try {
            serverBootstrap = new ServerBootstrap()
                    // select -> accept，read/write 事件
                    .group(boss,work)
                    // 协议/通道类型
                    .channel(NioServerSocketChannel.class)
                    // 连接初始化回调对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 连接初始化回调方法
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            log.debug("server init channel ");

                            // 心跳检测 ， 需要而外提供检测触发的回调（出、入站channel handel，事件检测方法）。 p0: 读间隔时间 , p1：写间隔时间 ， p2: 读写合计间隔时间
                            socketChannel.pipeline().addLast(new IdleStateHandler(0,0,5));
                            // 出、入站channel handel , 检测事件
                            socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                                // 客户端 连接成功
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelActive(ctx);
                                    System.out.println("---server---ChannelInboundHandlerAdapter---channelActive");
                                }
                                // 客户端 断开连接
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelInactive(ctx);
                                    // 关闭 client socket ， 删除集合中的对象
                                    System.out.println("---server---ChannelInboundHandlerAdapter---channelInactive");
                                }
                                // 客户端 异常
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    super.exceptionCaught(ctx, cause);
                                    System.out.println("---server---ChannelInboundHandlerAdapter---exceptionCaught :"+cause.getMessage());
                                }
                                // 心跳事件监听回调
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    super.userEventTriggered(ctx, evt);
                                    // 检测IdleStateHandler 事件
                                    IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                    System.out.println("---IdleStates event:"+idleStateEvent.toString());
                                    // close channel
                                    //ctx.channel().close();
                                }
                            });
                            // 半包、黏包 处理 ，需客户端消息字节中添加内容字节（ 基于内容 length 字段的处理；base length field ）, p0:缓冲区的最大字节长度 、p1: length 字段的起始偏移量字节、p2：length 字段占用的字节、p3：length 字段后还要排除几个字段、p4：以上得出的结果还要去掉几个字段为内容。
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,0,4,0,4));

                            // netty 出站和入站都是 byteBuf 对象也就是 nio.socket 的byteBuffer , 底层传输为 byte[]
                            // ** 所有的handle 都是 事件的回调拦截，handle chain 需要向下传递
                            //input byte[] to string decode
//                            socketChannel.pipeline().addLast(new StringDecoder());
                            //output string to byte[] encode
//                            socketChannel.pipeline().addLast(new StringEncoder());
                            //input 1
                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    // ByteBuf 可以理解为是一个 byte[]
                                    ByteBuf buf = (ByteBuf)msg;
                                    System.out.println("---server1---input read: "+msg);
                                    //
//                                    System.out.println("---server1---input read: "+buf.readByte());
                                    // byteBuf 中可读字节数
                                    byte[] bytes = new byte[buf.readableBytes()];
                                    buf.readBytes(bytes);
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for (int i=0;i<bytes.length;i++){
                                        stringBuffer.append(bytes[i]);
                                    }
                                    System.out.println("---server1---input read byte 10进制: "+stringBuffer.toString());
                                    // handle chain... ( 传递 handle 链： ctx.fireChannelRead(msg); )
                                    ctx.fireChannelRead(bytes);
                                    // 释放 byteBuf 内存
                                    buf.release();

                                    // client socket close

                                }
                            });
                            //input 2
                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("---server2---input read: "+msg);
                                    //
                                    byte[] bytes = (byte[]) msg;
                                    String str = new String(bytes,StandardCharsets.UTF_8);
                                    System.out.println("---server2---input read: "+str);
                                    //
                                    //socketChannel.writeAndFlush("sent back:"+msg);
                                    String outstr = "sent back: "+str;
                                    ByteBuf byteBuf = ctx.alloc().buffer();
                                    byteBuf.writeBytes(outstr.getBytes(StandardCharsets.UTF_8));
                                    //
                                    socketChannel.writeAndFlush(byteBuf);
                                }
                            });
                            //output
                            socketChannel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    System.out.println("---server---output write: "+msg);
                                    // handle chain... ( 传递 handle 链： ctx.fireChannelRead(msg); )
                                    super.write(ctx, msg, promise);

                                }
                                // handel 传递时，会触发下一个 channel.pipeline 中的 handle 读取 当前 handle 事件.
                                @Override
                                public void read(ChannelHandlerContext ctx) throws Exception {
                                    // handle chain... ( 传递 handle 链： ctx.fireChannelRead(msg); )
                                    super.read(ctx);
                                    System.out.println("---server---output read: ");
                                }
                            });

                        }

                    });
            channelFuture = serverBootstrap.bind(9099).sync();
            //
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            log.debug("exception by server Socket:{}",e);

        } finally {
            log.debug("finally closed ...");
            boss.shutdownGracefully();
            work.shutdownGracefully();

        }

    }

}
