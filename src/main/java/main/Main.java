package main;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.cloud.firestore.*;

import lombok.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Firestore firestore = FirestoreOptions.getDefaultInstance().getService();

        // Some raw java objects. (Which can normally persist by JPA)
        SomeObject foo = new SomeObject(123, "hello world with firestore!", LocalDate.now(), LocalDateTime.now());

        // Convert it to a map and convert LocalDate / LocalDateTime to java.sql.Timestamp
        Map<String, Object> map = FirebaseObjectConverter.convert(foo);

        // Save it to Cloud Firestore
        firestore.collection("java").add(map).get();

    }

    @Data
    @AllArgsConstructor
    public static class SomeObject {
        private int someNumber;
        private String someText;
        private LocalDate someDate;
        private LocalDateTime someTime;
    }

    public static class FirebaseObjectConverter {
        public static Map<String, Object> convert(Object object) throws Exception {
            Map<String, Object> map = new HashMap<>();
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), FirebaseConvertTypes.convert(field.get(object)));
            }
            return map;
        }
    }

    @Getter
    public enum FirebaseConvertTypes {
        LocalDate(LocalDate.class, object -> java.sql.Timestamp.valueOf(((LocalDate) object).atStartOfDay())),
        LocalDateTime(LocalDateTime.class, object -> java.sql.Timestamp.valueOf((LocalDateTime) object));

        private final Function<Object, Object> processor;
        private final Class<?> type;

        private FirebaseConvertTypes(Class<?> type, Function<Object, Object> processor) {
            this.processor = processor;
            this.type = type;
        }

        private static Object convert(Object object) {
            return Stream.of(FirebaseConvertTypes.values())
                    .filter(processor -> object != null && processor.type.equals(object.getClass()))
                    .findAny()
                    .map(processor -> processor.processor)
                    .orElse(o -> o)
                    .apply(object);
        }
    }

}
