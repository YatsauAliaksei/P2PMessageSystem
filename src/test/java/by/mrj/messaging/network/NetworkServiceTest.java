package by.mrj.messaging.network;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

@Slf4j
public class NetworkServiceTest {


    @Test
    @Ignore
    public void dnsLookup_Bitcoin_Full_Nodes() {
        Set<String> strings = dnsLookup("seed.bitcoin.sipa.be", "A");
        log.info(strings.toString());
    }

    @SneakyThrows
    public Set<String> dnsLookup(String host, String type) {
        Hashtable<String, String> envProps = new Hashtable<>();
        envProps.put(Context.PROVIDER_URL, "dns://8.8.8.8/");
        envProps.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        DirContext dnsContext = new InitialDirContext(envProps);
        Attributes dnsEntries = dnsContext.getAttributes(host, new String[]{type});

        Set<String> results = new HashSet<>();
        if (dnsEntries != null) {
            NamingEnumeration<?> dnsEntryIterator = dnsEntries.get(type).getAll();
            while (dnsEntryIterator.hasMoreElements()) {
                results.add(dnsEntryIterator.next().toString());
            }
        }

        return results;
    }

}