package com.sds.securitycontroller.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chen on 14-9-15.
 */
public class FlowAction {

    List<Integer> output = new ArrayList<>();        /* Output to switch port. */
    short vlanId;        /* Set the 802.1q VLAN id. */
    byte vlanPcp;        /* Set the 802.1q priority. */
    boolean stripVlan;      /* Strip the 802.1q header. */
    String dlSrc;        /* Ethernet source address. */
    String dlDst;        /* Ethernet destination address. */
    String nwSrc;           /* IP source address. */
    String nwDst;           /* IP destination address. */
    byte nwTos;          /* IP ToS (DSCP field, 6 bits). */
    short tpSrc;         /* TCP/UDP source port. */
    short tpDst;         /* TCP/UDP destination port. */
    int[] enqueue = new int[2];          /* 0 port 1 queueId. Output to queue. */


    public FlowAction(Integer port) {
        this.output.add(port);
    }

    public FlowAction() {
        super();
    }



    public List<Integer> getOutput() {
        return output;
    }

    public void setOutput(List<Integer> output) {
        this.output = output;
    }

    public short getVlanId() {
        return vlanId;
    }

    public void setVlanId(short vlanId) {
        this.vlanId = vlanId;
    }

    public byte getVlanPcp() {
        return vlanPcp;
    }

    public void setVlanPcp(byte vlanPcp) {
        this.vlanPcp = vlanPcp;
    }

    public boolean isStripVlan() {
        return stripVlan;
    }

    public void setStripVlan(boolean stripVlan) {
        this.stripVlan = stripVlan;
    }

    public String getDlSrc() {
        return dlSrc;
    }

    public void setDlSrc(String dlSrc) {
        this.dlSrc = dlSrc;
    }

    public String getDlDst() {
        return dlDst;
    }

    public void setDlDst(String dlDst) {
        this.dlDst = dlDst;
    }

    public String getNwSrc() {
        return nwSrc;
    }

    public void setNwSrc(String nwSrc) {
        this.nwSrc = nwSrc;
    }

    public String getNwDst() {
        return nwDst;
    }

    public void setNwDst(String nwDst) {
        this.nwDst = nwDst;
    }

    public byte getNwTos() {
        return nwTos;
    }

    public void setNwTos(byte nwTos) {
        this.nwTos = nwTos;
    }

    public short getTpSrc() {
        return tpSrc;
    }

    public void setTpSrc(short tpSrc) {
        this.tpSrc = tpSrc;
    }

    public short getTpDst() {
        return tpDst;
    }

    public void setTpDst(short tpDst) {
        this.tpDst = tpDst;
    }

    public int[] getEnqueue() {
        return enqueue;
    }

    public void setEnqueue(int[] enqueue) {
        this.enqueue = enqueue;
    }

    @Override
    public String toString() {
        return "FlowAction{" +
                "output=" + output +
                ", vlanId=" + vlanId +
                ", vlanPcp=" + vlanPcp +
                ", stripVlan=" + stripVlan +
                ", dlSrc=" + dlSrc +
                ", dlDst=" + dlDst +
                ", nwSrc=" + nwSrc +
                ", nwDst=" + nwDst +
                ", nwTos=" + nwTos +
                ", tpSrc=" + tpSrc +
                ", tpDst=" + tpDst +
                ", enqueue=" + Arrays.toString(enqueue) +
                '}';
    }
};