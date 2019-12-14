package com.busgo.cat;

/**
 * 任务快照执行状态
 *
 * @author busgo
 * @date 2019-12-14 17:42
 */
public interface JobExecuteStatus {

    // 执行中
    int JobExecuteDoingStatus = 1;

    // 执行成功
    int JobExecuteSuccessStatus = 2;

    // 未知
    int JobExecuteUkonwStatus = 3;

    // 执行失败
    int JobExecuteErrorStatus = -1;
}
