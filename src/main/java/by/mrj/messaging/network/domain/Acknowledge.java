package by.mrj.messaging.network.domain;

import by.mrj.crypto.util.CryptoUtils;
import by.mrj.messaging.network.types.ResponseStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString
@Builder
@EqualsAndHashCode
public class Acknowledge implements Hashable, Serializable {

    @NonNull
    String address;
    @NonNull
    ResponseStatus responseStatus;
    @NonNull
    String correlationId;

    @Override
    public String hash() {
        return CryptoUtils.doubleSha256(address + responseStatus + correlationId);
    }
}
