package by.mrj.messaging.network.discovery;

import by.mrj.crypto.util.CryptoUtils;
import by.mrj.message.domain.Message;
import by.mrj.message.domain.Registration;
import by.mrj.message.types.Command;
import by.mrj.message.util.MessageUtils;
import by.mrj.message.util.NetUtils;
import by.mrj.messaging.network.transport.Transport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.google.common.base.Splitter;
import com.google.common.io.BaseEncoding;

@Getter
@Log4j2
//@AllArgsConstructor
public class DnsDiscovery implements DiscoveryService {

    private Transport transport;
    private final String dnsAddress;
    private final String networkAddress; // host
    private List<String> nodes;

    public DnsDiscovery(Transport transport, String dnsAddress, String networkAddress) {
        this.transport = transport;
        this.dnsAddress = dnsAddress;
        this.networkAddress = networkAddress;
    }

    @Override
    public List<String> discoverPeers() {
        String publicKey = BaseEncoding.base16().encode(CryptoUtils.pubKey);
        String address = CryptoUtils.sha256ripemd160(publicKey);
        Message<Registration> registrationMessage = MessageUtils.makeMessageWithSig(
                Registration.builder()
                        .networkAddress(networkAddress)
                        .address(address).build(),
                Command.REGISTRATION);

        byte[] bytes = NetUtils.serialize(registrationMessage);
        try (InputStream is = transport.sendWithResponse(bytes, dnsAddress)) {
            Message<Registration> peersResponse = (Message<Registration>) NetUtils.deserialize(is);
            nodes = Splitter.on(";")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(peersResponse.getPayload().getNetworkAddress());
            log.info("Nodes received [{}]", nodes);
            return nodes ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns info about {@param peerName} peer.
     */
    @Override
    public Registration getPeerData(String peerName) {
        return Registration.builder().networkAddress(nodes.get(0)).build();
    }
}
