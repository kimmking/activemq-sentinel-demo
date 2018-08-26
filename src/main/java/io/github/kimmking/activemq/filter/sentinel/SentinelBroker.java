package io.github.kimmking.activemq.filter.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.apache.activemq.broker.*;
import org.apache.activemq.command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SentinelBroker extends BrokerFilter {

    private static final Logger logger = LoggerFactory.getLogger(SentinelBroker.class);

    public final String ACTIVEMQ_SEND   = "ACTIVEMQ_SEND";
    public final String ACTIVEMQ_PULL   = "ACTIVEMQ_PULL";

    private List<FlowRule> rules = new ArrayList<FlowRule>();
    private Map<String,FlowRule> ruleMap = new HashMap<String, FlowRule>();

    public SentinelBroker(Broker next) {
        super(next);
    }

    public void init() {
        initRules(ACTIVEMQ_SEND,this.getSendQps());
        initRules(ACTIVEMQ_PULL,this.getRecvQps());
    }
    public void initRules(String res, int qps) {
        if(qps>0 && !ruleMap.containsKey(res)) {
            FlowRule rule = new FlowRule();
            rule.setResource(res);
            rule.setCount(qps);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rules.add(rule);
            ruleMap.put(res,rule);
            FlowRuleManager.loadRules(rules);
        }
    }


    @Override
    public void send(ProducerBrokerExchange producerExchange, Message messageSend) throws Exception {
        Entry entry = null;
        Entry entryQueue = null;
        String resourceName = ACTIVEMQ_SEND;
        String resourceNameQueue = ACTIVEMQ_SEND + "_" + messageSend.getDestination().getQualifiedName();
        initRules(resourceNameQueue,this.getSendQpsPerQueue());
        try {
            ContextUtil.enter(resourceNameQueue);
            entry = SphU.entry(resourceName, EntryType.IN);
            entryQueue = SphU.entry(resourceNameQueue, EntryType.IN);
            super.send(producerExchange, messageSend);
        } catch (BlockException ex) {
            logger.error("SentinelBroker Send Blocked.", ex);
            processException(ex);
        } finally {
            if (entryQueue != null) {
                entryQueue.exit();
            }
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    @Override
    public void preProcessDispatch(MessageDispatch messageDispatch) {
        Entry entry = null;
        Entry entryQueue = null;
        String resourceName = ACTIVEMQ_PULL;
        String resourceNameQueue = ACTIVEMQ_PULL + "_" + messageDispatch.getDestination().getQualifiedName();
        initRules(resourceNameQueue,this.getRecvQpsPerQueue());
        try {
            ContextUtil.enter(resourceNameQueue);
            entry = SphU.entry(resourceName, EntryType.OUT);
            entryQueue = SphU.entry(resourceNameQueue, EntryType.OUT);
            super.preProcessDispatch(messageDispatch);
        } catch (BlockException ex) {
            logger.error("SentinelBroker dispatch Blocked.", ex);
            processException(ex);
        } finally {
            if (entryQueue != null) {
                entryQueue.exit();
            }
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    private void processException(BlockException ex){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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