package com.poetry.hearing.requestCached;

import org.springframework.util.Assert;

import javax.servlet.ServletRequest;

public class PortResolverImpl implements PortResolver {
    private PortMapper portMapper = new PortMapperImpl();

    public PortResolverImpl() {
    }

    public PortMapper getPortMapper() {
        return this.portMapper;
    }

    public int getServerPort(ServletRequest request) {
        int serverPort = request.getServerPort();
        Integer portLookup = null;
        String scheme = request.getScheme().toLowerCase();
        if ("http".equals(scheme)) {
            portLookup = this.portMapper.lookupHttpPort(serverPort);
        } else if ("https".equals(scheme)) {
            portLookup = this.portMapper.lookupHttpsPort(serverPort);
        }

        if (portLookup != null) {
            serverPort = portLookup;
        }

        return serverPort;
    }

    public void setPortMapper(PortMapper portMapper) {
        Assert.notNull(portMapper, "portMapper cannot be null");
        this.portMapper = portMapper;
    }
}
