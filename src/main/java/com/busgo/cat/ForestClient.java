package com.busgo.cat;


import com.busgo.cat.etcd.EtcdClient;
import com.busgo.cat.job.Job;
import com.busgo.cat.etcd.LeaseResponse;
import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author busgo
 * @date 2019-12-11 15:37
 */
public class ForestClient implements StreamObserver<LeaseKeepAliveResponse> {


    private final static Logger log = LoggerFactory.getLogger(ForestClient.class);


    private final static String jobSnapshotPrefix = "/forest/client/snapshot/%s/%s/";

    private final static String jobExecuteSnapshotPrefix = "/forest/client/execute/snapshot/%s/%s/";

    private final static String jobClientPrefix = "/forest/client/%s/clients/%s";

    private EtcdClient etcdClient;


    private volatile boolean nodeState;


    // 节点标识
    private String ip;


    // 任务工作集群
    private String group;


    // 任务节点路径
    private String jobClientPath;


    // 待执行任务快照目录
    private String jobSnapshotPath;

    // 核心线程数
    private int corePoolSize = Runtime.getRuntime().availableProcessors() * 8;

    // 任务队列容量
    private int capacity = 50;

    // 具体执行任务列表
    private ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();


    // 任务是否已注册
    private volatile boolean registerStatus = false;

    // 是否已初始化
    private boolean init;


    private JobSnapshotProcessor jobSnapshotProcessor;


    public String getIp() {
        return ip;
    }

    public String getGroup() {
        return group;
    }


    public int getCorePoolSize() {
        return corePoolSize;
    }

    public ForestClient(String endpoints, String group, String ip) {

        this.ip = ip;

        this.group = group;

        this.etcdClient = new EtcdClient(endpoints);

        this.jobSnapshotPath = String.format(jobSnapshotPrefix, group, ip);

        this.jobClientPath = String.format(jobClientPrefix, group, ip);


    }


    /**
     * 添加一个任务到新的任务容器中
     *
     * @param target 任务唯一标识
     * @param job    具体任务
     */
    public void pushJob(String target, Job job) {

        this.jobSnapshotProcessor.addJob(target, job);
    }

    /**
     * 初始化
     */
    private void init() {


        String jobExecuteSnapshotPath = String.format(jobExecuteSnapshotPrefix, group, ip);

        this.jobSnapshotProcessor = new JobSnapshotProcessor(etcdClient, jobExecuteSnapshotPath)
                .withCorePoolSize(this.corePoolSize)
                .withJobList(this.jobs)
                .withMaxCapacitySize(this.capacity)
                .build();
        //自动注册节点
        this.loopRegister();
    }


    public ForestClient withCorePoolSize(int corePoolSize) {


        this.corePoolSize = corePoolSize;
        return this;
    }


    public ForestClient withWorkCapacity(int capacity) {

        this.capacity = capacity;
        return this;
    }


    public void start() {

        if (!init) {
            this.init();
            this.init = true;
        }

        // 定时拉取任务
        this.loopWork();
    }

    /**
     * 自旋工作
     */
    private void loopWork() {


        new Thread(() -> {
            while (true) {


                try {


                    if (!nodeState) {
                        log.debug("{}-{}任务节点{}当前非注册状态:", this.group, this.ip, this.jobClientPath);
                        continue;
                    }

                    log.debug("{}-{}任务节点{}开始扫描任务:", this.group, this.ip, this.jobClientPath);

                    List<KeyValue> kvs = etcdClient.getValueListWithPrefix(this.jobSnapshotPath, 20L);

                    if (kvs == null || kvs.size() == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("{}-{}任务节点{}没有要执行的任务:", this.group, this.ip, this.jobClientPath);
                        }

                        Thread.sleep(1500);
                        continue;
                    }


                    for (KeyValue kv : kvs) {

                        String key = kv.getKey().toString(Charset.defaultCharset());

                        if (etcdClient.deleteWithKey(key)) {
                            log.debug("当前任务节点{}-{}领取任务快照:{}成功:", group, ip, key);
                            String value = kv.getValue().toString(Charset.defaultCharset());
                            this.jobSnapshotProcessor.processJobSnapshot(value);


                        }
                    }

                } catch (Exception e) {
                    log.error("", e);

                }


            }
        }).start();
    }


    /**
     * 自旋注册节点信息
     */
    private void loopRegister() {


        if (this.registerStatus) {
            log.info("group:{},ip:{} 注册节点信息:{},正在注册中", this.group, this.ip, this.jobClientPath);
            return;
        }

        this.registerStatus = true;
        this.nodeState = false;

        new Thread(() -> {


            while (true) {

                LeaseResponse response = etcdClient.putNotExistsWithKeepAliveTTL(jobClientPath, ip, 10L, ForestClient.this);

                if (response == null || !response.isSuccess()) {
                    try {
                        log.info("group:{},ip:{} 注册节点信息失败,将重试:{}", this.group, this.ip, this.jobClientPath);
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {

                    }
                    continue;
                }

                log.info("group:{},ip:{} 成功注册节点信息:{}", this.group, this.ip, this.jobClientPath);

                nodeState = true;
                registerStatus = false;
                break;
            }

        }).start();

    }


    public ConcurrentHashMap<String, Job> getJobs() {
        return jobs;
    }

    public void setJobs(ConcurrentHashMap<String, Job> jobs) {
        this.jobs = jobs;
    }

    @Override
    public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {

    }

    @Override
    public void onError(Throwable throwable) {

        // log.error("", throwable);
    }

    @Override
    public void onCompleted() {
        log.warn("group:{},ip:{} 失去注册节点信息:{},发起自动注册", this.group, this.ip, this.jobClientPath);
        this.loopRegister();
    }


}
