package com.busgo.cat;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务执行快照
 *
 * @author busgo
 * @date 2019-12-14 17:41
 */
public class JobExecuteSnapshot implements Serializable {


    // 快照id
    private String id;

    // 任务定义名称
    private String jobId;

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

    // 开始时间
    private Date startTime;

    // 结束时间
    private Date finishTime;

    // 状态
    private Integer status;

    // 耗时
    private Long times;

    // 返回结果
    private String result;


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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getTimes() {
        return times;
    }

    public void setTimes(Long times) {
        this.times = times;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    @Override
    public String toString() {
        return "JobExecuteSnapshot{" +
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
                ", startTime=" + startTime +
                ", finishTime=" + finishTime +
                ", status=" + status +
                ", times=" + times +
                ", result='" + result + '\'' +
                '}';
    }
}
