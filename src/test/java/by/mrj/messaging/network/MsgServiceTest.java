package by.mrj.messaging.network;

import by.mrj.messaging.network.domain.Registration;
import by.mrj.crypto.util.EncodingUtils;
import by.mrj.messaging.network.types.Command;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public class MsgServiceTest {

    @Test
    public void signMessage_And_Verify_Success() {
        Message<Registration> message = createBaseMsg();

        log.info("Sig: [{}]", message.getSignature());

        boolean verifySignature = MsgService.verifyMessage(message);
        assertThat(verifySignature).isTrue();
    }

    @Test
    public void signMessage_BrokenSignature_Verify_Caught() {
        Message<Registration> message = createBaseMsg();
        byte[] sig = EncodingUtils.HEX.decode(message.getSignature());
        sig[sig.length - 1] += 1;

        message.setSignature(EncodingUtils.HEX.encode(sig));

        log.info("Sig: [{}]", message.getSignature());

        boolean verifySignature = MsgService.verifyMessage(message);
        assertThat(verifySignature).isFalse();
    }

    private Message<Registration> createBaseMsg() {
        Registration payload = Registration.builder().ip("192.168.0.0").address("My address").build();
        val message = MsgService.makeMessageWithPubKey(payload, Command.REGISTRATION);
        MsgService.signMessage(message);
        return message;
    }
}