package com.example.webService.MainTest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @ClassName: NettyClient
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/9/26 21:47
 * @Version: 1.0.0
 * @Description: TODO
 */
@Slf4j
public class NettyClient {
    public static void main(String[] args) {

        NioEventLoopGroup work = new NioEventLoopGroup();
        ChannelFuture channelFuture;
        Bootstrap bootstrap = null;
        try {
            bootstrap = new Bootstrap()
                    .group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            log.debug("client init channel ");
                            // netty 出站和入站都是 byteBuf 对象也就是 nio.socket 的byteBuffer , 底层传输为 byte[]
                            // ** 所有的handle 都是 事件的回调拦截，handle chain 需要向下传递
                            //input byte[] tos string decode
                            //socketChannel.pipeline().addLast(new StringDecoder());
//                          //output string to byte[] encode
                            //socketChannel.pipeline().addLast(new StringEncoder());
//                            //input 1
                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("---client1---input read :"+msg);
                                    ByteBuf buf = (ByteBuf) msg;
                                    byte[] bytes = new byte[buf.readableBytes()];
                                    buf.readBytes(bytes);
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for (int i=0;i<bytes.length;i++){
                                        stringBuffer.append(bytes[i]);
                                    }
                                    System.out.println("---client1---input read byte 10进制 :"+stringBuffer.toString());
                                    // handle chain... ( 传递 handle 链： ctx.fireChannelRead(msg); )
                                    super.channelRead(ctx, bytes);
                                }
                            });
                            //input 2
                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    byte[] bytes = (byte[]) msg;
                                    //
                                    String str = new String(bytes,StandardCharsets.UTF_8);
                                    System.out.println("---client2---input read :"+str);
                                    //
                                    //ctx.channel().close();
                                }
                            });
//                            //output
                            socketChannel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    System.out.println("---client---output write :"+msg);
                                    // handle chain... ( 传递 handle 链： ctx.fireChannelRead(msg); )
                                    super.write(ctx, msg, promise);

                                }
                                // handel 传递时，会触发下一个 channel.pipeline 中的 handle 读取 当前 handle 事件.
                                @Override
                                public void read(ChannelHandlerContext ctx) throws Exception {
                                    // handle chain... ( 传递 handle 链： ctx.fireChannelRead(msg); )
                                    super.read(ctx);
                                    System.out.println("---client----output read: ");
                                }
                            });
                        }
                    });
            //
            channelFuture = bootstrap.connect("localhost",9080).sync();
            //
            for (int i =0;i<10;i++){
                Thread.sleep(1000);
                ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
                // content
                String str = "hello server/服务器 from client_"+i;
                byte[] contentBytes = str.getBytes(StandardCharsets.UTF_8);

                // length 字段用 int类型 表示占用 4个字节 , （配合 基于length字段的粘包/半包处理器使用）
                int contentByteLength = contentBytes.length;
                // length field ，写入内容长度 int 占用4个字节
                buf.writeInt(contentByteLength);

                // 写入内容
                buf.writeBytes(contentBytes);
                //
                channelFuture.channel().writeAndFlush(buf);

            }
//            String str = "hello server from client_xxx";
//            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
//            buf.writeBytes(str.getBytes());
//            channelFuture.channel().writeAndFlush(buf);

            //
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            log.debug("exception by client Socket:{}",e);

        }finally {
            log.debug("finally closed ...");
            work.shutdownGracefully();
        }
    }
}
