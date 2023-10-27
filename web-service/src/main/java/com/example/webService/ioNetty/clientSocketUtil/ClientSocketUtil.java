package com.example.webService.ioNetty.clientSocketUtil;

import com.example.webService.common.DataResult;
import com.example.webService.entity.PendingEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.SocketChannel;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @ClassName: ClientSocketUtil
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/9/28 15:23
 * @Version: 1.0.0
 * @Description: socket 客户端连接工具类
 */

@Component
public class ClientSocketUtil {
    // 客户端集合 (线程安全)
    private static Hashtable<String, SocketChannel> clientSocketsByName = new Hashtable<>();
    private static Hashtable<SocketChannel, String> clientSocketsByChannel = new Hashtable<>();
    // 待处理事件（客户端确认事件）(线程安全)
    private static List<PendingEvent> socketEvents = new Vector<>();
    // 定时任务线程池
    
    // 添加 channel
    public synchronized void addSocketChannel(String name,SocketChannel socketChannel){
        //
        clientSocketsByName.put(name,socketChannel);
        clientSocketsByChannel.put(socketChannel,name);
    }

    // 删除 channel by name
    public synchronized void removeSocketChannel(String name){
        // remove name
        clientSocketsByChannel.remove(this.getSocketChannel(name));
        // remove channel
        clientSocketsByName.remove(name);
    }

    // 删除 channel by channel
    public synchronized void removeSocketChannel(SocketChannel socketChannel){
        // remove channel
        clientSocketsByName.remove(this.getSocketChannelName(socketChannel));
        // remove name
        clientSocketsByChannel.remove(socketChannel);

    }

    // 获取 channel by name
    public SocketChannel getSocketChannel(String name){
        return clientSocketsByName.get(name);
    }

    // 获取 channel name by channel
    public String getSocketChannelName(SocketChannel socketChannel){
        return clientSocketsByChannel.get(socketChannel);
    }

    // 获取 channel 数量
    public int getCurrentSocketChannelCount(){
        Set<Map.Entry<String, SocketChannel>> entries = clientSocketsByName.entrySet();
        for (Map.Entry<String,SocketChannel> entry: entries){
            System.out.println("----name:"+entry.getKey()+";channel:"+entry.getValue());
        }
        return clientSocketsByName.size();
    }

    // 单点发送
    public DataResult sentMsgByName(String name,String msg){
        SocketChannel socketChannel = this.getSocketChannel(name);
        if (socketChannel != null){
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
            buf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
            socketChannel.writeAndFlush(buf);
            return new DataResult("sent msg ok");
        }else {
            return new DataResult(1,"socket channel not exist");
        }
    }

    // 添加待处理事件
    public boolean addEvent(PendingEvent event){
        return socketEvents.add(event);
    }

    // 删除待处理事件
    public boolean deleteEvent(PendingEvent event){
        return socketEvents.remove(event);
    }
    // 删除待处理事件
    public PendingEvent deleteEvent(int index){
        return socketEvents.remove(index);
    }

    // confirm 确认待处理事件
    public boolean confirmEvent(String channelName){
        return true;
    }

    // confirm 确认待处理事件
    public boolean confirmEvent(int socketChannelId,int feedBack){
        for (PendingEvent event : socketEvents){
            // 反馈事件的客户端id匹配
            if(event.getSocketChannelId() == socketChannelId){
                // 反馈事件状态类型匹配
                if (event.getStatus() == feedBack){
                    System.out.println("---确认待处理事件完成");
                    //
                    return deleteEvent(event);
                }
            }
        }
        System.out.println("---确认待处理事件无匹配");
        return false;
    }

    // 检查确认事件是否处理完成
    public int checkEvent(int eventId){
        System.out.println("---检查确认事件是否处理完成：");
        for (PendingEvent event : socketEvents){
            System.out.println(event.toString());
            if (event.getId() == eventId){
                return 1;
            }
        }
        return 0;
    }

    // 执行未处理事件
    public void executeEvent(int channelId){
        for (PendingEvent event : socketEvents){
            if (event.getSocketChannelId() == channelId){
                this.sentMsgByName(String.valueOf(channelId),event.getId()+","+channelId+",check payment 已支付");
            }
        }
    }

    // 获取客户端集合
    public Hashtable<String,SocketChannel> getClientSockets(){
        return clientSocketsByName;
    }


    private static List<String> msgs = new Vector<>();

    public void addMsg(String msg){
        msgs.add(msg);
    }

    public List<String> getAllMsgs(){
        ArrayList<String> bMsgs = new ArrayList<>();
        for (String msg : msgs){
            bMsgs.add(msg);
        }
        msgs.clear();
        return bMsgs;
    }


}
