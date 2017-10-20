package com.sds.securitycontroller.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen on 14-11-12.
 */
public class ProxyCommand extends FlowCommandBase {

	private static final long serialVersionUID = 1L;
	long dpid;
    short inPort;
    MatchArguments matchArguments;	// 用于匹配的参数，五元组
    List<RedirectDeviceInfo> devices = new ArrayList<RedirectDeviceInfo>();

    public long getDpid() {
        return dpid;
    }

    public void setDpid(long dpid) {
        this.dpid = dpid;
    }

    public short getInPort() {
        return inPort;
    }

    public void setInPort(short inPort) {
        this.inPort = inPort;
    }

    @Override
    public MatchArguments getMatchArguments() {
        return matchArguments;
    }

    @Override
    public void setMatchArguments(MatchArguments matchArguments) {
        this.matchArguments = matchArguments;
    }

    public List<RedirectDeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(List<RedirectDeviceInfo> devices) {
        this.devices = devices;
    }
}
