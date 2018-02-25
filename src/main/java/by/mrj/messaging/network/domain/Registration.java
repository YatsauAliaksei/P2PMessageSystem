package by.mrj.messaging.network.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

import static by.mrj.crypto.util.CryptoUtils.doubleSha256;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString
@Builder
@EqualsAndHashCode
public class Registration implements Serializable, Hashable {
    @NonNull
    String ip;
    @NonNull
    String address;

    @Override
    public String hash() {
        return doubleSha256(ip + address);
    }
}
