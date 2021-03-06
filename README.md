### cat


>   forest Java sdk



### 说明

[分布式调度平台](https://github.com/busgo/forest)


### 快速开始

```java

package com.busgo.cat.test;

import com.alibaba.fastjson.JSON;
import com.busgo.cat.ForestClient;
import com.busgo.cat.JobSnapshot;
import com.busgo.cat.etcd.EtcdClient;
import com.busgo.cat.job.Job;
import com.busgo.cat.test.job.EchoJob;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试模块
 *
 * @author busgo
 * @date 2019-12-14 19:13
 */
public class ForestTest {


    private final static Logger log = LoggerFactory.getLogger(ForestTest.class);


    private final String group = "trade";

    private final String ip = "127.0.0.1";

    private final String endpoints = "http://localhost:2379";


    private EtcdClient etcdClient;


    @Before
    public void before() {


        this.etcdClient = new EtcdClient(endpoints);
        String jobSnapshotPath = String.format("/forest/client/snapshot/%s/%s/", group, ip);
        for (int a = 0; a < 100; a++) {


            JobSnapshot snapshot = new JobSnapshot();
            snapshot.setId(UUID.randomUUID().toString());
            snapshot.setCreateTime(new Date());
            snapshot.setCron("0 0 * * * ?");
            snapshot.setGroup(group);
            snapshot.setIp(ip);
            snapshot.setJobId("110");
            snapshot.setMobile("18758586900");
            snapshot.setName("测试任务");
            snapshot.setParams("我是参数");
            snapshot.setTarget("com.busgo.cat.job.EchoJob");
            snapshot.setRemark("测试任务备注");
            String content = JSON.toJSONString(snapshot);
            etcdClient.putWithKey(jobSnapshotPath + snapshot.getId(), content);
            log.info("插入任务快照数据:{}", content);
        }


    }

    @Test
    public void test() throws Exception {


        ForestClient forestClient = new ForestClient("http://localhost:2379", "trade", "127.0.0.1");

        System.err.println(forestClient);
        ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();
        jobs.put("com.busgo.cat.job.EchoJob", new EchoJob());
        forestClient.setJobs(jobs);

        forestClient.start();


        System.in.read();


    }
}


```



### 联系方式

如有问题请联系 QQ:466862016 Email:466862016@qq.com 讨论QQ群:806735002 欢迎指点拍砖！