package com.busgo.cat.job;

/**
 * 任务抽象接口
 *
 * @author busgo
 * @date 2019-12-12 09:26
 */
public interface Job {

    /**
     * 执行任务
     *
     * @param param 参数
     * @return
     */
    String execute(String param);
}
