package com.busgo.cat;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Date;

/**
 * @author busgo
 * @date 2019-12-11 15:37
 */
public class JobSnapshot implements Serializable {

    // 快照id
    private String id;

    // 任务定义名称
    private String jobId;

    // 任务名称
    private String name;

    // 集群
    private String group;

    // corn表达式
    private String cron;

    // 目标任务
    private String target;

    // ip
    private String ip;

    // 参数
    private String params;

    // 手机号码
    private String mobile;

    // 备注
    private String remark;

    //创建时间
    private Date createTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    @Override
    public String toString() {
        return "JobSnapshot{" +
                "id='" + id + '\'' +
                ", jobId='" + jobId + '\'' +
                ", name='" + name + '\'' +
                ", group='" + group + '\'' +
                ", cron='" + cron + '\'' +
                ", target='" + target + '\'' +
                ", ip='" + ip + '\'' +
                ", params='" + params + '\'' +
                ", mobile='" + mobile + '\'' +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                '}';
    }


    public static void main(String[] args) {


        JobSnapshot snapshot = new JobSnapshot();
        snapshot.setId("110");
        snapshot.setCreateTime(new Date());
        snapshot.setCron("0 0 * * * ?");
        snapshot.setGroup("trade");
        snapshot.setIp("127.0.0.1");
        snapshot.setJobId("110");
        snapshot.setMobile("18758586900");
        snapshot.setName("测试任务");
        snapshot.setParams("我是参数");
        snapshot.setTarget("com.busgo.cat.job.EchoJob");
        snapshot.setRemark("测试任务备注");

        System.err.println(JSON.toJSONString(snapshot));
    }
}
