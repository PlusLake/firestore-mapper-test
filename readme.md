## Firestore Object Mapper Sample

Firestore's timestamp only accept java.sql.Timestamp or java.util.Date<br>
(See https://github.com/firebase/firebase-admin-java/issues/86)<br>

However those classes are deprecated since Java8.<br>
So this is a sample to convert a object to a Firestore-uploadable-object with reflection

[Main.java](/src/main/java/main/Main.java)

```java
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
```
