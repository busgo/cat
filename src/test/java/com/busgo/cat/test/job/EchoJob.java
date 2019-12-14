package com.busgo.cat.test.job;

import com.busgo.cat.job.Job;

/**
 * @author busgo
 * @date 2019-12-14 19:00
 */
public class EchoJob implements Job {
    @Override
    public String execute(String param) {

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "执行完成任务了耗时1000秒";
    }
}
