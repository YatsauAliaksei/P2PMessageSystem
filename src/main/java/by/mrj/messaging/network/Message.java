package by.mrj.messaging.network;

import by.mrj.crypto.util.CryptoUtils;
import by.mrj.messaging.network.domain.Hashable;
import by.mrj.messaging.network.types.Command;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString
@Builder
@EqualsAndHashCode
public class Message<T extends Hashable & Serializable> implements Hashable, Serializable {

    @NonNull
    Command command; //?
    //    long length;
    @NonNull
    String checksum;
    @NonNull
    String address;
    @NonNull
    T payload;
    @NonNull
    String publicKey;
    @Setter
    String signature;

    public static <T extends Hashable & Serializable> MessageBuilder<T> builder() {return new MessageBuilder<T>();}

    @Override
    public String hash() {
        return CryptoUtils.doubleSha256(payload.hash() + address + publicKey + command);
    }
}
