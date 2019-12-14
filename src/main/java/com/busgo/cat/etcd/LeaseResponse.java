package com.busgo.cat.etcd;

import java.io.Serializable;

/**
 * @author busgo
 * @date 2019-12-11 16:54
 */
public class LeaseResponse implements Serializable {



    private Long leaseId;

    private boolean success;



    public Long getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(Long leaseId) {
        this.leaseId = leaseId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


}
