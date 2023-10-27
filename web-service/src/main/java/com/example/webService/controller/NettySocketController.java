package com.example.webService.controller;

import com.example.webService.common.DataResult;
import com.example.webService.ioNetty.clientSocketUtil.ClientSocketUtil;
import io.netty.channel.socket.SocketChannel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: NettySocketController
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/9/30 13:58
 * @Version: 1.0.0
 * @Description: TODO
 */
@RestController
@CrossOrigin // 跨域
public class NettySocketController {

    // client socket 工具类
    @Autowired
    private ClientSocketUtil clientSocketUtil;

    // thread pool
    private ThreadPoolExecutor threadPool= new ThreadPoolExecutor(10, 15, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @GetMapping("/getClientSocket")
    //@Hidden
    public DataResult getClientSocket(){//@PathVariable int eventId
        System.out.println("getClientSocket  : ");
        //
        Map<String, SocketChannel> clientSockets = this.clientSocketUtil.getClientSockets();
        List<String> channels = new ArrayList<>();
        if(clientSockets != null && clientSockets.size() > 0){
            Set<Map.Entry<String,SocketChannel>> entries = clientSockets.entrySet();
            for (Map.Entry<String,SocketChannel> entry : entries){
                System.out.println("address:"+entry.getValue().remoteAddress());
                channels.add(entry.getValue().remoteAddress().toString());
            }
        }
        //System.out.println("---"+clientSockets.size());
        //System.out.println("---"+clientSockets.get("0").remoteAddress());
        return new DataResult<>("ok",channels);
    }
    @PostMapping ("/sentAllChannel")
    //@Hidden
    public DataResult sentAllChannel(@RequestBody String msg,HttpServletRequest request) {//@RequestParm支持POST,GET请求 只支持Content-Type: 为 application/x-www-form-urlencoded编码的内容。
        System.out.println("sentAllChannel  msg: "+ msg);
        Map<String, SocketChannel> clientSockets = this.clientSocketUtil.getClientSockets();
        if(clientSockets != null && clientSockets.size() > 0){
            Set<Map.Entry<String,SocketChannel>> entries = clientSockets.entrySet();
            for (Map.Entry<String,SocketChannel> entry : entries){
                System.out.println("address:"+entry.getValue().remoteAddress());
                //
                this.threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        clientSocketUtil.sentMsgByName(entry.getKey(),msg);
                    }
                });
            }
            return new DataResult("ok");
        }
        return new DataResult(0,"failed","服务器未检测到客户端接入");
    }

    @PostMapping ("/sentFirstOneChannel")
    //@Hidden
    public DataResult sentFirstOneChannel(@RequestBody String msg) {//@RequestParm支持POST,GET请求 只支持Content-Type: 为 application/x-www-form-urlencoded编码的内容。
        System.out.println("sentFirstOneChannel  msg: "+ msg);
        Map<String, SocketChannel> clientSockets = this.clientSocketUtil.getClientSockets();
        if(clientSockets != null && clientSockets.size() > 0){
            Set<Map.Entry<String,SocketChannel>> entries = clientSockets.entrySet();
            for (Map.Entry<String,SocketChannel> entry : entries){
                System.out.println("address:"+entry.getValue().remoteAddress());
                //
//                this.threadPool.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                });
                clientSocketUtil.sentMsgByName(entry.getKey(),msg);

                return new DataResult("ok");
            }
        }
        return new DataResult(0,"failed","服务器未检测到客户端接入");
    }

    @GetMapping("/getBackMsg")
    //@Hidden
    public DataResult getBackMsg() {
        System.out.println("getBackMsg  : ");
        List<String> msgs = this.clientSocketUtil.getAllMsgs();
        return new DataResult<>("ok",msgs);
    }

}
