package com.poetry.hearing.requestCached;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PortMapperImpl implements PortMapper {
    private final Map<Integer, Integer> httpsPortMappings = new HashMap();

    public PortMapperImpl() {
        this.httpsPortMappings.put(80, 443);
        this.httpsPortMappings.put(8080, 8443);
    }

    public Map<Integer, Integer> getTranslatedPortMappings() {
        return this.httpsPortMappings;
    }

    public Integer lookupHttpPort(Integer httpsPort) {
        Iterator var2 = this.httpsPortMappings.keySet().iterator();

        Integer httpPort;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            httpPort = (Integer)var2.next();
        } while(!((Integer)this.httpsPortMappings.get(httpPort)).equals(httpsPort));

        return httpPort;
    }

    public Integer lookupHttpsPort(Integer httpPort) {
        return (Integer)this.httpsPortMappings.get(httpPort);
    }

    public void setPortMappings(Map<String, String> newMappings) {
        Assert.notNull(newMappings, "A valid list of HTTPS port mappings must be provided");
        this.httpsPortMappings.clear();
        Iterator var2 = newMappings.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var2.next();
            Integer httpPort = Integer.valueOf((String)entry.getKey());
            Integer httpsPort = Integer.valueOf((String)entry.getValue());
            if (httpPort < 1 || httpPort > 65535 || httpsPort < 1 || httpsPort > 65535) {
                throw new IllegalArgumentException("one or both ports out of legal range: " + httpPort + ", " + httpsPort);
            }

            this.httpsPortMappings.put(httpPort, httpsPort);
        }

        if (this.httpsPortMappings.size() < 1) {
            throw new IllegalArgumentException("must map at least one port");
        }
    }
}

