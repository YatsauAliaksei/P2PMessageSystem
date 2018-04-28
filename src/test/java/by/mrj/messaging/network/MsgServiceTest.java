package by.mrj.messaging.network;

import by.mrj.message.domain.Message;
import by.mrj.message.domain.Registration;
import by.mrj.crypto.util.EncodingUtils;
import by.mrj.message.types.Command;
import by.mrj.message.util.MessageUtils;
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

        boolean verifySignature = MessageUtils.verifyMessage(message);
        assertThat(verifySignature).isTrue();
    }

    @Test
    public void signMessage_BrokenSignature_Verify_Caught() {
        Message<Registration> message = createBaseMsg();
        byte[] sig = EncodingUtils.HEX.decode(message.getSignature());
        sig[sig.length - 1] += 1;

        message.setSignature(EncodingUtils.HEX.encode(sig));

        log.info("Sig: [{}]", message.getSignature());

        boolean verifySignature = MessageUtils.verifyMessage(message);
        assertThat(verifySignature).isFalse();
    }

    private Message<Registration> createBaseMsg() {
        Registration payload = Registration.builder().networkAddress("192.168.0.0").address("My address").build();
        val message = MessageUtils.makeMessageWithPubKey(payload, Command.REGISTRATION);
        MessageUtils.signMessage(message);
        return message;
    }
}