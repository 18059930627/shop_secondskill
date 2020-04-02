package com.qf.util;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Channel工具类  uid - channel
 *
 * @Author chenzhongjun
 * @Date 2020/1/8
 */
public class ChannelUtil {

    private static Map<Integer, Channel> map = new ConcurrentHashMap<>();

    /**
     * 绑定
     *
     * @param uid
     * @param channel
     */
    public static void add(Integer uid, Channel channel) {
        map.put(uid, channel);
    }

    /**
     * 根据用户ID移除连接
     *
     * @param uid
     */
    public static void removeChannel(Integer uid) {
        map.remove(uid);
    }

    /**
     * 根据用户ID得到对应的连接
     *
     * @param uid
     * @return
     */
    public static Channel getChannel(Integer uid) {
        return map.get(uid);
    }

    /**
     * 通过Channel得到对应的用户
     *
     * @param channel
     * @return
     */
    public static Integer getUid(Channel channel) {
        for (Map.Entry<Integer, Channel> entry : map.entrySet()) {
            if (entry.getValue() == channel) {
                return entry.getKey();
            }
        }
        return null;
    }
}
