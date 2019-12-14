package com.busgo.cat.etcd;

import com.busgo.cat.ForestClient;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author busgo
 * @date 2019-12-11 16:03
 */
public class EtcdClient {


    private final static Logger log = LoggerFactory.getLogger(ForestClient.class);


    private String endpoints;


    private Client client;


    private KV kvClient;


    private Lease leaseClient;


    public EtcdClient(String endpoints) {
        this.endpoints = endpoints;
        this.init();
    }


    private void init() {


        this.client = Client.builder().endpoints(this.endpoints).build();
        this.kvClient = this.client.getKVClient();
        this.leaseClient = this.client.getLeaseClient();
    }

    /**
     * @param prefix key前缀
     * @param limit  每次拉去条数
     * @return
     */
    public List<KeyValue> getValueListWithPrefix(String prefix, Long limit) {

        ByteSequence byteSequence = ByteSequence.from(prefix, Charset.defaultCharset());

        try {


            GetResponse getResponse = this.kvClient.get(byteSequence, GetOption.newBuilder().withLimit(limit).withPrefix(byteSequence).build()).get();

            // 空的 key
            if (getResponse.getCount() == 0) return new ArrayList<>();
            for (KeyValue kv : getResponse.getKvs()) {

                kv.getKey();
            }

            return getResponse.getKvs();

        } catch (Exception e) {

            log.error("", e);
            return null;
        }
    }


    /**
     * @param key      key
     * @param ttl      ttl
     * @param observer 观察者对象
     * @return
     */
    public LeaseResponse putNotExistsWithKeepAliveTTL(String key, String value, long ttl, StreamObserver<LeaseKeepAliveResponse> observer) {


        try {
            LeaseGrantResponse grantResponse = this.leaseClient.grant(ttl).get();
            long leaseId = grantResponse.getID();


            this.leaseClient.keepAlive(leaseId, observer);


            Txn txn = this.kvClient.txn();

            txn.If(
                    new Cmp(ByteSequence.from(key.getBytes()), Cmp.Op.EQUAL, CmpTarget.version(0L))
            ).Then(
                    Op.put(ByteSequence.from(key.getBytes()), ByteSequence.from(value.getBytes()), PutOption.newBuilder().withLeaseId(leaseId).build())
            );


            TxnResponse txnResponse = txn.commit().get();

            LeaseResponse response = new LeaseResponse();
            response.setLeaseId(leaseId);
            response.setSuccess(txnResponse.isSucceeded());
            if (!txnResponse.isSucceeded()) {
                this.leaseClient.revoke(leaseId).get();
            }

            return response;

        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }


    /**
     * 创建一个key
     *
     * @param key   key
     * @param value value
     * @return
     */
    public boolean putWithKey(String key, String value) {


        try {
            this.kvClient.put(ByteSequence.from(key, Charset.defaultCharset()), ByteSequence.from(value, Charset.defaultCharset()), PutOption.newBuilder().build()).get();
            return true;
        } catch (Exception e) {
            log.error("", e);
        }

        return false;

    }

    /**
     * 根据租约id 释放租约
     *
     * @param leaseId 租约id
     * @return
     * @throws Exception
     */
    public boolean releaseLeaseId(long leaseId) throws Exception {

        this.leaseClient.revoke(leaseId).get();

        return true;
    }

    /**
     * 根据key 删除
     *
     * @param key key
     * @return
     */
    public boolean deleteWithKey(String key) {


        try {
            DeleteResponse deleteResponse = this.kvClient.delete(ByteSequence.from(key.getBytes())).get();

            return deleteResponse.getDeleted() > 0;
        } catch (Exception e) {
            log.error("", e);
        }


        return false;

    }
}
