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
                map.put(field.getName(), FirebaseObjectConvertTypes.convert(field.get(object)));
            }
            return map;
        }
    }

    @Getter
    public enum FirebaseObjectConvertTypes {
        LocalDate(LocalDate.class, object -> java.sql.Timestamp.valueOf(((LocalDate) object).atStartOfDay())),
        LocalDateTime(LocalDateTime.class, object -> java.sql.Timestamp.valueOf((LocalDateTime) object));

        private final Function<Object, Object> converter;
        private final Class<?> type;

        private FirebaseObjectConvertTypes(Class<?> type, Function<Object, Object> converter) {
            this.converter = converter;
            this.type = type;
        }

        private static Object convert(Object object) {
            return Stream.of(FirebaseObjectConvertTypes.values())
                    .filter(converter -> object != null && converter.type.equals(object.getClass()))
                    .findAny()
                    .map(converter -> converter.converter)
                    .orElse(o -> o)
                    .apply(object);
        }
    }

}
