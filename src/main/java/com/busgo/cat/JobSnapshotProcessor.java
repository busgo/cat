package com.busgo.cat;


import com.alibaba.fastjson.JSON;
import com.busgo.cat.etcd.EtcdClient;
import com.busgo.cat.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @author busgo
 * @date 2019-12-12 12:02
 */
public class JobSnapshotProcessor {


    private final Logger log = LoggerFactory.getLogger(JobSnapshotProcessor.class);


    private final int DEFAULT_MAX_CAPACITY_SIZE = 100;

    // 执行任务线程池
    private ThreadPoolExecutor exec;

    // 执行任务队列
    private LinkedBlockingQueue<Runnable> workers;


    // 执行任务快照目录
    private String jobExecuteSnapshotPath;

    // 核心线程数
    private int corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;

    private EtcdClient etcdClient;


    private int maxCapacitySize = DEFAULT_MAX_CAPACITY_SIZE;


    // 具体执行任务容器
    private ConcurrentHashMap<String, Job> jobContainer;


    public JobSnapshotProcessor(EtcdClient etcdClient, String jobExecuteSnapshotPath) {


        this.jobExecuteSnapshotPath = jobExecuteSnapshotPath;
        this.etcdClient = etcdClient;
        this.jobContainer = new ConcurrentHashMap<>();
    }


    public JobSnapshotProcessor withCorePoolSize(int corePoolSize) {

        this.corePoolSize = corePoolSize;
        return this;
    }


    public JobSnapshotProcessor withMaxCapacitySize(int maxCapacitySize) {
        this.maxCapacitySize = maxCapacitySize;
        return this;
    }


    public JobSnapshotProcessor withJobList(ConcurrentHashMap<String, Job> jobList) {

        if (jobList == null || jobList.isEmpty()) return this;

        for (String key : jobList.keySet()) {
            this.jobContainer.putIfAbsent(key.trim(), jobList.get(key));
        }

        return this;
    }


    public JobSnapshotProcessor build() {

        this.workers = new LinkedBlockingQueue<>(this.maxCapacitySize);
        this.exec = new ThreadPoolExecutor(this.corePoolSize,
                this.corePoolSize, 5L, TimeUnit.MINUTES, this.workers, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        return this;

    }


    /**
     * 添加任务
     *
     * @param target 任务标识
     * @param job    任务
     */
    public void addJob(String target, Job job) {

        if (job == null) return;
        this.jobContainer.putIfAbsent(target, job);
    }

    /**
     * 处理任务快照
     *
     * @param value value 任务快照内容
     */
    public void processJobSnapshot(String value) {


        JobSnapshot jobSnapshot = this.parseJobSnapshot(value);

        if (jobSnapshot == null) {
            log.warn("the job snapshot can not parse:{}", value);
            return;
        }

        JobExecuteSnapshot snapshot = this.buildJobExecuteSnapshot(jobSnapshot);

        this.exec.submit(new JobWorker(snapshot));

    }


    /**
     * 构建任务执行快照
     *
     * @param jobSnapshot 任务快照
     * @return
     */
    private JobExecuteSnapshot buildJobExecuteSnapshot(JobSnapshot jobSnapshot) {

        JobExecuteSnapshot executeSnapshot = new JobExecuteSnapshot();

        executeSnapshot.setId(jobSnapshot.getId());
        executeSnapshot.setJobId(jobSnapshot.getJobId());
        executeSnapshot.setCreateTime(jobSnapshot.getCreateTime());
        executeSnapshot.setGroup(jobSnapshot.getGroup());
        executeSnapshot.setIp(jobSnapshot.getIp());
        executeSnapshot.setMobile(jobSnapshot.getMobile());
        executeSnapshot.setName(jobSnapshot.getName());
        executeSnapshot.setTarget(jobSnapshot.getTarget());
        executeSnapshot.setParams(jobSnapshot.getParams());
        executeSnapshot.setRemark(jobSnapshot.getRemark());
        executeSnapshot.setTimes(0L);
        executeSnapshot.setCron(jobSnapshot.getCron());
        executeSnapshot.setStartTime(new Date());
        executeSnapshot.setCreateTime(jobSnapshot.getCreateTime());
        executeSnapshot.setStatus(JobExecuteStatus.JobExecuteDoingStatus);

        return executeSnapshot;
    }


    /**
     * 解析任务快照
     *
     * @param content 内容
     * @return
     */
    private JobSnapshot parseJobSnapshot(String content) {


        try {

            if (content == null || content.equals("")) {
                log.warn("parse job snapshot content is null");
                return null;
            }

            return JSON.parseObject(content, JobSnapshot.class);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("parse job snapshot error:{}", e.getMessage(), e);
            return null;
        }
    }


    private class JobWorker implements Runnable {

        private JobExecuteSnapshot snapshot;

        public JobWorker(JobExecuteSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public void run() {


            String target = snapshot.getTarget();

            Job job = jobContainer.get(target);

            String path = jobExecuteSnapshotPath + snapshot.getId();
            if (job == null) {
                log.warn("the job snapshot:{} not found target job", snapshot);
                snapshot.setStatus(JobExecuteStatus.JobExecuteUkonwStatus);
                snapshot.setResult("此任务作业不存在");
            }

            // 添加任务
            etcdClient.putWithKey(path, JSON.toJSONString(snapshot));

            if (JobExecuteStatus.JobExecuteDoingStatus != snapshot.getStatus() || job == null) {
                return;
            }


            String params = snapshot.getParams();


            try {
                log.debug("the job execute:{} start execute", this.snapshot);

                String result = job.execute(params);
                Date finishTime = new Date();
                long times = finishTime.getTime() - this.snapshot.getStartTime().getTime();
                this.snapshot.setTimes(times / 1000);
                this.snapshot.setResult(result);
                this.snapshot.setStatus(JobExecuteStatus.JobExecuteSuccessStatus);
                // update  job execute snapshot
                etcdClient.putWithKey(path, JSON.toJSONString(this.snapshot));
                log.debug("the job execute:{} finish execute,耗时:{}秒", this.snapshot, times / 1000);

            } catch (Exception e) {

                log.error("he job execute:{}  execute fail:{}", this.snapshot, e);
                this.snapshot.setStatus(JobExecuteStatus.JobExecuteErrorStatus);
                this.snapshot.setResult(e.getMessage());
                etcdClient.putWithKey(path, JSON.toJSONString(this.snapshot));
            }


        }
    }
}
