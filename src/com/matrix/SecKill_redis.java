package com.matrix;

import java.io.IOException;
import java.util.List;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class SecKill_redis {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecKill_redis.class);

    public static void main(String[] args) {
        Jedis jedis = new Jedis("192.168.255.132", 6379);
        System.out.println(jedis.ping());
        jedis.close();
    }

    public static boolean doSecKill(String uid, String prodid) throws IOException {

        // 拼接key
        String kcKey = "Seckill:" + prodid + ":kc";
        String userKey = "Seckill:" + prodid + ":user";

        Jedis jedis = new Jedis("192.168.255.132", 6379);

        // 监视库存
        jedis.watch(kcKey);

        // 获取库存
        String kc = jedis.get("kcKey");

        // 秒杀还没有开始 表示库存为null
        if (kc == null) {
            System.out.println("秒杀还没有开始 !");
            jedis.close();
            return false;
        }

        // 已经秒杀成功 表示为库存中uid的set集合中已经有该用户
        if (jedis.sismember(userKey, uid)) {
            System.out.println("已经秒杀过 不可重复秒杀 !");
            jedis.close();
            return false;
        }

        // 判断库存 若大于零则减库存 若小于等于零则秒杀结束
        if (Integer.parseInt(kc) <= 0) {
            System.out.println("秒杀已结束");
            jedis.close();
            return false;
        }

        // 库存大于零
        Transaction transaction = jedis.multi();
//        jedis.decr(kcKey);
//        jedis.sadd(userKey, uid);
        transaction.decr(kcKey);
        transaction.sadd(userKey, uid);
        List<Object> exec = transaction.exec();

        if (exec == null || exec.size() == 0) {
            System.out.println("秒杀失败");
            jedis.close();
            return false;
        }

        System.out.println("秒杀成功 !");
        jedis.close();
        return true;

//        //拼接key
//        String kcKey = "Seckill:" + prodid + ":kc";
//        String userKey = "Seckill:" + prodid + ":user";
//        Jedis jedis = new Jedis("192.168.255.132", 6379);
//        //监视库存
//        jedis.watch(kcKey);
//        //获取库存
//        String kc = jedis.get(kcKey);
//
//        //秒杀还没开始，表示为库存为null
//        if (kc == null) {
//            System.out.println("秒杀还没开始");
//            jedis.close();
//            return false;
//        }
//
//        //已经秒杀成功，表示为存储uid的set中已经有该用户uid
//        if (jedis.sismember(userKey, uid)) {
//            System.out.println("已经秒杀成功，不能重复秒杀");
//            jedis.close();
//            return false;
//        }
//
//        //判断库存，若大于0，则减库存，加人，若小于等于0，秒杀结束
//        if (Integer.parseInt(kc) <= 0) {
//            System.out.println("秒杀已结束");
//            jedis.close();
//            return false;
//        }
//
//        //库存大于0，减库存，加人
//        Transaction transaction = jedis.multi();
//
//        transaction.decr(kcKey);
//        transaction.sadd(userKey, uid);
//
//        List<Object> exec = transaction.exec();
//
//        if (exec == null || exec.size() == 0) {
//            System.out.println("秒杀失败");
//            jedis.close();
//            return false;
//        }
//
//        System.out.println("秒杀成功");
//        jedis.close();
//        return true;
    }
}
