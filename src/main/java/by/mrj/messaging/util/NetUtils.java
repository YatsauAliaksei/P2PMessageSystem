package by.mrj.messaging.util;

import by.mrj.messaging.network.Message;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@Log4j2
public abstract class NetUtils {
    public static final String MAGIC = "MAGIC";

    public static byte[] serialize(Message<?> object) { // xxx: Possible should work only with Message type. Some for below.
        return serialize(object, true);
    }

    public static byte[] serialize(Serializable object, boolean withMagic) { // xxx: Possible should work only with Message type. Some for below.
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            if (withMagic)
                oos.writeBytes(MAGIC); // first step check

            oos.writeObject(object);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return clazz.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Message<?> deserialize(InputStream is) {
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            checkMagic(ois);
            return (Message) ois.readObject(); // command check to know validations to be performed.
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Works only with Message deserialization
    private static void checkMagic(ObjectInputStream in) throws IOException {
        byte[] buf = new byte[MAGIC.length()];
        in.read(buf, 0, buf.length);
        String exMagic = new String(buf);

        if (!MAGIC.equals(exMagic)) {
            in.close();
            throw new RuntimeException("Wrong magic"); // todo
        }
    }
}
