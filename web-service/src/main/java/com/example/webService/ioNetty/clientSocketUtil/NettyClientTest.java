package com.example.webService.ioNetty.clientSocketUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @ClassName: NettyClient
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/9/26 21:47
 * @Version: 1.0.0
 * @Description: TODO
 */
@Slf4j
public class NettyClientTest {
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

                            // 心跳检测 ， 需要而外提供检测触发的回调（出、入站channel handel，事件检测方法）。 p0: 读间隔时间 , p1：写间隔时间 ， p2: 读写合计间隔时间
                            //socketChannel.pipeline().addLast(new IdleStateHandler(0,0,3));
                            // 出、入站channel handel , 检测事件
                            socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    //super.userEventTriggered(ctx, evt);
                                    // 检测IdleStateHandler 事件
                                    IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                    System.out.println("---IdleStates event:"+idleStateEvent.toString());
                                    // sent heartBeat 心跳
                                    String heartBeat = "heartBeat";
                                    ByteBuf buf = ctx.alloc().buffer();
                                    // 满足 半包粘包 handle
                                    buf.writeInt(heartBeat.length());
                                    buf.writeBytes(heartBeat.getBytes(StandardCharsets.UTF_8));
                                    ctx.channel().writeAndFlush(buf);
                                }
                            });

                            // netty 出站和入站都是 byteBuf 对象也就是 nio.socket 的byteBuffer , 底层传输为 byte[]
                            // ** 所有的handle 都是 事件的回调拦截，handle chain 需要向下传递
                            //input byte[] tos string decode
                            //socketChannel.pipeline().addLast(new StringDecoder());
//                          //output string to byte[] encode
                            //socketChannel.pipeline().addLast(new StringEncoder());

                            //input 1
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
                                    // 释放 byteBuf 内存
                                    buf.release();
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

                                    // 处理事件响应
                                    ByteBuf buf = ctx.alloc().buffer();
                                    String[] strings = str.split(",");
                                    System.out.println("---client2---input read :"+strings);
                                    // 返回格式标志 + 正文
                                    str = "b,"+strings[0]+","+strings[1]+",响应处理结果";
                                    // length
                                    buf.writeInt(str.length());
                                    // 正文
                                    buf.writeBytes(str.getBytes(StandardCharsets.UTF_8));
                                    ctx.channel().writeAndFlush(buf);
                                    // 关闭 channel
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
            channelFuture = bootstrap.connect("localhost",9099).sync();
            //
            for (int i =0;i<0;i++){
                Thread.sleep(2000);
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
