package com.example.webService.ioNetty.serverSocket;

import com.example.webService.ioNetty.clientSocketUtil.ClientSocketUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName: NettyServerSocket
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/9/28 10:17
 * @Version: 1.0.0
 * @Description: 基于 java nio socket（非阻塞io），netty 框架网络编程
 */

@Slf4j
@Component
public class NettyServerSocket {

    private int name = 0;
    // client socket 工具类
    @Autowired
    private ClientSocketUtil clientSocketUtil;

    // select 多路复用
    private NioEventLoopGroup boss = new NioEventLoopGroup();
    private NioEventLoopGroup work = new NioEventLoopGroup();
    //
    ServerBootstrap serverBootstrap;
    ChannelFuture channelFuture = null;
    public void start(){
        log.info("start inner...");
        try {
            serverBootstrap = new ServerBootstrap()
                    .group(boss,work)
                    .channel(NioServerSocketChannel.class)
                    // 连接初始化回调对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            log.info("server init channel ");
                            // 绑定 handle 事件回调 ， input/output

                            // 心跳检测 ， 需要而外提供检测触发的回调（出、入站channel handel，事件检测方法）。 p0: 读间隔时间 , p1：写间隔时间 ， p2: 读写合计间隔时间
                            //socketChannel.pipeline().addLast(new IdleStateHandler(0,0,10));
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
                                    System.out.println("---server---ChannelInboundHandlerAdapter---channelInactive");
                                    // 关闭 client socket ， 删除集合中的对象
                                    clientSocketUtil.removeSocketChannel((SocketChannel) ctx.channel());
                                    //
                                    clientSocketUtil.getCurrentSocketChannelCount();
                                }
                                // 客户端 异常
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    super.exceptionCaught(ctx, cause);
                                    System.out.println("---server---ChannelInboundHandlerAdapter---exceptionCaught :"+cause.getMessage());
                                }
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    //super.userEventTriggered(ctx, evt);
                                    // 检测IdleStateHandler 事件
                                    IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                    System.out.println("---IdleStates event:"+idleStateEvent.toString());
                                    // close channel
                                    ctx.channel().close();
                                }
                            });

                            // 半包、黏包 处理 ，需客户端消息字节中添加内容字节（ 基于内容 length 字段的处理；base length field ）, p0:缓冲区的最大字节长度 、p1: length 字段的起始偏移量字节、p2：length 字段占用的字节、p3：length 字段后还要排除几个字段、p4：以上得出的结果还要去掉几个字段为内容。
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(2048,0,4,0,4));
                            // input byte[] to string decode
                            //socketChannel.pipeline().addLast(new StringDecoder());
                            // output string to byte[] encode
                            //socketChannel.pipeline().addLast(new StringEncoder());
                            // custom input
                            socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("---server---input---read byteBuf: "+msg);
                                    //
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    byte[] bytes = new byte[byteBuf.readableBytes()];
                                    byteBuf.readBytes(bytes);
                                    // 释放 byteBuf 内存
                                    byteBuf.release();
                                    //
                                    String str = new String(bytes, StandardCharsets.UTF_8);
                                    System.out.println("---server---input---read str: "+str);

                                    //存储接收的消息
                                    clientSocketUtil.addMsg(str);

                                    //
                                    String[] strings = str.split(",");
                                    // 处理返回
                                    if(strings[0].equals("b")){
                                        System.out.println("收到确认消息");
                                        // 处理确认消息
                                        int count = clientSocketUtil.checkEvent(Integer.parseInt(strings[1]));
                                        System.out.println("处理完成：COUNT:"+count);
                                    }else {
                                        // 回送信息
                                        str = "server send back:"+str;
                                        ByteBuf buf = ctx.alloc().buffer();
                                        buf.writeBytes(str.getBytes(StandardCharsets.UTF_8));
                                        ctx.channel().writeAndFlush(buf);
                                        //super.channelRead(ctx, msg);
                                    }

                                }
                            });
                            // custom output
                            socketChannel.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
                                @Override
                                public void read(ChannelHandlerContext ctx) throws Exception {
                                    log.info("serverSocket ChannelOutboundHandlerAdapter read");
                                    super.read(ctx);
                                }

                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    log.info("serverSocket ChannelOutboundHandlerAdapter write msg: {}",msg);
                                    super.write(ctx, msg, promise);
                                }
                            });

                            // 添加 client socket channel 实例

                            clientSocketUtil.addSocketChannel(String.valueOf(name),socketChannel);
                            // 打印 数量
                            clientSocketUtil.getCurrentSocketChannelCount();
                            // 是否有未处理事件
                            clientSocketUtil.executeEvent(name);
                            //
                            name++;
                        }
                    });
            // bind
            log.info("serverBootstrap.bind(9099) before...");
            channelFuture = serverBootstrap.bind(9099).sync();
            log.info("serverBootstrap.bind(9099) is successful");
            // close event
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            log.info("serverBootstrap start Exception: {}",e);

        } finally {
            // close select(多路复用器/事件监听循环器)
            boss.shutdownGracefully();
            work.shutdownGracefully();
            log.info("shutdownGracefully select: boss/work");
        }
    }
    //
    public ChannelFuture getChannelFuture(){
        return this.channelFuture;
    }
}
