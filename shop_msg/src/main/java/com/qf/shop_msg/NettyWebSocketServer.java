package com.qf.shop_msg;

import com.qf.handler.WebSocketHeartHandler;
import com.qf.handler.WebSocketInitHandler;
import com.qf.handler.WebSocketMsgHandler;
import com.qf.handler.WebSocketOutHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
@Component
public class NettyWebSocketServer implements CommandLineRunner {

    @Value("${server.port}")
    private Integer port;

    private EventLoopGroup master = new NioEventLoopGroup();

    private EventLoopGroup slave = new NioEventLoopGroup();

    /**
     * 消息处理器
     */
    @Autowired
    private WebSocketMsgHandler msgHandler;

    /**
     * 心跳处理器
     */
    @Autowired
    private WebSocketHeartHandler heartHandler;

    /**
     * 出站类型转换处理器
     */
    @Autowired
    private WebSocketOutHandler outHandler;

    /**
     * 注册事件处理器
     */
    @Autowired
    private WebSocketInitHandler initHandler;

    @Override
    public void run(String... args) throws Exception {

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap
                //配置Netty线程模型
                .group(master, slave)
                //配置管道类型
                .channel(NioServerSocketChannel.class)
                //配置事件处理
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();

                        //配置Http请求的事件处理器
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(1024 * 1024));

                        //配置WebSocket的事件处理器：http的升级请求
                        pipeline.addLast(new WebSocketServerProtocolHandler("/msg"));

                        //服务器端的心跳机制，在10s内没有发送消息，则就关闭连接
                        pipeline.addLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS));

                        //出站处理器
                        pipeline.addLast(outHandler);

                        //入站消息处理器
                        pipeline.addLast(msgHandler);
                        pipeline.addLast(heartHandler);
                        pipeline.addLast(initHandler);


                    }
                });

        serverBootstrap.bind(port).sync();
        System.out.println("消息中心启动成功，端口为：" + port);
    }
}
