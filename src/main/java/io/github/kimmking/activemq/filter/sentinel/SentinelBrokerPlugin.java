package io.github.kimmking.activemq.filter.sentinel;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

public class SentinelBrokerPlugin implements BrokerPlugin {

    public Broker installPlugin(Broker broker) throws Exception {
        SentinelBroker sentinelBroker = new SentinelBroker(broker);
        sentinelBroker.setSendQps(this.getSendQps());
        sentinelBroker.setRecvQps(this.getRecvQps());
        sentinelBroker.setSendQpsPerQueue(this.getSendQpsPerQueue());
        sentinelBroker.setRecvQpsPerQueue(this.getRecvQpsPerQueue());
        sentinelBroker.init();
        return sentinelBroker;
    }


    private int sendQps = 0;
    private int recvQps = 0;
    private int sendQpsPerQueue = 0;
    private int recvQpsPerQueue = 0;

    public int getSendQps() {
        return sendQps;
    }

    public void setSendQps(int sendQps) {
        this.sendQps = sendQps;
    }

    public int getRecvQps() {
        return recvQps;
    }

    public void setRecvQps(int recvQps) {
        this.recvQps = recvQps;
    }

    public int getSendQpsPerQueue() {
        return sendQpsPerQueue;
    }

    public void setSendQpsPerQueue(int sendQpsPerQueue) {
        this.sendQpsPerQueue = sendQpsPerQueue;
    }

    public int getRecvQpsPerQueue() {
        return recvQpsPerQueue;
    }

    public void setRecvQpsPerQueue(int recvQpsPerQueue) {
        this.recvQpsPerQueue = recvQpsPerQueue;
    }


}